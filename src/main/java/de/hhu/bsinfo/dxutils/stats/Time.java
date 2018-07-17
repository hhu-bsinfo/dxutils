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
 * Statistics operation to measure time
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public class Time extends AbstractOperation {
    public enum Prefix {
        NANO, MICRO, MILLI, SEC
    }

    static final double[] MS_PREFIX_TABLE = {
            1.0,
            1000.0,
            1000.0 * 1000.0,
            1000.0 * 1000.0 * 1000.0,
    };

    private static final String[] MS_PREFIX_NAMES = {
            "ns", "us", "ms", "sec"
    };

    private long m_counter;
    private long m_start;

    private long m_total;
    private long m_best = Long.MAX_VALUE;
    private long m_worst = Long.MIN_VALUE;

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    public Time(final Class<?> p_class, final String p_name) {
        super(p_class, p_name);
    }

    /**
     * Start time measurement
     */
    public void start() {
        m_counter++;
        m_start = System.nanoTime();
    }

    /**
     * Stop time measurement
     *
     * @return Meausred delta time in ns
     */
    public long stop() {
        if (m_start == 0) {
            return 0;
        }

        long delta = System.nanoTime() - m_start;
        m_start = 0;

        m_total += delta;

        if (delta < m_best) {
            m_best = delta;
        }

        if (delta > m_worst) {
            m_worst = delta;
        }

        return delta;
    }

    /**
     * "Debug version". Identical to normal call but is removed on non-debug builds.
     */
    public void startDebug() {
        start();
    }

    /**
     * "Debug version". Identical to normal call but is removed on non-debug builds.
     */
    public long stopDebug() {
        return stop();
    }

    /**
     * Get the (call) counter value
     */
    public long getCounter() {
        return m_counter;
    }

    /**
     * Get the total time measured so far
     *
     * @return Total time in ns
     */
    public long getTotalTime() {
        return m_total;
    }

    /**
     * Get the total time measured so far
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Total time measured scaled to specified prefix
     */
    public double getTotalTime(final Prefix p_prefix) {
        return m_total / MS_PREFIX_TABLE[p_prefix.ordinal()];
    }

    /**
     * Get the average time of all measured times
     *
     * @return Average time in ns
     */
    public double getAvgTime() {
        return (double) m_total / m_counter;
    }

    /**
     * Get the average time of all measured times
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Average time scaled to specified prefix
     */
    public double getAvgTime(final Prefix p_prefix) {
        if (m_counter == 0) {
            return 0;
        } else {
            return m_total / MS_PREFIX_TABLE[p_prefix.ordinal()] / m_counter;
        }
    }

    /**
     * Get the best time measured so far
     *
     * @return Best time in ns
     */
    public double getBestTime() {
        return m_best;
    }

    /**
     * Get the best time measured so far
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Best time scaled to specified prefix
     */
    public double getBestTime(final Prefix p_prefix) {
        return m_best / MS_PREFIX_TABLE[p_prefix.ordinal()];
    }

    /**
     * Get the worst time measured so far
     *
     * @return Worst time in ns
     */
    public double getWorstTime() {
        return m_worst;
    }

    /**
     * Get the worst time measured so far
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Worst time scaled to specified prefix
     */
    public double getWorstTime(final Prefix p_prefix) {
        return m_worst / MS_PREFIX_TABLE[p_prefix.ordinal()];
    }

    @Override
    public String dataToString(final String p_indent, final boolean p_extended) {
        return p_indent + "counter " + m_counter + ";total " + formatTime(m_total) + ";avg " + formatTime(
                getAvgTime(Prefix.NANO)) + ";best " + formatTime(m_best) + ";worst " + formatTime(m_worst);
    }

    @Override
    public String generateCSVHeader(final char p_delim) {
        return "name" + p_delim + "counter" + p_delim + "total" + p_delim + "avg" + p_delim + "best" + p_delim +
                "worst";
    }

    @Override
    public String toCSV(final char p_delim) {
        return getOperationName() + p_delim + m_counter + p_delim + m_total + p_delim + getAvgTime(Prefix.NANO) +
                p_delim + m_best + p_delim + m_worst;
    }

    /**
     * Auto format the time
     *
     * @param p_timeNs
     *         Time in ns to format
     * @return String with auto formated time
     */
    static String formatTime(final double p_timeNs) {
        for (int i = Prefix.MICRO.ordinal(); i <= Prefix.SEC.ordinal(); i++) {
            if (p_timeNs < MS_PREFIX_TABLE[i]) {
                return String.format("%.3f %s", p_timeNs / MS_PREFIX_TABLE[i - 1], MS_PREFIX_NAMES[i - 1]);
            }
        }

        return String.format("%.3f %s", p_timeNs / MS_PREFIX_TABLE[Prefix.SEC.ordinal()],
                MS_PREFIX_NAMES[Prefix.SEC.ordinal()]);
    }
}
