package de.hhu.bsinfo.dxutils;

/**
 * A helper class to create bit masks
 *
 * @author Florian Hucke (florian.hucke@hhu.de) on 07.02.18
 * @projectname dxram-memory
 */
public class BitMask {

    private final int bitSize;
    private long control = 0x0;
    private byte usedBits = 0;

    public BitMask(int byteSizeForBitMask){
        bitSize = byteSizeForBitMask * 8;
    }

    /**
     * Create a bit mask. Check if there are a union with previous created bit masks
     * @param neededBits number of ones
     * @return a checked bit mask
     */
    public long checkedCreate(long neededBits){
        assert usedBits < bitSize;

        long ret = create(neededBits, usedBits);

        assert (control & ret) == 0 : "overlapping masks. control = " +
                String.format("0x%016X", control) + " ret = " + String.format("0x%016X", ret);
        control |= ret;

        usedBits += neededBits;

        return ret;
    }

    /**
     * create bit masks
     * @param neededBits number of ones
     * @param offset offset from the LSB
     * @return a bit mask
     */
    public long create(long neededBits, int offset){


        long ret = 0;
        for (int i = 0; i < neededBits; i++) {
            ret = (ret << 1) | 1;
        }

        return ret << offset;
    }

    /**
     * Static method to create a bit mask
     * @param neededBits number of ones
     * @param offset offset of the ones
     * @return a bit mask as long variable
     */
    public static long createMask(long neededBits, int offset){
        assert neededBits+offset <= Long.SIZE : "bit mask result in a overflow";

        long ret = 0;
        for (int i = 0; i < neededBits; i++) {
            ret = (ret << 1) | 1;
        }

        return ret << offset;
    }

    public byte getUsedBits(){
        return usedBits;
    }

}
