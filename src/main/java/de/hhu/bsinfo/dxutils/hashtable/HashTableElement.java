package de.hhu.bsinfo.dxutils.hashtable;

/**
 * Element for generic hash table. Stores key and value.
 *
 * @param <T>
 *         the generic value
 */
public final class HashTableElement<T> {

    private final int m_key;
    private T m_value;

    /**
     * Creates an instance of HashTableElement.
     *
     * @param p_key
     *         the key
     * @param p_value
     *         the value
     */
    HashTableElement(final int p_key, final T p_value) {
        m_key = p_key;
        m_value = p_value;
    }

    /**
     * Returns the key.
     *
     * @return the key
     */
    public int getKey() {
        return m_key;
    }

    /**
     * Returns the value.
     *
     * @return the generic value.
     */
    public T getValue() {
        return m_value;
    }

    /**
     * Sets the value.
     *
     * @param p_value
     *         the generic value.
     */
    public void setValue(final T p_value) {
        m_value = p_value;
    }
}
