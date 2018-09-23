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
 * Implementation for access to raw device for logging.
 *
 * @author Christian Gesse <christian.gesse@hhu.de> 27.09.16
 */
public final class JNIFileRaw {

    /*
     * Steps to prepare a raw device:
     * 1) Use an empty partition
     * 2) If executed in nspawn container: add "--capability=CAP_SYS_MODULE --bind-ro=/lib/modules" to systemd-nspawn
     *      command in boot script
     * 3) Get root access
     * 4) mkdir /dev/raw
     * 5) cd /dev/raw/
     * 6) mknod raw1 c 162 1
     * 7) modprobe raw
     * 8) If /dev/raw/rawctl was not created: mknod /dev/raw/rawctl c 162 0
     * 9) raw /dev/raw/raw1 /dev/*empty partition*
     * 10) Execute DXRAM as root user (sudo -P for nfs)
     */

    /**
     * Constructor
     */
    private JNIFileRaw() {

    }

    /**
     * Prepares the raw device for use as logging device.
     *
     * @param p_devicePath
     *         Path to raw device file (e.g., "/dev/raw/raw1")
     * @param p_mode
     *         0 -> overwrite existing data, 1 -> check for old data and do not overwrite
     * @return file descriptor of raw device or -1 on error
     */
    public static native int prepareRawDevice(final String p_devicePath, final int p_mode);

    /**
     * Opens an existing log file or creates a new one.
     *
     * @param p_logName
     *         the filename (and only the name without path!) of the logfile
     * @param p_logSize
     *         the size of the log (in case of 0 it is an dynamically growing log, use  8MB then for a start)
     * @return index of log file as descriptor and -1 if an error occurred
     */
    public static native int open(final String p_logName, final long p_logSize);

    /**
     * Closes an opened log file.
     *
     * @param p_logID
     *         the ID of start index
     * @return 0 on success
     */
    public static native int close(final int p_logID);

    /**
     * Deletes a log from index if it is not open.
     *
     * @param p_fileID
     *         the file descriptor
     * @return 0 on success, -1 on error (log open or writing back index filed)
     */
    public static native int delete(final int p_fileID);

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
    public static native int write(final int p_fileID, final long p_data, final int p_offset, final int p_length,
            final long p_position, final byte p_retainEnd, final byte p_setFileLength);

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
    public static native int read(final int p_fileID, final long p_data, final int p_offset, final int p_length,
            final long p_position);

    /**
     * Returns the length of the file.
     *
     * @param p_fileID
     *         file descriptor
     * @return the length of the file
     */
    public static native long length(final int p_fileID);

    /**
     * Returns a String with all names of the logs, separated by '\n'.
     * This is needed because raw device cannot be scanned for files with File().
     *
     * @return String with all log names separated by '\n'
     */
    public static native String getFileList();

    /**
     * Prints all logs in index together with their index number, size and name. Only for test use.
     */
    public static native void printIndexForTest();

}
