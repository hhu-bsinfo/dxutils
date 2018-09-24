/*
 * Copyright (C) 2018 Heinrich-Heine-Universitaet Duesseldorf, Institute of Computer Science, Department Operating
 * Systems
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

// Compile: gcc -O2 -m64 -o ../libJNIFileRaw.so -fpic -shared -I/usr/lib/jvm/java-8-oracle/include/
// -I/usr/lib/jvm/java-8-oracle/include/linux ./JNIFileRaw.c

/* This is an implementation of raw device access bypassing the page cache of linux.
 * All writes and reads go to an unformatted partition of the SSD, mounted as a raw device.
 * Some of the Macros, structures and definitions used here come from JNIFileRawStructures.h
 * Keep in mind that all addresses have to be aligned to a given BLOCKSIZE (here  4096 Byte
 * for SSD) and that the length of data has to be a multiple of this BLOCKSIZE.
 *
 * Created for DXRAM by Christian Gesse, 2016
 * Edited by Kevin Beineke, 2018
 */

#define _GNU_SOURCE
#include <jni.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <linux/fs.h>
#include <assert.h>
#include <pthread.h>

#include "JNIFileRawStructures.h"


#define BLOCKSIZE 4096

/* For low-level stuff in index use uintN_t instead of int and long.
   Since java has no simple implementation of unsigned, all parameter from jni are
   normal int and long */

// device number after opening raw-device
int device = -1;
// length of raw device
uint64_t dev_size = 0;
// pointer to position where next log has to be appended
uint64_t append = 0;
// pointer to first index_entry
uint64_t index_start = 0;
// pointer to start of data
uint64_t data_start = 0;
// next free index to use
uint32_t next_free_index = 0;
// index entries per BLOCKSIZE
uint32_t entries_per_block = 0;
// number of available index entries
uint32_t index_length = 0;
// recognition string for logging device_header
char header_begin[] = {"DXRAMdevice"};
// a lock used to synchronize all changes to the index
pthread_mutex_t lock;


// pointer to index in memory
index_entry_t *ind = NULL;

/*
 * Function to write an index-entry back to disk. Since we are performing
 * direct disk access, the whole block has to bew written.
 * Index has to be locked before function call!
 *
 * index_number: number of entry that should be written back
 * return: 0 on success, -1 on error
 */
int writeBackIndex(uint32_t index_number) {
    // write changed block of index back to disk - first calculate start of the block
    uint64_t start_write = index_start + (index_number - (index_number % entries_per_block)) * sizeof(index_entry_t);
    if (pwrite(device, (void*) &ind[index_number - (index_number % entries_per_block)], BLOCKSIZE, start_write) == -1){
        // if writing back failed delete entry in index to avoid inconsistency
        ind[index_number].status = 0x00;

        return -1;
    }
    return 0;
}

/*
 * Checks if the given index position AND the enough space after append are available
 * Needed if new index_entries should be created
 * Lock on index has to be acquired before calling the function!
 * pos:     index-position to check
 * length:  space needed behind append
 * return: 0 if ok, < 0 if not
 */
int checkNextPosAndSpace(uint32_t pos, uint32_t length) {
    // check if index position is available
    if (pos >= index_length){
        printf("Index of rawdevice is full - cannot create new entry\n");
        fflush(stdout);
        return -2;
    }
    // check if device has enough free space
    if (append + length  >= dev_size){
        printf("Rawdevice is full - cannot create new entry\n");
        fflush(stdout);
        return -1;
    }
    // everything ok
    return 0;
}

/*
 * Gets the length of file.
 */
long get_length(int fileID) {

    /* Pre-allocated size
    long ret = ind[fileID].part_length;
    // scan for next block
    uint32_t cur_ind = fileID;
    // Scan all blocks of the file and sum up the preallocated blocksizes
    while (ind[cur_ind].nextBlock != cur_ind){
      cur_ind = ind[cur_ind].nextBlock;
      ret += ind[cur_ind].part_length;
    }
    return ret;*/

    return ind[fileID].cur_length;
}

/*
 * Class:   de_hhu_bsinfo_dxutils_jni_JNIFileRaw
 * Method:  prepareRawDevice
 *          prepares the raw device for use as logging-device.
 * jpath:   Path to raw device-file(e.g. /dev/raw/raw1)
 * mode:    0 -> overwrite existing data, 1 -> check for old data and do not overwrite
 * return:  file descriptor of raw device or -1 on error
 */
JNIEXPORT jint JNICALL Java_de_hhu_bsinfo_dxutils_jni_JNIFileRaw_prepareRawDevice(JNIEnv *env, jclass clazz,
        jstring jpath, jint mode) {

    // convert path to device
    const jbyte *path;
    path = (*env)->GetStringUTFChars(env, jpath, NULL);

    // init the mutex
    pthread_mutex_init (&lock, NULL);

    // open device - use O_SYNC to make sure that all writes and reads are synchronous,
    // so that data is persistent on disk on return
    // O_RDWR: read and write access to device
    device = open(path, O_RDWR|O_SYNC);
    if (device == -1) {
        // Error
        printf("JNI error (JNIFileRaw, open): could not open raw device (%s).", strerror(errno));
        printf(" [Path: %s]\n", path);
        return -1;
    }
    printf("Opened device %d at %s\n", device, path);

    // create buffer for header of rawdevice
    device_header_t *dev_head = NULL;
    if (posix_memalign((void**) &dev_head, BLOCKSIZE, BLOCKSIZE) != 0){
        printf("JNI error (JNIFileRaw, open): could not allocate memory (%s).", strerror(errno));
        close(device);
        return -1;
    }
    // read current header of rawdevice
    int read_bytes = pread(device, (void*) dev_head, BLOCKSIZE, 0);
    if (read_bytes != BLOCKSIZE) {
        // Error
        printf("JNI error (JNIFileRaw, open): could not read header (%s).", strerror(errno));
        printf(" [FileID: %d], [Block addr.: 0x%x], [Buffer addr.: 0x%p], [Length: %d] [Ret: %d]\n",
            device, 0, (void*) dev_head, BLOCKSIZE, read_bytes);
        return -1;
    }

    // read in existing metadata in case that there is existing data
    if (mode == 1 && (strncmp(dev_head->begin, header_begin, 11) == 0)){

        // fill in data from header_begin
        dev_size = dev_head->length;
        index_start = dev_head->index;
        index_length = dev_head->index_length;
        data_start = dev_head->data;
        entries_per_block = dev_head->entries_per_block;

        // Scan for last used index and set next_free_index, set append

        pthread_mutex_lock(&lock);

        // allocate aligned buffer to load index data
        if (posix_memalign((void**) &ind, BLOCKSIZE, index_length * sizeof(index_entry_t)) != 0){
            printf("JNI error (JNIFileRaw, open): could not allocate memory (%s).", strerror(errno));
            pthread_mutex_unlock(&lock);
            close(device);
            return -1;
        }

        // read index data from device
        read_bytes = pread(device, (void*) ind, index_length * sizeof(index_entry_t), index_start);
        if (read_bytes != index_length * sizeof(index_entry_t)) {
            // Error
            printf("JNI error (JNIFileRaw, open): could not read index data (%s).", strerror(errno));
            printf(" [FileID: %d], [Block addr.: 0x%lx], [Buffer addr.: 0x%p], [Length: %ld] [Ret: %d]\n",
                device, index_start, (void*) ind, index_length * sizeof(index_entry_t), read_bytes);
            return -1;
        }

        // now look for the first index that is not used
        uint32_t i = 0;
        for(i=0; i < index_length; i++){
            // check if the first bit of status is set
            if (((ind[i].status) & sel_bit_first) == 0x00){
                // set next free index to i because this entry is not in use
                next_free_index = i;
                // calculate new append position
                append = ind[i].begin + ind[i].part_length;
                break;
            }
        }

        pthread_mutex_unlock(&lock);

        printf("Loaded existing log-index.\n");
        printf("The size of this device is %lu MB, the index has %u entries and data begin at %lu.\n",
            dev_size/(1024*1024), index_length, data_start);
        printf("The next free index is at %u and we have %u entries per block.\n", next_free_index, entries_per_block);
        fflush(stdout);

    } else {
        // or create new device and overwrite exisiting data

        // get length of device and align it to BLOCKSIZE
        if (ioctl(device, BLKGETSIZE64, &dev_size) == -1){
            printf("JNI error (JNIFileRaw, open): could not align file (%s).", strerror(errno));
            close(device);
            return -1;
        }
        dev_size = dev_size - (dev_size % BLOCKSIZE);

        // set begin of index to 4096 (first 4096 bytes should be used for metadata)
        index_start = BLOCKSIZE;

        // get number of index entries per block
        entries_per_block = BLOCKSIZE / sizeof(index_entry_t);

        // calculate length of index and align it to a multiple of entries_per_block
        // Comments on the formula: First check, how many SecondaryLogs would fit into the partition
        // ignoring that there are also VersionLogs.
        // Then, multiply by INDEX_FACTOR in assumption that for every SecondaryLog there is need
        // of up to INDEX_FACTOR-1 VersionLog-Chunks with size VER_BLOCK_SIZE
        // This should ensure that there is enough space in the index since it has a fixed size
        // Otherwise, this ensures that the preallocated fix index size is not too big
        // If VersionLogs are growing more than expected, simply increment INDEX_FACTOR or tune VER_BLOCK_SIZE
        // the use of RAM is <2MB even for very big partitions (>2.5TBytes)
        index_length = ((dev_size / SEC_LOGSIZE)*INDEX_FACTOR);
        // alignement
        index_length = index_length + (entries_per_block - (index_length % entries_per_block));

        // tprint out several infos
        printf("Created raw device with length %lu MB.\n", (long) dev_size/(1024*1024));
        printf("There are %u index-entries per block and %u entries are available.\n", entries_per_block, index_length);
        printf("The size of one index entry is %lu bytes.\n", sizeof(index_entry_t));
        fflush(stdout);

        pthread_mutex_lock(&lock);

        // allocate aligned buffer for index data
        if (posix_memalign((void**) &ind, BLOCKSIZE, index_length * sizeof(index_entry_t)) != 0){
            printf("JNI error (JNIFileRaw, open): could not allocate memory (%s).", strerror(errno));
            pthread_mutex_unlock(&lock);
            close(device);
            return -1;
        }

        // fill index buffer with 0 and flush to device at start position of index
        memset((void*) ind, 0, index_length * sizeof(index_entry_t));
        int written_bytes = pwrite(device, (void*) ind, index_length * sizeof(index_entry_t), index_start);
        if (written_bytes != index_length * sizeof(index_entry_t)) {
            // Error
            printf("JNI error (JNIFileRaw, open): could not write index data (%s).", strerror(errno));
            printf(" [FileID: %d], [Block addr.: 0x%lx], [Buffer addr.: 0x%p], [Length: %ld] [Ret: %d]\n",
                device, index_start, (void*) ind, index_length * sizeof(index_entry_t), read_bytes);
            return -1;
        }

        // start point for data
        data_start = index_start + index_length * sizeof(index_entry_t);
        append = data_start;

        // fill header struct with 0
        memset((void*) dev_head, 0, BLOCKSIZE);
        // fill in data for device header and flush to disk
        strcpy(dev_head->begin, header_begin);
        dev_head->length = dev_size;
        dev_head->blocksize = BLOCKSIZE;
        dev_head->index = index_start;
        dev_head->index_length = index_length;
        dev_head->data = data_start;
        dev_head->entry_size = sizeof(index_entry_t);
        dev_head->entries_per_block = entries_per_block;
        // write device header to device
        written_bytes = pwrite(device, (void*) dev_head, BLOCKSIZE, 0);
        if (written_bytes != BLOCKSIZE) {
            // Error
            printf("JNI error (JNIFileRaw, open): could not write header (%s).", strerror(errno));
            printf(" [FileID: %d], [Block addr.: 0x%x], [Buffer addr.: 0x%p], [Length: %d] [Ret: %d]\n",
                device, 0, (void*) dev_head, BLOCKSIZE, read_bytes);
            return -1;
        }

        pthread_mutex_unlock(&lock);

        printf("Wrote index and header to disk - preparation successful!\n");
        fflush(stdout);
    }

    free((void*) dev_head);
    (*env)->ReleaseStringUTFChars(env, jpath, path);

    return device;
}

/*
 * Class:   de_hhu_bsinfo_dxutils_jni_JNIFileRaw
 * Method:  open
 *          opens a new log file or creates it if it does not exist
 * jpath:   complete path to log including the filename
 * size:    preallocate disk space if != 0
 * return:  the file descriptor or negative value on error
 */
JNIEXPORT jint JNICALL Java_de_hhu_bsinfo_dxutils_jni_JNIFileRaw_open(JNIEnv *env, jclass clazz, jstring jpath,
        jlong size) {

    // Convert jstring path to pointer
    const jbyte *path;
    path = (*env)->GetStringUTFChars(env, jpath, NULL);

    int fileID = -1;

    // search for log with this name
    uint32_t lastIndex = next_free_index;
    int i = 0;
    for(i = 0; i <= lastIndex; i++){
        // if the filename is found and the first status bit is set and it is the first block
        // of the file, we have found the file
        if ((strstr((char*) path, ind[i].logName) != NULL) && (((ind[i].status) & sel_bit_first) != 0)
                && (ind[i].firstBlock == i)){
            printf("Opened log with name %s and index %u\n", (char*) path, i);
            printf("The size of this log is %u MB.\n", ind[i].part_length/(1024*1024));
            fflush(stdout);

            // set the open bit - no need for page to be written back,
            // because information is not persistent
            pthread_mutex_lock(&lock);
            ind[i].status = ind[i].status | set_bit_second;
            pthread_mutex_unlock(&lock);

            // return index as descriptor
            fileID = i;
        }
    }

    if (fileID == -1 /* file not found */) {
        // check if it is a primary, secondary or version log
        char logtype = 0x00;
        if (strstr((char*) path, prim_indicator) != NULL){
            // primary log
            logtype = 'p';
        } else if ( strstr((char*) path, sec_indicator) != NULL){
            // secondary log
            logtype = 's';
        } else if (strstr((char*) path, ver_indicator) != NULL){
            // version log
            logtype = 'v';
        } else{
            printf("Illegal filename\n");
            return -1;
        }

        // check size for log
        uint32_t logsize;
        if (size == 0){
            logsize = VER_BLOCK_SIZE;
        } else {
            logsize = size;
        }

        // prepare statusbits for index (first bit -> block exists, second bit -> log is open)
        char logstatus = 0xC0;

        // enter critical section because index data is affected
        pthread_mutex_lock(&lock);

        // check if device has enough free space and index position is available
        if (checkNextPosAndSpace(next_free_index, size) < 0){
            pthread_mutex_unlock(&lock);
            return -1;
        }

        // create index entry
        ind[next_free_index].status = logstatus;
        ind[next_free_index].type = logtype;
        ind[next_free_index].begin = append;
        ind[next_free_index].part_length = logsize;
        ind[next_free_index].nextBlock = next_free_index;
        ind[next_free_index].firstBlock = next_free_index;
        ind[next_free_index].cur_length = 0;
        strncpy(ind[next_free_index].logName, (char*) path, 37);
        ind[next_free_index].logName[37] = '\0';

        if (writeBackIndex(next_free_index) == -1){
            pthread_mutex_unlock(&lock);
            return -1;
        }

        // increment next free index position
        fileID = next_free_index;
        next_free_index++;
        // increase append-pointer
        append += logsize;

        // leave critical section
        pthread_mutex_unlock(&lock);

        printf("Created log with index number %u and name %s at address 0x%p.\n", fileID, ind[fileID].logName,
            ind[fileID].begin);
        fflush(stdout);
    }

    // release String-reference
    (*env)->ReleaseStringUTFChars(env, jpath, path);

    // return file-ID
    return (jint) fileID;
}

/*
 * Class:   de_hhu_bsinfo_dxutils_jni_JNIFileRaw
 * Method:  close
 *          closes an open log file
 * fileID: the file descriptor of the log file
 * return: 0 on success, -1 on error
 */
JNIEXPORT jint JNICALL Java_de_hhu_bsinfo_dxutils_jni_JNIFileRaw_close(JNIEnv *env, jclass clazz, jint fileID) {
    pthread_mutex_lock(&lock);
    // delete status bit - no need to write back to disk,
    // because this is not persistent
    ind[fileID].status = ind[fileID].status & null_bit_second;

    pthread_mutex_unlock(&lock);
    return 0;
}

/*
 * Class:   de_hhu_bsinfo_dxutils_jni_JNIFileRaw
 * Method:  remove
 *          deletes an existing log from index, so that space could be used for another log
 * fileID:  the file descriptor
 * return:  0 on success, -1 on error (log was opened or writing back index failed)
 */
JNIEXPORT jint JNICALL Java_de_hhu_bsinfo_dxutils_jni_JNIFileRaw_remove(JNIEnv *env, jclass clazz, jint fileID) {

    pthread_mutex_lock(&lock);

    // Check if log is opened and reutrn error
    if (((ind[fileID].status) & sel_bit_second) != 0) {
        pthread_mutex_unlock(&lock);
        return -1;
    }
    // set third bit for deleted and delete used-bit
    ind[fileID].status = ind[fileID].status | set_bit_third;
    ind[fileID].status = ind[fileID].status & null_bit_first;
    // write changed index back
    int ret = writeBackIndex(fileID);

    pthread_mutex_unlock(&lock);

    return ret;
}

/*
 * Copies overlapping bytes in front of the buffer.
 */
long retain_preceding_bytes(long write_buffer, long buffer_offset, long length, long aligned_start_pos,
        int off_start_pos) {
    // Determine start of previous page in buffer
    long block_start = write_buffer + buffer_offset - BLOCKSIZE;
    if (buffer_offset % BLOCKSIZE != 0) {
        block_start -= buffer_offset % BLOCKSIZE;
    }

    // Read bytes from disk
    int read_bytes;
    while (1) {
        read_bytes = pread(device, (void*) block_start, BLOCKSIZE, aligned_start_pos);
        if (read_bytes > 0) {
            // We want to read less than a page -> either all or nothing is read
            break;
        } else if (read_bytes == -1) {
            // Error
            printf("JNI error (JNIFileRaw, retain_preceding_bytes): could not read from file (%s).",
                strerror(errno));
            printf(" [FileID: %d], [Block addr.: 0x%lx], [Buffer addr.: 0x%lx]\n", device, aligned_start_pos,
                block_start);
            return -1;
        }
    }

    // Move bytes to write at the end of the copied bytes
    memmove((void*) (block_start + off_start_pos), (void*) (write_buffer + buffer_offset), length);

    return block_start;
}

/*
 * Class:   de_hhu_bsinfo_dxutils_jni_JNIFileRaw
 * Method:  appendAndTruncate
 *          writes buffer to file
 * fileID:  the file descriptor
 * buffer:  reference to the byte buffer containing the data; buffer must be page-aligned
 * offset:  start offset in data buffer
 * length:  number of bytes to write
 * pos:     write position in file
 * retain_end:  whether overlapping bytes can be overwritten (0) or not (1)
 * set_file_length:  whether the file length must be set after writing (0) or not (1)
 * return:  0 on success or -1 on error
 */
JNIEXPORT jint JNICALL Java_de_hhu_bsinfo_dxutils_jni_JNIFileRaw_write(JNIEnv *env, jclass clazz, jint fileID,
        jlong buffer, jint buffer_offset, jint length, jlong pos, jbyte retain_end, jbyte set_file_length) {

    // Get current position in file where write-access starts
    long start_pos = pos;
    // Get end position for write access
    long end_pos = start_pos + length - 1;

    // Get offsets to next block-aligned positions in file
    int off_start_pos = start_pos % BLOCKSIZE;
    int off_end_pos = BLOCKSIZE - (end_pos % BLOCKSIZE);

    // Calculate positions for begin and end of access with aligned start position
    long aligned_start_pos = start_pos - off_start_pos + ind[fileID].begin;
    long aligned_end_pos = end_pos + off_end_pos - 1 + ind[fileID].begin;
    long aligned_length = aligned_end_pos - aligned_start_pos + 1;

    long block_start = buffer;
    if (pos % BLOCKSIZE != 0) {
        // The file start position is not page-aligned -> copy overlapping bytes in front of the buffer aligned
        // at previous page. Given buffer MUST have one additional reserved preceding page!
        block_start = retain_preceding_bytes(buffer, buffer_offset, length, aligned_start_pos, off_start_pos);
        if (block_start == -1) {
            // Error (printed in retain_preceding_bytes)
            return -1;
        }
    } else if (buffer_offset % BLOCKSIZE) {
        // Even though the buffer is page-aligned, the beginning of the write access is not
        // -> move to the beginning of the previous page
        // (buffers are allocated with at least one overlapping page on both sides)
        long write_pos = buffer + buffer_offset;
        block_start = (write_pos - (write_pos % BLOCKSIZE));
        memmove((void*) block_start, (void*) write_pos, length);
    }

    int written_bytes;
    if (set_file_length == 0) {
        // Write the data from buffer to file
        while (aligned_length > 0) {
            written_bytes = pwrite(device, (void*) block_start, aligned_length, aligned_start_pos);
            if (written_bytes == -1) {
                // Error
                printf("JNI error (JNIFileRaw, write): could not write to file (%s).", strerror(errno));
                printf(" [FileID: %d], [Block addr.: 0x%lx], [Buffer addr.: 0x%lx], [Length: %ld]\n",
                    device, aligned_start_pos, block_start, aligned_length);
                return -1;
            }
            aligned_length -= written_bytes;
        }
    } else {
        uint32_t start_index_entry = 0;       // index of part containing aligned_start_pos
        uint32_t end_index_entry = 0;         // index of part containing aligned_end_pos
        uint32_t length_at_begin = 0;         // in-file-offset of the beginning of the start_index-part
        uint32_t length_at_end = 0;           // in-file-offset of the beginning of the end_index-part
        uint32_t cur_ind = fileID;            // index to scan
        uint32_t rem_length = aligned_length; // length that is not placed in a part yet
        uint32_t in_file_pos = 0;             // current position in file
        uint64_t start_index_write = 0;       // position to write changed index back


        // first step: check file size an add parts if needed
        // this is only index operation without writing the data, because a lock is required to do this
        pthread_mutex_lock(&lock);

        //         This is the loop that checks if there are enough parts allocated for this
        //         VersionLog to write all the data at the given position.
        //         rem_length counts how many bytes can be stored in the already scanned area,
        //         if there are no remaining bytes left, the loop finishes and the function continues.
        //         Because of the many cases that can occur, loop has many branches with if-else.
        //         Therefore, a small overview is given here:
        //
        //         while bytes are remaining
        //           ->if write-start position is in current part
        //               ->if write-end position is in current part
        //                   nothing else to do, write_end position exists -> no remaining bytes left
        //               ->else (write_end position not in current part)
        //                   ->if next part exists
        //                       goto next part -> next round
        //                   ->else (there exists no next part)
        //                      a new part mus be allcated for all remaining data -> no remaining bytes left
        //           ->else (write-start position not in current part)
        //               ->if write-end position is in this part
        //                   nothing else to do, write-end position exists -> no remaining bytes left
        //               ->else (write-end position is not in this part)
        //                   ->if write-start position is surpassed
        //                      remaining length has to be updated
        //                   ->if next part exists
        //                      goto next part -> next round
        //                   ->else (there is no next part)
        //                      a new part has to be created
        //                      ->if write-start position was not surpassed
        //                         add offset from current to write-start position to the remaining length
        //                      create part with remaining length -> no remaining bytes left
        //
        //         This scheme should guide you through the following loop:

        while (rem_length > 0){
            // start position for write is in current part
            if (aligned_start_pos >= in_file_pos && aligned_start_pos < in_file_pos + ind[cur_ind].part_length){
                // set start_index
                start_index_entry = cur_ind;
                length_at_begin = in_file_pos;

                // end position is in current part -> nothing to do
                if (aligned_end_pos < in_file_pos + ind[cur_ind].part_length){
                    end_index_entry = cur_ind;
                    length_at_end = in_file_pos;
                    rem_length = 0;
                    // search for another part or add an new part
                } else {
                    // set position to end of part and calculate remaining length to write
                    in_file_pos += ind[cur_ind].part_length;
                    rem_length -= (in_file_pos - aligned_start_pos);

                    // case 1: there is a next part
                    if (ind[cur_ind].nextBlock != cur_ind){
                        // scan next part
                        cur_ind = ind[cur_ind].nextBlock;
                        // case 2: there exists no further part -> allocate space for a new part
                        // that is big enough for all the remaining data and make index entry
                    } else {
                        // calculate suitable length for new part -> multiple of VER_BLOCK_SIZE;
                        rem_length += (VER_BLOCK_SIZE - (rem_length % VER_BLOCK_SIZE));

                        // check if device has enough free space and index position is available
                        if (checkNextPosAndSpace(next_free_index, rem_length) < 0){
                            pthread_mutex_unlock(&lock);
                            return -1;
                        }

                        // create new index entry
                        ind[next_free_index].status = 0x80;
                        ind[next_free_index].type = ind[fileID].type;
                        ind[next_free_index].begin = append;
                        ind[next_free_index].part_length = rem_length;
                        ind[next_free_index].nextBlock = next_free_index;
                        ind[next_free_index].firstBlock = fileID;
                        ind[next_free_index].cur_length;
                        strncpy(ind[next_free_index].logName, ind[fileID].logName, 37);
                        ind[next_free_index].logName[37] = '\0';

                        // write new index page to disk
                        if (writeBackIndex(next_free_index) == -1){
                            pthread_mutex_unlock(&lock);
                            return -1;
                        }

                        // set pointer to new part
                        ind[cur_ind].nextBlock = next_free_index;

                        // write changed index back to disk
                        if (writeBackIndex(cur_ind) == -1){
                            pthread_mutex_unlock(&lock);
                            return -1;
                        }

                        end_index_entry = next_free_index;
                        length_at_end = in_file_pos;

                        // change values
                        append += rem_length;
                        next_free_index++;

                        // remaining length is 0
                        rem_length = 0;
                    }
                }
                // start position for write is not in current part
            } else {
                // case 1: end position in this part - nothing to do
                if (aligned_end_pos < in_file_pos + ind[cur_ind].part_length){
                    length_at_end = in_file_pos;
                    end_index_entry = cur_ind;
                    // no bytes remaining
                    rem_length = 0;
                    // case 2: end position is not in this part
                } else {
                    // if aligned_start_pos is already surpassed, remaining length must be calulated
                    if (in_file_pos >= aligned_start_pos){
                        rem_length -= ind[cur_ind].part_length;
                    }
                    in_file_pos += ind[cur_ind].part_length;

                    // case 1: there exists another part - scan this part
                    if (ind[cur_ind].nextBlock != cur_ind){
                        cur_ind = ind[cur_ind].nextBlock;
                        // case 2: there is no other part - a new part must be allocated
                    } else {
                        // calculate suitable length for new part -> multiple of VER_BLOCK_SIZE
                        // if start-position is not surpassed, offset has to be added to remaining length
                        if (in_file_pos <= aligned_start_pos){
                            rem_length += (aligned_start_pos - in_file_pos);
                        }
                        rem_length += (VER_BLOCK_SIZE - (rem_length % VER_BLOCK_SIZE));

                        // check if device has enough free space and index position is available
                        if (checkNextPosAndSpace(next_free_index, rem_length) < 0){
                            pthread_mutex_unlock(&lock);
                            return -1;
                        }

                        // create new index entry
                        ind[next_free_index].status = 0x80;
                        ind[next_free_index].type = ind[fileID].type;
                        ind[next_free_index].begin = append;
                        ind[next_free_index].part_length = rem_length;
                        ind[next_free_index].nextBlock = next_free_index;
                        ind[next_free_index].firstBlock = fileID;
                        ind[next_free_index].cur_length = 0;
                        strncpy(ind[next_free_index].logName, ind[fileID].logName, 37);
                        ind[next_free_index].logName[37] = '\0';

                        // write new index page to disk
                        if (writeBackIndex(next_free_index) == -1){
                            pthread_mutex_unlock(&lock);
                            return -1;
                        }

                        // set pointer to new part
                        ind[cur_ind].nextBlock = next_free_index;

                        // write changed index page to disk
                        if (writeBackIndex(cur_ind) == -1){
                            pthread_mutex_unlock(&lock);
                            return -1;
                        }

                        // if start-position is not surpassed, it is in new allocated block;
                        if (in_file_pos <= aligned_start_pos){
                            start_index_entry = next_free_index;
                            length_at_begin = in_file_pos;
                        }
                        end_index_entry = next_free_index;
                        length_at_end = in_file_pos;

                        // change values
                        append += rem_length;
                        next_free_index++;

                        // there are no remaining bytes
                        rem_length = 0;
                    }
                }
            }
        }

        pthread_mutex_unlock(&lock);

        fflush(stdout);

        // the needed space on device is allocated - now we can read/write the data without locks

        // loop that writes buffer to disk
        rem_length = aligned_length;
        cur_ind = start_index_entry;
        int ret = 0;
        uint32_t start_disp = aligned_start_pos - length_at_begin;
        uint32_t end_disp = 0;

        while (rem_length > 0){
            if (cur_ind == end_index_entry){
                end_disp = ind[cur_ind].part_length - start_disp - rem_length;
            }
            uint32_t write_length = ind[cur_ind].part_length - start_disp - end_disp;

            if (pwrite(device, (void*) (block_start + (aligned_length - rem_length)), write_length,
                    ind[cur_ind].begin + start_disp) < 0){
                return -1;
            }

            if (cur_ind == start_index_entry){
                start_disp = 0;
            }
            cur_ind = ind[cur_ind].nextBlock;
            rem_length -= write_length;
        }

        // Set length of file - cut away the padding that has been added because of alignment
        pthread_mutex_lock(&lock);
        ind[fileID].cur_length = (uint32_t)length;
        // write changed index page to disk
        if (writeBackIndex(fileID) == -1){
            pthread_mutex_unlock(&lock);
            printf("JNI error (JNIFileRaw, write): could not truncate file (%s).", strerror(errno));
            printf(" [FileID: %d], [File length: %ld]\n", fileID, start_pos + length);
            return -1;
        }
        pthread_mutex_unlock(&lock);
    }

    return written_bytes;
}

/*
 * Class:   de_hhu_bsinfo_dxutils_jni_JNIFileRaw
 * Method:  read
 *          reads from log file into data buffer at current position
 * fileID:  the file descriptor
 * buffer:  reference to the byte buffer that should receive the data
 * offset:  start offset in data buffer
 * length:  number of bytes to read
 * pos:     read position in file
 * return:  0 on success or -1 on error
 */
JNIEXPORT jint JNICALL Java_de_hhu_bsinfo_dxutils_jni_JNIFileRaw_read(JNIEnv *env, jclass clazz, jint fileID,
        jlong buffer, jint offset, jint length, jlong pos) {

    /* Every read-access with O_DIRECT has to be block-aligned.
    Therefore, we get the boundaries for this access and extend them to block-aligned boundaries. */

    // get current position in file where read-access starts
    long start_pos = pos;
    // get end position for read access
    long end_pos = start_pos + length - 1;

    // get offsets to next block-aligned positions in file
    int off_start = start_pos % BLOCKSIZE;
    int off_end = BLOCKSIZE - (end_pos % BLOCKSIZE);

    // calculate positions for begin and end of read access with aligned start position and length
    long aligned_start_pos = start_pos - off_start + ind[fileID].begin;
    long aligned_end_pos = end_pos + off_end - 1 + ind[fileID].begin;
    long aligned_length = aligned_end_pos - aligned_start_pos + 1;

    int ret;
    if (ind[fileID].type == 'v') {
        uint32_t start_index_entry = 0;       // index of part containing aligned_start_pos
        uint32_t end_index_entry = 0;         // index of part containing aligned_end_pos
        uint32_t length_at_begin = 0;         // in-file-offset of the beginning of the start_index-part
        uint32_t cur_ind = fileID;            // index to scan
        uint32_t rem_length = aligned_length;   // length that is not read from a part yet
        uint32_t in_file_pos = 0;             // current position in file

        // find start index for aligned_start_pos
        while (aligned_start_pos >= in_file_pos + ind[cur_ind].part_length){
            in_file_pos = in_file_pos + ind[cur_ind].part_length;
            // if there is no other block -> no read possible
            if (ind[cur_ind].nextBlock == cur_ind){
                return -1;
            }
            cur_ind = ind[cur_ind].nextBlock;
        }
        // aligned_start_pos is in part with index cur_ind
        start_index_entry = cur_ind;
        length_at_begin = in_file_pos;

        // find index for aligned_end_pos
        while (aligned_end_pos >= in_file_pos + ind[cur_ind].part_length){
            in_file_pos = in_file_pos + ind[cur_ind].part_length;
            // if there is no other block -> no read possible
            if (ind[cur_ind].nextBlock == cur_ind){
                return -1;
            }
            cur_ind = ind[cur_ind].nextBlock;
        }
        // alinged_end_pos is in part with index cur_ind
        end_index_entry = cur_ind;

        // loop that reads to buffer
        cur_ind = start_index_entry;
        ret = 0;
        uint32_t start_disp = aligned_start_pos - length_at_begin;
        uint32_t end_disp = 0;
        uint32_t read_length = 0;

        while (rem_length > 0){
            if (cur_ind == end_index_entry){
                end_disp = ind[cur_ind].part_length - start_disp - rem_length;
            }
            read_length = ind[cur_ind].part_length - start_disp - end_disp;

            if (pread(device, (void*) (buffer + (aligned_length - rem_length)), read_length,
                    ind[cur_ind].begin + start_disp) < 0){
                return -1;
            }

            if (cur_ind == start_index_entry){
                start_disp = 0;
            }
            cur_ind = ind[cur_ind].nextBlock;
            rem_length -= read_length;
        }
    } else {
        // read the data from file to buffer
        ret = pread(device, (void*) buffer, aligned_length, aligned_start_pos);
        if (ret == -1 || ret != aligned_length && ret != get_length(fileID)) {
            // Error
            printf("JNI error (JNIFileRaw, read): could not read from file (%s).", strerror(errno));
            printf(" [FileID: %d], [Block addr.: 0x%lx], [Buffer addr.: 0x%lx], [Length: %ld] [Ret: %d]\n",
                device, aligned_start_pos, buffer, aligned_length, ret);
            return -1;
        }
    }

    return (jint) ret;
}

/*
 * Class:   de_hhu_bsinfo_dxutils_jni_JNIFileRaw
 * Method:  length
 *          returns the length of the file
 * fileID:  file descriptor
 * return:  the length
 */
JNIEXPORT jlong JNICALL Java_de_hhu_bsinfo_dxutils_jni_JNIFileRaw_length(JNIEnv *env, jclass clazz, jint fileID) {
    return (jlong) get_length(fileID);
}

/*
 * Class:   de_hhu_bsinfo_dxutils_jni_JNIFileRaw
 * Method:  getFileList
 *          returns a String with all names of the logs, separated by '\n'
 * return:  String with all log names
 */
JNIEXPORT jstring JNICALL Java_de_hhu_bsinfo_dxutils_JNIFileRaw_getFileList(JNIEnv *env, jclass clazz) {
    // acquire lock becuase index must not be chnaged during this
    pthread_mutex_lock(&lock);

    // first: calculate length of resulting string
    uint32_t string_length = 0;
    uint32_t i = 0;
    // loop while entry is in use
    while (((ind[i].status) & sel_bit_first) != 0x00){
        if (ind[i].firstBlock == i){
            // add length of string
            string_length += strlen(ind[i].logName);
            // add \n character
            string_length++;
        }
        i++;
    }
    // add 0-Terminator
    string_length++;

    // now create resulting String -> each entry has 37 characters + \n, in addition \0 for whole string
    char *res;
    if ((res = malloc(string_length)) == NULL){
        pthread_mutex_unlock(&lock);
        return (*env)->NewStringUTF(env, "Failure");
    }

    i = 0;
    uint32_t cur_pos = 0;
    while (((ind[i].status) & sel_bit_first) != 0x00){
        if (ind[i].firstBlock == i){
            // String is terminated in any case
            strcpy((res + cur_pos), ind[i].logName);
            // set linebreak
            res[cur_pos + strlen(ind[i].logName)] = '\n';
            // next position in array
            cur_pos += (strlen(ind[i].logName) + 1);
        }
        i++;
    }
    // set 0-Terminator
    res[string_length-1] = '\0';

    pthread_mutex_unlock(&lock);

    return (*env)->NewStringUTF(env, res);
}

/*
 * Class:   de_hhu_bsinfo_dxutils_jni_JNIFileRaw
 * Method:  print_index for test purposes
 *          prints all logs in index together with their index number, size and name. Only for tests.
 */
JNIEXPORT void JNICALL Java_de_hhu_bsinfo_dxutils_JNIFileRaw_printIndexForTest(JNIEnv *env, jclass clazz) {
    printf("(List of logs or segments in index)\n");

    uint32_t i = 0;
    for(i = 0; i < index_length; i++){
        // check if the first bit of status is set
        if (((ind[i].status) & sel_bit_first) != 0x00){
            printf("Log index %u and logname %s with size %u MB. It begins at position %lu \n", i, ind[i].logName,
                ind[i].part_length / (1024*1024), ind[i].begin);
        } else {
            break;
        }
    }
}
