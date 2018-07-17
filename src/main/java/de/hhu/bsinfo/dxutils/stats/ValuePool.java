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

/**
 * (Thread safe) value operation using a pool with per thread local value operations
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public class ValuePool extends OperationPool {

    /**
     * Constructor
     * Defaults to Base 10.
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    public ValuePool(final Class<?> p_class, final String p_name) {
        super(Value.class, p_class, p_name);
    }

    /**
     * Get the counter value of all threads summed up
     *
     * @return Counter value
     */
    public long getCounter() {
        long val = 0;

        for (AbstractOperation[] opArr : m_pool) {
            for (AbstractOperation op : opArr) {
                if (op != null) {
                    val += ((Value) op).getCounter();
                }
            }
        }

        return val;
    }

    /**
     * Get the counter value of all threads summed up
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Counter value scaled to specified prefix
     */
    public double getCounter(final Value.Prefix p_prefix) {
        return getCounter() / (double) Value.MS_PREFIX_TABLE[Value.Base.B_10.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the total value of all threads summed up
     *
     * @return Total value
     */
    public long getTotalValue() {
        long val = 0;

        for (AbstractOperation[] opArr : m_pool) {
            for (AbstractOperation op : opArr) {
                if (op != null) {
                    val += ((Value) op).getTotalValue();
                }
            }
        }

        return val;
    }

    /**
     * Get the total value
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Total value scaled to specified prefix
     */
    public double getTotalValue(final Value.Prefix p_prefix) {
        return getTotalValue() / (double) Value.MS_PREFIX_TABLE[Value.Base.B_10.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the average value of all applied values of all threads
     *
     * @return Average value
     */
    public double getAvgValue() {
        long counter = getCounter();

        return counter == 0 ? 0.0 : (double) getTotalValue() / counter;
    }

    /**
     * Get the average value of all applied values of all threads
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Average value scaled to specified prefix
     */
    public double getAvgValue(final Value.Prefix p_prefix) {
        return getAvgValue() / (double) Value.MS_PREFIX_TABLE[Value.Base.B_10.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the min value of all applied values of all threads
     *
     * @return Min value
     */
    public long getMinValue() {
        long min = Long.MAX_VALUE;

        for (AbstractOperation[] opArr : m_pool) {
            for (AbstractOperation op : opArr) {
                if (op != null) {
                    long val = ((Value) op).getMinValue();

                    if (val < min) {
                        min = val;
                    }
                }
            }
        }

        return min;
    }

    /**
     * Get the min value of all applied values
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Min value scaled to specified prefix
     */
    public double getMinValue(final Value.Prefix p_prefix) {
        return getMinValue() / (double) Value.MS_PREFIX_TABLE[Value.Base.B_10.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the max value of all applied values of all threads
     *
     * @return Max value
     */
    public long getMaxValue() {
        long max = Long.MIN_VALUE;

        for (AbstractOperation[] opArr : m_pool) {
            for (AbstractOperation op : opArr) {
                if (op != null) {
                    long val = ((Value) op).getMaxValue();

                    if (val > max) {
                        max = val;
                    }
                }
            }
        }

        return max;
    }

    /**
     * Get the max value of all applied values of all threads
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Max value scaled to specified prefix
     */
    public double getMaxValue(final Value.Prefix p_prefix) {
        return getMaxValue() / (double) Value.MS_PREFIX_TABLE[Value.Base.B_10.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Increment the total value by 1
     */
    public void inc() {
        ((Value) getThreadLocalValue()).inc();
    }

    /**
     * Add a value to the total value
     *
     * @param p_val
     *         Value to add to the total value
     */
    public void add(final long p_val) {
        ((Value) getThreadLocalValue()).add(p_val);
    }

    /**
     * "Debug version". Identical to normal call but is removed on non-debug builds.
     */
    public void incDebug() {
        inc();
    }

    /**
     * "Debug version". Identical to normal call but is removed on non-debug builds.
     */
    public void addDebug(final long p_val) {
        add(p_val);
    }
}
