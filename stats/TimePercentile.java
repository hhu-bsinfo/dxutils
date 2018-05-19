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
 * Statistics operation to measure time which stores the full time history
 * to determine percentiles
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 08.03.2018
 */
public class TimePercentile extends AbstractOperation {
    private ValuePercentile m_percentile;
    private Value m_value;

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    public TimePercentile(final Class<?> p_class, final String p_name) {
        super(p_class, p_name);

        m_percentile = new ValuePercentile(p_class, p_name);
        m_value = new Value(p_class, p_name);
    }

    /**
     * Get the counter
     *
     * @return Counter value
     */
    public long getCounter() {
        return m_value.getCounter();
    }

    /**
     * Get the average value
     *
     * @return Average value
     */
    public long getAvg() {
        return (long) m_value.getAvgValue();
    }

    /**
     * Get the average value
     *
     * @param p_prefix
     *         Prefix to apply
     */
    public double getAvg(final Time.Prefix p_prefix) {
        return m_value.getAvgValue() / Time.MS_PREFIX_TABLE[p_prefix.ordinal()];
    }

    /**
     * Get the min value
     *
     * @return Min value
     */
    public long getMin() {
        return m_value.getMinValue();
    }

    /**
     * Get the min value
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Min value scaled to specified prefix
     */
    public double getMin(final Time.Prefix p_prefix) {
        return m_value.getMinValue() / Time.MS_PREFIX_TABLE[p_prefix.ordinal()];
    }

    /**
     * Get the max value
     *
     * @return Max value
     */
    public long getMax() {
        return m_value.getMaxValue();
    }

    /**
     * Get the max value
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Max value scaled to specified prefix
     */
    public double getMax(final Time.Prefix p_prefix) {
        return m_value.getMaxValue() / Time.MS_PREFIX_TABLE[p_prefix.ordinal()];
    }

    /**
     * Sort all registered values (ascending). Call this before getting any percentile values
     * to update the internal state.
     */
    public void sortValues() {
        m_percentile.sortValues();
    }

    /**
     * Get the score for the Xth percentile of all recorded values
     *
     * @param p_percentile
     *         the percentile
     * @return Score of specified percentile
     */
    public long getPercentileScore(final float p_percentile) {
        return m_percentile.getPercentileScore(p_percentile);
    }

    /**
     * Get the score for the Xth percentile of all recorded values
     *
     * @param p_percentile
     *         the percentile
     * @param p_prefix
     *         Prefix to apply to score
     * @return Score of specified percentile scaled to specified prefix
     */
    public double getPercentileScore(final float p_percentile, final Time.Prefix p_prefix) {
        return getPercentileScore(p_percentile) / Time.MS_PREFIX_TABLE[p_prefix.ordinal()];
    }

    /**
     * Record a single value
     *
     * @param p_valueNs
     *         Time value in ns to record (separate from start/stop)
     */
    public void record(final long p_valueNs) {
        m_percentile.record(p_valueNs);
        m_value.add(p_valueNs);
    }

    @Override
    public String dataToString(final String p_indent, final boolean p_extended) {
        if (p_extended) {
            sortValues();

            return m_value.dataToString(p_indent, p_extended) + ";95th percentile " +
                    Time.formatTime(getPercentileScore(0.95f)) + ";99th percentile " +
                    Time.formatTime(getPercentileScore(0.99f)) + ";99.9th percentile " +
                    Time.formatTime(getPercentileScore(0.999f));
        } else {
            // don't print percentile for debug output because sorting might take too long if there are too many values
            return m_value.dataToString(p_indent, p_extended);
        }
    }

    @Override
    public String generateCSVHeader(final char p_delim) {
        return m_value.generateCSVHeader(p_delim) + p_delim + "95th percentile" + p_delim + "99th percentile" +
                p_delim + "99.9th percentile";
    }

    @Override
    public String toCSV(final char p_delim) {
        sortValues();
        return m_value.toCSV(p_delim) + p_delim + getPercentileScore(0.95f) + p_delim + getPercentileScore(0.99f) +
                p_delim + getPercentileScore(0.999f);
    }
}
