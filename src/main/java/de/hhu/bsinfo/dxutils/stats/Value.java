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
 * Statistics operation to count, sum up data, track a value etc
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public class Value extends AbstractOperation {
    public enum Base {
        B_2, B_10,
    }

    public enum Prefix {
        NONE, KILO, MEGA, GIGA, TERA, PETA, EXA, MAX, COUNT
    }

    private static final long[] MS_BASE_TABLE = {1024, 1000};

    private static final String[] MS_PREFIX_NAMES = {
            "units", "ku", "mu", "gu", "tu", "pu", "eu", "eu"
    };

    public static final long[][] MS_PREFIX_TABLE = {
            {
                    (long) Math.pow(MS_BASE_TABLE[Base.B_2.ordinal()], Prefix.NONE.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_2.ordinal()], Prefix.KILO.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_2.ordinal()], Prefix.MEGA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_2.ordinal()], Prefix.GIGA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_2.ordinal()], Prefix.TERA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_2.ordinal()], Prefix.PETA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_2.ordinal()], Prefix.EXA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_2.ordinal()], Prefix.EXA.ordinal())
            },
            {
                    (long) Math.pow(MS_BASE_TABLE[Base.B_10.ordinal()], Prefix.NONE.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_10.ordinal()], Prefix.KILO.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_10.ordinal()], Prefix.MEGA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_10.ordinal()], Prefix.GIGA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_10.ordinal()], Prefix.TERA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_10.ordinal()], Prefix.PETA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_10.ordinal()], Prefix.EXA.ordinal()),
                    (long) Math.pow(MS_BASE_TABLE[Base.B_10.ordinal()], Prefix.EXA.ordinal())
            },
    };

    private final Base m_base;

    private long m_counter;
    private long m_total;

    private long m_min = Long.MAX_VALUE;
    private long m_max = Long.MIN_VALUE;

    /**
     * Constructor
     * Defaults to Base 10.
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    public Value(final Class<?> p_class, final String p_name) {
        this(p_class, p_name, Base.B_10);
    }

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     * @param p_base
     *         Base to use for any scaling using prefixes
     */
    public Value(final Class<?> p_class, final String p_name, final Base p_base) {
        super(p_class, p_name);

        m_base = p_base;
    }

    /**
     * Get the base set for this value
     */
    public Base getBase() {
        return m_base;
    }

    /**
     * Get the counter
     *
     * @return Counter value
     */
    public long getCounter() {
        return m_counter;
    }

    /**
     * Get the counter
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Counter value scaled to specified prefix
     */
    public double getCounter(final Prefix p_prefix) {
        return m_counter / (double) MS_PREFIX_TABLE[m_base.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the total value
     *
     * @return Total value
     */
    public long getTotalValue() {
        return m_total;
    }

    /**
     * Get the total value
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Total value scaled to specified prefix
     */
    public double getTotalValue(final Prefix p_prefix) {
        return m_total / (double) MS_PREFIX_TABLE[m_base.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the average value of all applied values
     *
     * @return Average value
     */
    public double getAvgValue() {
        return m_counter == 0 ? 0.0 : (double) m_total / m_counter;
    }

    /**
     * Get the average value of all applied values
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Average value scaled to specified prefix
     */
    public double getAvgValue(final Prefix p_prefix) {
        return getAvgValue() / (double) MS_PREFIX_TABLE[m_base.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the min value of all applied values
     *
     * @return Min value
     */
    public long getMinValue() {
        return m_min;
    }

    /**
     * Get the min value of all applied values
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Min value scaled to specified prefix
     */
    public double getMinValue(final Prefix p_prefix) {
        return m_min / (double) MS_PREFIX_TABLE[m_base.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the max value of all applied values
     *
     * @return Max value
     */
    public long getMaxValue() {
        return m_max;
    }

    /**
     * Get the max value of all applied values
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Max value scaled to specified prefix
     */
    public double getMaxValue(final Prefix p_prefix) {
        return m_max / (double) MS_PREFIX_TABLE[m_base.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Increment the total value by 1
     */
    public void inc() {
        add(1);
    }

    /**
     * Add a value to the total value
     *
     * @param p_val
     *         Value to add to the total value
     */
    public void add(final long p_val) {
        m_counter++;
        m_total += p_val;

        if (m_min > p_val) {
            m_min = p_val;
        }

        if (m_max < p_val) {
            m_max = p_val;
        }
    }

    @Override
    public String dataToString(final String p_indent, final boolean p_extended) {
        return p_indent + "counter " + m_counter + ";total " + formatValue(m_total) + ";avg " + formatValue(
                getAvgValue()) + ";min " + formatValue(m_min) + ";max " + formatValue(m_max);
    }

    @Override
    public String generateCSVHeader(final char p_delim) {
        return "name" + p_delim + "counter" + p_delim + "total" + p_delim + "avg" + p_delim + "min" + p_delim + "max";
    }

    @Override
    public String toCSV(final char p_delim) {
        return getOperationName() + p_delim + m_counter + p_delim + m_total + p_delim + (long) getAvgValue() + p_delim +
                m_min + p_delim + m_max;
    }

    /**
     * Format the specified value
     *
     * @param p_value
     *         Value to format
     * @return String with formated value
     */
    String formatValue(final double p_value) {
        for (int i = 1; i < Prefix.COUNT.ordinal(); i++) {
            if (p_value < MS_PREFIX_TABLE[m_base.ordinal()][i]) {
                return String.format("%.3f %s", p_value / MS_PREFIX_TABLE[m_base.ordinal()][i - 1],
                        MS_PREFIX_NAMES[i - 1]);
            }
        }

        return p_value + " " + MS_PREFIX_NAMES[0];
    }
}
