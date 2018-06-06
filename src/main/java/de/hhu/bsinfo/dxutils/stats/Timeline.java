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

public class Timeline extends AbstractOperation {
    private final Time[] m_times;
    private int m_pos;

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     * @param p_sectionNames
     *         Names for the timed sections (determines the total number of sections)
     */
    public Timeline(final Class<?> p_class, final String p_name, final String... p_sectionNames) {
        super(p_class, p_name);

        m_times = new Time[p_sectionNames.length];

        for (int i = 0; i < m_times.length; i++) {
            m_times[i] = new Time(p_class, p_sectionNames[i]);
        }
    }

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     * @param p_times
     *         References to Time objects to use for the sections
     */
    public Timeline(final Class<?> p_class, final String p_name, final Time... p_times) {
        super(p_class, p_name);

        m_times = p_times;
    }

    public void start() {
        m_pos = 0;
        m_times[m_pos].start();
    }

    public void nextSection() {
        m_times[m_pos].stop();
        m_pos++;

        if (m_pos >= m_times.length) {
            throw new IndexOutOfBoundsException("Invalid section index " + m_pos);
        }

        m_times[m_pos].start();
    }

    public void stop() {
        if (m_pos == 0) {
            return;
        }

        m_times[m_pos].stop();
    }

    @Override
    public String dataToString(final String p_indent, final boolean p_extended) {
        StringBuilder builder = new StringBuilder();

        long totalTime = 0;

        for (int i = 0; i < m_times.length; i++) {
            totalTime += m_times[i].getTotalTime();
        }

        for (int i = 0; i < m_times.length; i++) {
            builder.append(p_indent);
            builder.append('(');
            builder.append(i);
            builder.append(") ");
            builder.append(m_times[i].getOperationNameSimple());
            builder.append(": dist ");
            builder.append(String.format("%.2f", (double) m_times[i].getTotalTime() / totalTime * 100));
            builder.append(" %;");
            builder.append(m_times[i].dataToString("", p_extended));

            if (i + 1 < m_times.length) {
                builder.append('\n');
            }
        }

        return builder.toString();
    }

    @Override
    public String generateCSVHeader(final char p_delim) {
        return "section" + p_delim + "dist" + p_delim + new Time(m_class, m_name).generateCSVHeader(p_delim);
    }

    @Override
    public String toCSV(final char p_delim) {
        StringBuilder builder = new StringBuilder();

        long totalTime = 0;

        for (int i = 0; i < m_times.length; i++) {
            totalTime += m_times[i].getTotalTime();
        }

        for (int i = 0; i < m_times.length; i++) {
            builder.append(m_times[i].getOperationNameSimple());
            builder.append(p_delim);
            builder.append(String.format("%.2f", (double) m_times[i].getTotalTime() / totalTime * 100));
            builder.append(p_delim);
            builder.append(m_times[i].toCSV(p_delim));

            if (i + 1 < m_times.length) {
                builder.append('\n');
            }
        }

        return builder.toString();
    }
}
