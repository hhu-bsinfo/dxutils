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

package de.hhu.bsinfo.dxutils.serialization;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of an Importer/Exporter for ByteBuffers.
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 26.01.2016
 */
public class ByteBufferImExporter implements Importer, Exporter {
    private ByteBuffer m_byteBuffer;

    /**
     * Constructor
     *
     * @param p_buffer
     *         Buffer to write to/read from.
     */
    public ByteBufferImExporter(final ByteBuffer p_buffer) {
        m_byteBuffer = p_buffer;
    }

    @Override
    public void exportObject(final Exportable p_object) {
        p_object.exportObject(this);
    }

    @Override
    public void writeBoolean(boolean p_v) {
        m_byteBuffer.put((byte) (p_v ? 1 : 0));
    }

    @Override
    public void writeByte(final byte p_v) {
        m_byteBuffer.put(p_v);
    }

    @Override
    public void writeShort(final short p_v) {
        m_byteBuffer.putShort(p_v);
    }

    @Override
    public void writeChar(final char p_v) {
        m_byteBuffer.putChar(p_v);
    }

    @Override
    public void writeInt(final int p_v) {
        m_byteBuffer.putInt(p_v);
    }

    @Override
    public void writeLong(final long p_v) {
        m_byteBuffer.putLong(p_v);
    }

    @Override
    public void writeFloat(final float p_v) {
        m_byteBuffer.putFloat(p_v);
    }

    @Override
    public void writeDouble(final double p_v) {
        m_byteBuffer.putDouble(p_v);
    }

    @Override
    public void writeCompactNumber(int p_v) {
        byte[] number = CompactNumber.compact(p_v);
        writeBytes(number);
    }

    @Override
    public void writeString(final String p_str) {
        writeByteArray(p_str.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public int writeBytes(final byte[] p_array) {
        return writeBytes(p_array, 0, p_array.length);
    }

    @Override
    public int writeBytes(final byte[] p_array, final int p_offset, final int p_length) {
        m_byteBuffer.put(p_array, p_offset, p_length);
        return p_length;
    }

    @Override
    public void importObject(final Importable p_object) {
        p_object.importObject(this);
    }

    @Override
    public boolean readBoolean(final boolean p_bool) {
        return m_byteBuffer.get() == 1;
    }

    @Override
    public byte readByte(final byte p_byte) {
        return m_byteBuffer.get();
    }

    @Override
    public short readShort(final short p_short) {
        return m_byteBuffer.getShort();
    }

    @Override
    public char readChar(final char p_char) {
        return m_byteBuffer.getChar();
    }

    @Override
    public int readInt(final int p_int) {
        return m_byteBuffer.getInt();
    }

    @Override
    public long readLong(final long p_long) {
        return m_byteBuffer.getLong();
    }

    @Override
    public float readFloat(final float p_float) {
        return m_byteBuffer.getFloat();
    }

    @Override
    public double readDouble(final double p_double) {
        return m_byteBuffer.getDouble();
    }

    @Override
    public int readCompactNumber(int p_int) {
        int ret = 0;
        int i;
        for (i = 0; i < Integer.BYTES; i++) {
            byte b = readByte((byte) 0);
            ret += (b & 0x7F) << i * 7;
            if ((b & 0x80) == 0) {
                break;
            }
        }

        return ret;
    }

    @Override
    public String readString(final String p_string) {
        return new String(readByteArray(null));
    }

    @Override
    public int readBytes(final byte[] p_array) {
        return readBytes(p_array, 0, p_array.length);
    }

    @Override
    public int readBytes(final byte[] p_array, final int p_offset, final int p_length) {
        m_byteBuffer.get(p_array, p_offset, p_length);
        return p_length;
    }

    @Override
    public int writeShorts(final short[] p_array) {
        return writeShorts(p_array, 0, p_array.length);
    }

    @Override
    public int writeChars(final char[] p_array) {
        return writeChars(p_array, 0, p_array.length);
    }

    @Override
    public int writeInts(final int[] p_array) {
        return writeInts(p_array, 0, p_array.length);
    }

    @Override
    public int writeLongs(final long[] p_array) {
        return writeLongs(p_array, 0, p_array.length);
    }

    @Override
    public int writeFloats(final float[] p_array) {
        return writeFloats(p_array, 0, p_array.length);
    }

    @Override
    public int writeDoubles(final double[] p_array) {
        return writeDoubles(p_array, 0, p_array.length);
    }

    @Override
    public int writeShorts(final short[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            m_byteBuffer.putShort(p_array[p_offset + i]);
        }

        return p_length;
    }

    @Override
    public int writeChars(final char[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            m_byteBuffer.putChar(p_array[p_offset + i]);
        }

        return p_length;
    }

    @Override
    public int writeInts(final int[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            m_byteBuffer.putInt(p_array[p_offset + i]);
        }

        return p_length;
    }

    @Override
    public int writeLongs(final long[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            m_byteBuffer.putLong(p_array[p_offset + i]);
        }

        return p_length;
    }

    @Override
    public int writeFloats(final float[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            m_byteBuffer.putFloat(p_array[p_offset + i]);
        }

        return p_length;
    }

    @Override
    public int writeDoubles(final double[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            m_byteBuffer.putDouble(p_array[p_offset + i]);
        }

        return p_length;
    }

    @Override
    public void writeByteArray(final byte[] p_array) {
        writeCompactNumber(p_array.length);
        writeBytes(p_array);
    }

    @Override
    public void writeShortArray(final short[] p_array) {
        writeCompactNumber(p_array.length);
        writeShorts(p_array);
    }

    @Override
    public void writeCharArray(final char[] p_array) {
        writeCompactNumber(p_array.length);
        writeChars(p_array);
    }

    @Override
    public void writeIntArray(final int[] p_array) {
        writeCompactNumber(p_array.length);
        writeInts(p_array);
    }

    @Override
    public void writeLongArray(final long[] p_array) {
        writeCompactNumber(p_array.length);
        writeLongs(p_array);
    }

    @Override
    public void writeFloatArray(final float[] p_array) {
        writeCompactNumber(p_array.length);
        writeFloats(p_array);
    }

    @Override
    public void writeDoubleArray(final double[] p_array) {
        writeCompactNumber(p_array.length);
        writeDoubles(p_array);
    }

    @Override
    public int readShorts(final short[] p_array) {
        return readShorts(p_array, 0, p_array.length);
    }

    @Override
    public int readChars(final char[] p_array) {
        return readChars(p_array, 0, p_array.length);
    }

    @Override
    public int readInts(final int[] p_array) {
        return readInts(p_array, 0, p_array.length);
    }

    @Override
    public int readLongs(final long[] p_array) {
        return readLongs(p_array, 0, p_array.length);
    }

    @Override
    public int readFloats(final float[] p_array) {
        return readFloats(p_array, 0, p_array.length);
    }

    @Override
    public int readDoubles(final double[] p_array) {
        return readDoubles(p_array, 0, p_array.length);
    }

    @Override
    public int readShorts(final short[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            p_array[p_offset + i] = m_byteBuffer.getShort();
        }

        return p_length;
    }

    @Override
    public int readChars(final char[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            p_array[p_offset + i] = m_byteBuffer.getChar();
        }

        return p_length;
    }

    @Override
    public int readInts(final int[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            p_array[p_offset + i] = m_byteBuffer.getInt();
        }

        return p_length;
    }

    @Override
    public int readLongs(final long[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            p_array[p_offset + i] = m_byteBuffer.getLong();
        }

        return p_length;
    }

    @Override
    public int readFloats(final float[] p_array, final int p_offset, final int p_length) {
        for (int i = 0; i < p_length; i++) {
            p_array[p_offset + i] = m_byteBuffer.getFloat();
        }

        return p_length;
    }

    @Override
    public int readDoubles(double[] p_array, int p_offset, int p_length) {
        for (int i = 0; i < p_length; i++) {
            p_array[p_offset + i] = m_byteBuffer.getDouble();
        }

        return p_length;
    }

    @Override
    public byte[] readByteArray(final byte[] p_array) {
        byte[] arr = new byte[readCompactNumber(0)];
        readBytes(arr);
        return arr;
    }

    @Override
    public short[] readShortArray(final short[] p_array) {
        short[] arr = new short[readCompactNumber(0)];
        readShorts(arr);
        return arr;
    }

    @Override
    public char[] readCharArray(final char[] p_array) {
        char[] arr = new char[readCompactNumber(0)];
        readChars(arr);
        return arr;
    }

    @Override
    public int[] readIntArray(final int[] p_array) {
        int[] arr = new int[readCompactNumber(0)];
        readInts(arr);
        return arr;
    }

    @Override
    public long[] readLongArray(final long[] p_array) {
        long[] arr = new long[readCompactNumber(0)];
        readLongs(arr);
        return arr;
    }

    @Override
    public float[] readFloatArray(final float[] p_array) {
        float[] arr = new float[readCompactNumber(0)];
        readFloats(arr);
        return arr;
    }

    @Override
    public double[] readDoubleArray(final double[] p_array) {
        double[] arr = new double[readCompactNumber(0)];
        readDoubles(arr);
        return arr;
    }
}
