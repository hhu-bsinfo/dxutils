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
 * Statistics operation for measuring throughput (Value / Time)
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public class Throughput extends AbstractOperation {
    private static final String[] MS_PREFIX_NAMES = {
            // Second eb/s is for MAX prefix of value
            "u/s", "ku/s", "mu/s", "gu/s", "tu/s", "pu/s", "eu/s", "eu/s"
    };

    private Value m_value;
    private Time m_time;

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     * @param p_base
     *         Base for the value
     */
    public Throughput(final Class<?> p_class, final String p_name, final Value.Base p_base) {
        super(p_class, p_name);

        m_value = new Value(p_class, p_name, p_base);
        m_time = new Time(p_class, p_name);
    }

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     * @param p_value
     *         Reference to an existing value to use as numerator
     * @param p_time
     *         Reference to an existing time to use as denominator
     */
    public Throughput(final Class<?> p_class, final String p_name, final Value p_value, final Time p_time) {
        super(p_class, p_name);

        m_value = p_value;
        m_time = p_time;
    }

    /**
     * Start throughput measurement. Ensure a call to stop() is following
     */
    public void start() {
        m_time.start();
    }

    /**
     * Start throughput measurement. Ensure a call to stop() is following
     *
     * @param p_val
     *         Value to add (e.g. data processed)
     */
    public void start(final long p_val) {
        m_time.start();
        m_value.add(p_val);
    }

    /**
     * Add a value, e.g. data processed
     *
     * @param p_val
     *         Value to add
     */
    public void add(final long p_val) {
        m_value.add(p_val);
    }

    /**
     * Stop throughput measurement. Ensure a call to start is preceding
     */
    public void stop() {
        m_time.stop();
    }

    /**
     * Stop throughput measurement. Ensure a call to start is preceding
     *
     * @param p_val
     *         Value to add (e.g. data processed)
     */
    public void stop(final long p_val) {
        m_time.stop();
        m_value.add(p_val);
    }

    /**
     * Get the current throughput in units/sec
     */
    public double getThroughput() {
        return getThroughput(Value.Prefix.NONE);
    }

    /**
     * Get the current throughput in prefixed defined scale / sec
     *
     * @param p_valuePrefix
     *         Prefix to use
     */
    public double getThroughput(final Value.Prefix p_valuePrefix) {
        return getThroughput(p_valuePrefix, Time.Prefix.SEC);
    }

    /**
     * Get the current throughput in prefix defined scale for value / prefixed defined scale for time
     *
     * @param p_valuePrefix
     *         Prefix for value to use
     * @param p_timePrefix
     *         Prefix for time to use
     */
    public double getThroughput(final Value.Prefix p_valuePrefix, final Time.Prefix p_timePrefix) {
        return m_value.getTotalValue(p_valuePrefix) / m_time.getTotalTime(p_timePrefix);
    }

    @Override
    public String dataToString(final String p_indent, final boolean p_extended) {
        return p_indent + formatValue() + ";value counter " + m_value.getCounter() + ";total " + m_value.formatValue(
                m_value.getTotalValue()) + ";avg " + m_value.formatValue(m_value.getAvgValue()) +
                ";min " + m_value.formatValue(m_value.getMinValue()) + ";max " + m_value.formatValue(
                m_value.getMaxValue()) + "time counter " + m_time.getCounter() + ";total " + m_time.formatTime(
                m_time.getTotalTime()) + ";avg " + m_time.formatTime(m_time.getAvgTime()) + ";best " +
                m_time.formatTime(m_time.getBestTime()) + ";worst " + m_time.formatTime(m_time.getWorstTime());
    }

    @Override
    public String generateCSVHeader(final char p_delim) {
        return "name" + p_delim + "throughput u/sec" + p_delim + "value counter" + p_delim + "total" + p_delim + "avg" +
                p_delim + "min" + p_delim + "max" + p_delim + "time counter" + p_delim + "total" + p_delim + "avg" +
                p_delim + "best" + p_delim + "worst";
    }

    @Override
    public String toCSV(final char p_delim) {
        return getOperationName() + p_delim + getThroughput() + p_delim + m_value.getCounter() + p_delim +
                m_value.getTotalValue() + p_delim + m_value.getAvgValue() + p_delim + m_value.getMinValue() + p_delim +
                m_value.getMaxValue() + p_delim + m_time.getCounter() + p_delim + m_time.formatTime(
                m_time.getTotalTime()) + p_delim + m_time.formatTime(m_time.getAvgTime()) + p_delim +
                m_time.formatTime(m_time.getBestTime()) + p_delim + m_time.formatTime(m_time.getWorstTime());
    }

    private String formatValue() {
        for (int i = 1; i < Value.Prefix.COUNT.ordinal(); i++) {
            if (m_value.getTotalValue() < Value.MS_PREFIX_TABLE[m_value.getBase().ordinal()][i]) {
                return String.format("%.3f %s",
                        m_value.getTotalValue(Value.Prefix.values()[i]) / m_time.getTotalTime(Time.Prefix.SEC),
                        MS_PREFIX_NAMES[i - 1]);
            }
        }

        return m_value.getTotalValue() + " " + MS_PREFIX_NAMES[0];
    }
}
