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
 * Implementation for access to  raw-device for logging
 *
 * @author Christian Gesse <christian.gesse@hhu.de> 27.09.16
 */
public final class JNIFileRaw {

    /**
     * Constructor
     */
    private JNIFileRaw() {

    }

    /**
     * returns a String with all names of the logs, seperated by \n
     * this is needed because raw device cannot be scanned for files with File()
     *
     * @return String with all lognames seperated by \n
     */
    public static native String getFileList();

    /**
     * prepares the rawdevice for use as logging-device.
     *
     * @param p_devicePath
     *         Path to rawdevice-file(e.g. /dev/raw/raw1)
     * @param p_mode
     *         0 -> overwrite exisiting data, 1 -> check for old data and do not overwrite
     * @return filedescriptor of rawdevice or -1 on error
     */
    public static native int prepareRawDevice(final String p_devicePath, final int p_mode);

    /**
     * create a new log and create index entry
     *
     * @param p_logName
     *         the filename (and only the name without path!) of the logfile
     * @param p_logSize
     *         the size of the log (in case of 0 it is an dynamically growing log, use  8MB then for a start)
     * @return indexnumber of the first block of the logfile -> used as filedescriptor
     */
    public static native int createLog(final String p_logName, final long p_logSize);

    /**
     * open an existing logfile
     *
     * @param p_logName
     *         the filename (and only the name wothout path!) of the logfile
     * @return index of logfile as descriptor and -1 if log was not found or error occured
     */
    public static native int openLog(final String p_logName);

    /**
     * closes an opened logfile
     *
     * @param p_logID
     *         the ID of start index
     * @return 0 on success
     */
    public static native int closeLog(final int p_logID);

    /**
     * creates a new aligned buffer in native memory
     *
     * @param p_bufSize:
     *         length of buffer in bytes, must be a multiple of BLOCKSIZE
     * @return address (pointer) of the created buffer or NULL
     */
    public static native long createBuffer(final int p_bufSize);

    /**
     * frees a created native buffer
     *
     * @param p_bufPtr
     *         address of the buffer, interpreted as a pointer
     */
    public static native void freeBuffer(final long p_bufPtr);

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
     * returns the allocated length of the file - warning: do not use as .length to achieve
     * data pointer into dynamically growing files!
     *
     * @param p_fileID
     *         file descriptor
     * @return the allocated length of file described as above
     */
    public static native long length(final int p_fileID);

    /**
     * returns the last write position in a dynamically growing file -
     * use to determine write position in Versionlogs
     *
     * @param p_fileID
     *         file descriptor
     * @return last write position (length) of file
     */
    public static native long dlength(final int p_fileID);

    /**
     * sets the write position (length) of a dynamically growing file - use in VersionLogs only
     *
     * @param p_fileID
     *         the file descriptor
     * @param p_fileLength
     *         new length of file
     * @return 0 on success, -1 on error
     */
    public static native int setDFileLength(final int p_fileID, final long p_fileLength);

    /**
     * deletes a log from index if it is not open
     *
     * @param p_fileID
     *         the file descriptor
     * @return 0 on success, -1 on error (log open or writing back index filed)
     */
    public static native int deleteLog(final int p_fileID);

    /**
     * prints all logs in index together with their indexnumber, size and name. Only for test use.
     */
    public static native void printIndexForTest();

}
