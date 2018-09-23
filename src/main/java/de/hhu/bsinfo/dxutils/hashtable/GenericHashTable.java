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

package de.hhu.bsinfo.dxutils.hashtable;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stores key-value tuples whereas keys are ints and values generic objects.
 *
 * @author Kevin Beineke, kevin.beineke@hhu.de, 26.02.2018
 */
@SuppressWarnings("unchecked")
public final class GenericHashTable<T> {

    private static final int INITIAL_SIZE = 100;
    private static final float LOAD_FACTOR = 0.9f;

    private static final Logger LOGGER = LogManager.getFormatterLogger(GenericHashTable.class.getSimpleName());

    private HashTableElement<T>[] m_table;
    private int m_elementCapacity;
    private int m_count;

    private ArrayList<HashTableElement<T>> m_list;

    /**
     * Creates an instance of GenericHashTable.
     */
    public GenericHashTable() {
        m_count = 0;
        m_elementCapacity = INITIAL_SIZE;

        m_table = new HashTableElement[m_elementCapacity];
        m_list = new ArrayList<>();
    }

    /**
     * Creates an instance of GenericHashTable.
     *
     * @param p_initialSize
     *         the initial size
     */
    public GenericHashTable(final int p_initialSize) {

        assert p_initialSize > 0;

        m_count = 0;
        m_elementCapacity = p_initialSize;

        m_table = new HashTableElement[m_elementCapacity];
        m_list = new ArrayList<>();
    }

    /**
     * Returns the size.
     *
     * @return the number of entries in the hash table
     */
    public int size() {
        return m_count;
    }

    /**
     * Returns the capacity.
     *
     * @return the capacity
     */
    public int capacity() {
        return m_elementCapacity;
    }

    /**
     * Returns whether this hash table is empty or not.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return m_count == 0;
    }

    /**
     * Returns whether this hash table is full or not.
     *
     * @return true if full, false otherwise
     */
    public boolean isFull() {
        return m_count >= (int) (m_elementCapacity * LOAD_FACTOR);
    }

    /**
     * Converts hash table with all entries to an ArrayList with pairs.
     *
     * @return view on ArrayList with entries as pairs of index + value
     */
    public List<HashTableElement<T>> convert() {
        int count = 0;
        for (int i = 0; i < m_elementCapacity; i++) {
            HashTableElement<T> element = m_table[i];
            if (element != null) {
                if (m_list.size() > count) {
                    m_list.set(count, element);
                } else {
                    m_list.add(element);
                }
                count++;
            }
        }

        return m_list.subList(0, m_count);
    }

    /**
     * Returns the value to which the specified key is mapped in GenericHashTable.
     *
     * @param p_key
     *         the searched key
     * @return the value to which the key is mapped in GenericHashTable
     */
    public T get(final int p_key) {
        T ret = null;
        HashTableElement<T> iter;
        int index;

        index = (HashFunctionCollection.hash(p_key) & 0x7FFFFFFF) % m_elementCapacity;

        iter = m_table[index];
        while (iter != null) {
            if (iter.getKey() == p_key) {
                ret = iter.getValue();
                break;
            }
            index = (index + 1) % m_elementCapacity;
            iter = m_table[index];
        }

        return ret;
    }

    /**
     * Maps the given key-value tuple in GenericHashTable.
     *
     * @param p_key
     *         the key
     */
    public void put(final int p_key, final T p_value) {
        HashTableElement<T> iter;
        int index;

        index = (HashFunctionCollection.hash(p_key) & 0x7FFFFFFF) % m_elementCapacity;

        iter = m_table[index];
        while (iter != null) {
            if (iter.getKey() == p_key) {
                // Replace value in entry
                iter.setValue(p_value);
                return;
            }
            index = (index + 1) % m_elementCapacity;
            iter = m_table[index];
        }

        // Add new entry
        m_table[index] = new HashTableElement<T>(p_key, p_value);
        if (++m_count >= m_elementCapacity * LOAD_FACTOR) {
            rehash();
        }
    }

    /**
     * Clears the GenericHashTable.
     */
    public void clear() {
        int length = m_table.length;

        /* The array and list is never truncated as the maximum number of concurrently accessed
         backup zones is rather low */

        // This is faster than Arrays.fill
        m_table[0] = null;
        for (int i = 1; i < length; i += i) {
            System.arraycopy(m_table, 0, m_table, i, length - i < i ? length - i : i);
        }

        m_count = 0;
    }

    /**
     * Increases the capacity of and internally reorganizes GenericHashTable.
     */
    private void rehash() {
        int index = 0;
        int oldCount;
        int oldElementCapacity;
        HashTableElement<T>[] oldTable;
        HashTableElement<T>[] newTable;

        LOGGER.trace("Re-hashing (count:  %d)", m_count);

        oldCount = m_count;
        oldElementCapacity = m_elementCapacity;
        oldTable = m_table;

        m_elementCapacity = m_elementCapacity * 2 + 1;

        newTable = new HashTableElement[m_elementCapacity];
        m_table = newTable;

        m_count = 0;
        while (index < oldElementCapacity) {
            HashTableElement<T> element = oldTable[index];
            if (element != null) {
                put(element.getKey(), element.getValue());
            }
            index = (index + 1) % m_elementCapacity;
        }
        m_count = oldCount;
    }
}
