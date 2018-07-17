/*
 * Copyright (C) 2018 Heinrich-Heine-Universitaet Duesseldorf, Institute of Computer Science,
 * Department Operating Systems
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package de.hhu.bsinfo.dxutils.stats;

import java.util.ArrayList;

/**
 * Statistics operation to record values which stores the full history
 * to determine percentiles
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 08.03.2018
 */
public class ValuePercentile extends AbstractOperation {
    static final int SLOT_SIZE = 100000;

    ArrayList<long[]> m_slots = new ArrayList<>();
    int m_index;

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    public ValuePercentile(final Class<?> p_class, final String p_name) {
        super(p_class, p_name);

        m_index = 0;
    }

    /**
     * Delete all registered values.
     */
    public void deleteValues() {
        m_slots = new ArrayList<>();
        m_index = 0;
    }

    /**
     * Sort all registered values (ascending). Call this before getting any percentile values
     * to update the internal state.
     */
    public void sortValues() {
        if (m_slots.isEmpty()) {
            return;
        }

        quickSort(0, (m_slots.size() - 1) * SLOT_SIZE + m_index - 1);
    }

    /**
     * Get the score for the Xth percentile of all recorded values
     *
     * @param p_percentile
     *         the percentile
     * @return Score of specified percentile
     */
    public long getPercentileScore(final float p_percentile) {
        if (p_percentile <= 0.0 || p_percentile >= 1.0) {
            throw new IllegalArgumentException("Percentile must be in (0.0, 1.0)!");
        }

        if (m_slots.isEmpty()) {
            return 0;
        }

        int size = (m_slots.size() - 1) * SLOT_SIZE + m_index;
        long index = (long) Math.ceil(p_percentile * size) - 1;

        return m_slots.get((int) (index / SLOT_SIZE))[(int) (index % SLOT_SIZE)];
    }

    /**
     * Record a single value
     *
     * @param p_value
     *         Value to record
     */
    public void record(final long p_value) {
        long[] arr;

        if (m_index % SLOT_SIZE == 0) {
            arr = new long[SLOT_SIZE];
            m_slots.add(arr);
            m_index = 0;
        } else {
            arr = m_slots.get(m_slots.size() - 1);
        }

        arr[m_index++] = p_value;
    }

    /**
     * "Debug version". Identical to normal call but is removed on non-debug builds.
     */
    public void recordDebug(final long p_value) {
        record(p_value);
    }

    @Override
    public String dataToString(final String p_indent, final boolean p_extended) {
        if (p_extended) {
            sortValues();

            return "95th percentile " + getPercentileScore(0.95f) + ";99th percentile " + getPercentileScore(0.99f) +
                    ";99.9th percentile " + getPercentileScore(0.999f);
        } else {
            // don't print percentile for debug output because sorting might take too long if there are too many values
            return "*** Suppressed to avoid performance penalties ***";
        }
    }

    @Override
    public String generateCSVHeader(final char p_delim) {
        return "95th percentile" + p_delim + "99th percentile" + p_delim + "99.9th percentile";
    }

    @Override
    public String toCSV(final char p_delim) {
        sortValues();

        return Long.toString(getPercentileScore(0.95f)) + p_delim + getPercentileScore(0.99f) + p_delim +
                getPercentileScore(0.999f);
    }

    /**
     * Quicksort implementation.
     *
     * @param p_lowerIndex
     *         the lower index
     * @param p_higherIndex
     *         the higher index
     */
    private void quickSort(int p_lowerIndex, int p_higherIndex) {
        int i = p_lowerIndex;
        int j = p_higherIndex;
        int index = p_lowerIndex + (p_higherIndex - p_lowerIndex) / 2;
        long pivot = m_slots.get(index / SLOT_SIZE)[index % SLOT_SIZE];

        while (i <= j) {
            while (m_slots.get(i / SLOT_SIZE)[i % SLOT_SIZE] < pivot) {
                i++;
            }

            while (m_slots.get(j / SLOT_SIZE)[j % SLOT_SIZE] > pivot) {
                j--;
            }

            if (i <= j) {
                long temp = m_slots.get(i / SLOT_SIZE)[i % SLOT_SIZE];
                m_slots.get(i / SLOT_SIZE)[i % SLOT_SIZE] = m_slots.get(j / SLOT_SIZE)[j % SLOT_SIZE];
                m_slots.get(j / SLOT_SIZE)[j % SLOT_SIZE] = temp;

                i++;
                j--;
            }
        }

        if (p_lowerIndex < j) {
            quickSort(p_lowerIndex, j);
        }

        if (i < p_higherIndex) {
            quickSort(i, p_higherIndex);
        }
    }
}
