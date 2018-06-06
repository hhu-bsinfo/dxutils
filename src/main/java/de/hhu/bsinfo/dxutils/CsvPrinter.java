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

package de.hhu.bsinfo.dxutils;

/**
 * Interface for generating CSV data strings from objects
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 20.02.2018
 */
public interface CsvPrinter {
    /**
     * Create the header which describes the data
     *
     * @param p_delim
     *         The delimiter to use to separate values
     * @return CSV header line which describes the data
     */
    String generateCSVHeader(final char p_delim);

    /**
     * Print the object/data as a single CSV line
     *
     * @param p_delim
     *         The delimiter to use to separate values
     * @return CSV line which contains the data of the object
     */
    String toCSV(final char p_delim);
}
