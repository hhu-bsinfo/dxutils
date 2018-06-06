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

package de.hhu.bsinfo.dxutils.jni;

/**
 * Implementation of JNI-Low-Level-Access to files with O_DIRECT Flag
 *
 * @author Christian Gesse <christian.gesse@hhu.de> 17.09.16
 */
public final class JNIFileDirect {

    /**
     * Constructor
     */
    private JNIFileDirect() {

    }

    /**
     * Opens a new logfile or creates it if it doesnt exist
     *
     * @param p_path
     *         complete path to log including the filename
     * @param p_mode
     *         0 -> read/write, 1 -> read only
     * @param p_size
     *         preallocate disk space if != 0
     * @return the filedescriptor or negative value on error
     */
    public static native int open(final String p_path, final int p_mode, final long p_size);

    /**
     * Closes an open logfile
     *
     * @param p_fileID
     *         the file descriptor of the logfile
     * @return 0 on success, -1 on error
     */
    public static native int close(final int p_fileID);

    /**
     * Appends the page-aligned buffer to the position in file. Buffer size must be a multiple integer of page size.
     *
     * @param p_fileID
     *         the file descriptor
     * @param p_data
     *         reference to the byte buffer containing the data
     * @param p_offset
     *         start-offset in data-buffer (must be page-aligned)
     * @param p_length
     *         number of bytes to write
     * @param p_position
     *         write-position in file or -1 for append
     * @param p_retainEnd
     *         whether overlapping bytes at the end can be overwritten (0) or not (1)
     * @param p_setFileLength
     *         whether the file length must be set after writing (0) or not (1)
     * @return 0 on success or -1 on error
     */
    public static native int write(final int p_fileID, final long p_data, final int p_offset, final int p_length, final long p_position, final byte p_retainEnd,
            final byte p_setFileLength);

    /**
     * Reads from logfile into data-buffer at current position
     *
     * @param p_fileID
     *         the file descriptor
     * @param p_data
     *         reference to the page-aligned byte buffer that should receive the data
     * @param p_offset
     *         start-offset in data-buffer
     * @param p_length
     *         number of bytes to read
     * @param p_position
     *         read-position in file
     * @return 0 on success or -1 on error
     */
    public static native int read(final int p_fileID, final long p_data, final int p_offset, final int p_length, final long p_position);

    /**
     * Sets the filepointer to given value, measured from beginning of the file
     *
     * @param p_fileID:
     *         the file descriptor
     * @param p_position:
     *         new position in file
     * @return new position in file or -1 on errror
     */
    public static native long seek(final int p_fileID, final long p_position);

    /**
     * Return length of the file
     *
     * @param p_fileID
     *         the descriptor of the fileID
     * @return the length of the file
     */
    public static native long length(final int p_fileID);

}
