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
 * Ratio of two statistic values
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public class Ratio extends AbstractOperation {
    private Value m_value1;
    private Value m_value2;

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    public Ratio(final Class<?> p_class, final String p_name) {
        super(p_class, p_name);

        m_value1 = new Value(p_class, p_name);
        m_value2 = new Value(p_class, p_name);
    }

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     * @param p_value1
     *         Reference to an existing value to use as numerator
     * @param p_value2
     *         Reference to an existing value to use as denominator
     */
    public Ratio(final Class<?> p_class, final String p_name, final Value p_value1, final Value p_value2) {
        super(p_class, p_name);

        m_value1 = p_value1;
        m_value2 = p_value2;
    }

    /**
     * Get the ratio of the counters
     */
    public double getRatioCounter() {
        double tmp = m_value2.getCounter();
        return m_value1.getCounter() / (tmp == 0.0 ? 1.0 : tmp);
    }

    /**
     * Get the ratio of the total values
     */
    public double getRatioTotalValue() {
        double tmp = m_value2.getTotalValue();
        return m_value1.getTotalValue() / (tmp == 0.0 ? 1.0 : tmp);
    }

    /**
     * Get the ratio of the min values
     */
    public double getRatioMinValue() {
        double tmp = m_value2.getMinValue();
        return m_value1.getMinValue() / (tmp == 0.0 ? 1.0 : tmp);
    }

    /**
     * Get the ratio of the max values
     */
    public double getRatioMaxValue() {
        double tmp = m_value2.getMaxValue();
        return m_value1.getMaxValue() / (tmp == 0.0 ? 1.0 : tmp);
    }

    @Override
    public String dataToString(final String p_indent, final boolean p_extended) {
        return p_indent + "counter_ratio " + getRatioCounter() + ";total_ratio " + getRatioTotalValue() +
                ";min_ratio " + getRatioMinValue() + ";max_ratio " + getRatioMaxValue();
    }

    @Override
    public String generateCSVHeader(final char p_delim) {
        return "name" + p_delim + "counter_ratio" + p_delim + "total_ratio" + p_delim + "min_ratio" + p_delim +
                "max_ratio";
    }

    @Override
    public String toCSV(final char p_delim) {
        return getOperationName() + p_delim + getRatioCounter() + p_delim + getRatioTotalValue() + p_delim +
                getRatioMinValue() + p_delim + getRatioMaxValue();
    }
}
