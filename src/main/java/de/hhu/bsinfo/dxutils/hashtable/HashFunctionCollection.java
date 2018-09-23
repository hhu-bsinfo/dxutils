package de.hhu.bsinfo.dxutils.hashtable;

public final class HashFunctionCollection {

    /**
     * Private constructor.
     */
    private HashFunctionCollection() {
    }

    /**
     * Hashes the given int key.
     *
     * @param p_key
     *         the key
     * @return the hash value
     */
    static int hash(final int p_key) {
        int hash = p_key;

        hash = (hash >> 16 ^ hash) * 0x45d9f3b;
        hash = (hash >> 16 ^ hash) * 0x45d9f3b;
        return hash >> 16 ^ hash;
    }

    /**
     * Hashes the given long key with MurmurHash3.
     *
     * @param p_key
     *         the key
     * @return the hash value
     */
    public static int hash(final long p_key) {
        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;
        int h1 = 0x9747b28c;
        int k1;

        k1 = ((short) p_key & 0xff) + ((int) p_key & 0xff00) + ((int) p_key & 0xff0000) + ((int) p_key & 0xff000000);
        k1 *= c1;
        k1 = k1 << 15 | k1 >>> 17;
        k1 *= c2;
        h1 ^= k1;
        h1 = h1 << 13 | h1 >>> 19;
        h1 = h1 * 5 + 0xe6546b64;

        k1 = (int) ((p_key & 0xff00000000L) + (p_key & 0xff0000000000L) + (p_key & 0xff000000000000L) +
                (p_key & 0xff000000000000L));
        k1 *= c1;
        k1 = k1 << 15 | k1 >>> 17;
        k1 *= c2;
        h1 ^= k1;
        h1 = h1 << 13 | h1 >>> 19;
        h1 = h1 * 5 + 0xe6546b64;

        h1 ^= 8;
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }
}
