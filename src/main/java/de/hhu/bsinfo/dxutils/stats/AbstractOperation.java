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

import de.hhu.bsinfo.dxutils.CsvPrinter;

/**
 * Base class for all statistics operations
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public abstract class AbstractOperation implements CsvPrinter {
    final Class<?> m_class;
    final String m_name;

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    AbstractOperation(final Class<?> p_class, final String p_name) {
        m_class = p_class;
        m_name = p_name;
    }

    /**
     * Get the name of the operation
     */
    public String getOperationName() {
        return m_class.getSimpleName() + '-' + m_name;
    }

    /**
     * Get the simple name (without class name prefix) of the operation
     */
    public String getOperationNameSimple() {
        return m_name;
    }

    /**
     * Print the data to a string ("toString")
     *
     * @param p_indent
     *         Indent to add as "prefix"
     * @param p_extended
     *         Set to true to tell the manager to print statistics that also
     *         require some processing/pre-calculation (e.g. percentile). The dedicated
     *         statistics thread will set this to false to avoid executing these expensive
     *         calculations during runtime.
     * @return Data as string
     */
    public abstract String dataToString(final String p_indent, final boolean p_extended);
}
