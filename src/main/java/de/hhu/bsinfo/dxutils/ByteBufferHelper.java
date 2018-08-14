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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Helper class for advanced usage of ByteBuffers
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 11.07.2017
 */
public final class ByteBufferHelper {
    private static final Class<?> ms_directByteBufferClass;
    private static final Field ms_byteBufferAddress;
    private static final Field ms_byteBufferCapacity;
    private static final Constructor<?> ms_directByteBufferConstructor;

    static {
        try {
            ms_directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
            ms_directByteBufferConstructor = ms_directByteBufferClass.getDeclaredConstructor(Long.TYPE, Integer.TYPE);
            ms_directByteBufferConstructor.setAccessible(true);
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            ms_byteBufferAddress = Buffer.class.getDeclaredField("address");
            ms_byteBufferAddress.setAccessible(true);

            ms_byteBufferCapacity = Buffer.class.getDeclaredField("capacity");
            ms_byteBufferCapacity.setAccessible(true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utils class
     */
    private ByteBufferHelper() {

    }

    /**
     * Get the memory address of the array allocated on the heap outside of the jvm for a DirectByteBuffer
     *
     * @param p_buffer
     *         Buffer to get the address of (must be a DirectByteBuffer!)
     * @return Memory address pointing to the data stored on the heap outside of the jvm
     */
    public static long getDirectAddress(final ByteBuffer p_buffer) {
        try {
            return ms_byteBufferAddress.getLong(p_buffer);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the memory address of the array allocated on the native heap (outside of the JVM)
     *
     * @param p_buffer
     *         DirectByteBuffer instance
     * @param p_address
     *         Native memory address to set
     */
    public static void setDirectAddress(final ByteBuffer p_buffer, final long p_address) {
        try {
            ms_byteBufferAddress.setLong(p_buffer, p_address);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the capacity of the array allocated on the native heap (outside of the JVM)
     *
     * @param p_buffer
     *         DirectByteBuffer instance
     * @param p_capacity
     *         Capacity to set
     */
    public static void setCapacity(final ByteBuffer p_buffer, final int p_capacity) {
        try {
            ms_byteBufferCapacity.setInt(p_buffer, p_capacity);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wrap a (unsafe) pointer
     *
     * @param p_addr
     *         Pointer address
     * @param p_size
     *         Size of the memory region
     * @return Wrapped addr as ByteBuffer
     */
    public static ByteBuffer wrap(final long p_addr, final int p_size) {
        try {
            return (ByteBuffer) ms_directByteBufferConstructor.newInstance(p_addr, p_size);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * "Unwrap" a previously wrapped DirectByteBuffer to avoid deallocation of the native memory on deallocation of the
     * ByteBuffer object
     *
     * @param p_buffer
     *         DirectByteBuffer object to unwrap
     */
    public static void unwrap(final ByteBuffer p_buffer) {
        if (p_buffer.getClass().isAssignableFrom(ms_directByteBufferClass)) {
            try {
                ms_byteBufferAddress.setLong(p_buffer, 0);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Can't unwrap a non direct byte buffer object: " + p_buffer);
        }
    }
}
