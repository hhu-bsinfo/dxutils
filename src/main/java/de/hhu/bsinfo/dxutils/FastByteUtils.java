package de.hhu.bsinfo.dxutils;

/**
 * @author Florian Hucke (florian.hucke@hhu.de) on 25.02.18
 * @projectname dxram-memory
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class FastByteUtils {

    /**
     * Converts a primitive long to a byte array.
     *
     * @param value Value to convert
     * @return A byte array which stores the value in the big-endian order A byte array
     */
    public static byte[] longToBytes(long value) {
        return ToBytes(value, Long.BYTES);
    }

    /**
     * Converts a byte array to a primitive long.
     *
     * @param bytes Bytes to convert
     * @return A primitive long based on the big-endian byte array input
     */
    public static long bytesToLong(byte[] bytes) {
        return bytesTo(bytes, Long.BYTES);
    }

    /**
     * Converts a primitive int to a byte array.
     *
     * @param value Value to convert
     * @return A byte array which stores the value in the big-endian order
     */
    public static byte[] intToBytes(int value) {
        return ToBytes(value, Integer.BYTES);
    }

    /**
     * Converts a byte array to a primitive int.
     *
     * @param bytes Bytes to convert
     * @return A primitive int based on the big-endian byte array input
     */
    public static int bytesToInt(byte[] bytes) {
        return (int) bytesTo(bytes, Integer.BYTES);
    }

    /**
     * Converts a primitive short to a byte array.
     *
     * @param value Value to convert
     * @return A byte array which stores the value in the big-endian order
     */
    public static byte[] shortToBytes(short value) {
        return ToBytes(value, Short.BYTES);
    }

    /**
     * Converts a byte array to a primitive short.
     *
     * @param bytes Bytes to convert
     * @return A primitive short based on the big-endian byte array input
     */
    public static short bytesToShort(byte[] bytes) {
        return (short) bytesTo(bytes, Short.BYTES);
    }

    /**
     * Create a byte array of a primitive data type. The order is big-endian.
     *
     * @param value Value to convert
     * @param byteSize Byte size of the primitive data type.
     * @return A byte array which stores the value in the big-endian order
     */
    private static byte[] ToBytes(long value, final int byteSize){
        byte[] ret = new byte[byteSize];

        for (int i = byteSize - 1; i >= 0; i--) {
            ret[i] = (byte)(value & 0xFF);
            value >>= 8;
        }
        return ret;
    }

    /**
     * Converts a byte array, in big-endian order, to a primitive data type
     *
     * @param bytes Bytes to convert
     * @param byteSize Byte size of the primitive data type.
     * @return A primitive data type based on the big-endian byte array input
     */
    private static long bytesTo(byte[] bytes, final int byteSize) {
        long ret = 0;
        int size = (bytes.length<byteSize) ? bytes.length:byteSize;

        for (int i = 0; i < size; i++) {
            ret <<= 8;
            ret |= (bytes[i] & 0xFF);
        }
        return ret;
    }

}
