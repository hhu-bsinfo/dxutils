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

package de.hhu.bsinfo.dxutils.stats;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * (Thread safe) throughput operation using a pool with per thread local throughput operations
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public class ThroughputPool extends AbstractOperation {
    private static final int MS_BLOCK_SIZE_POOL = 100;

    private final Value.Base m_base;
    private ArrayList<Throughput[]> m_pool = new ArrayList<>();
    private Lock m_poolLock = new ReentrantLock(false);
    private AtomicInteger m_numberEntries = new AtomicInteger(0);

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     * @param p_base
     *         Base for the value
     */
    public ThroughputPool(final Class<?> p_class, final String p_name, final Value.Base p_base) {
        super(p_class, p_name);

        m_base = p_base;
    }

    /**
     * Start throughput measurement. Ensure a call to stop() is following
     */
    public void start() {
        getThreadLocalValue().start();
    }

    /**
     * Start throughput measurement. Ensure a call to stop() is following
     *
     * @param p_val
     *         Value to add (e.g. data processed)
     */
    public void start(final long p_val) {
        getThreadLocalValue().start(p_val);
    }

    /**
     * Add a value, e.g. data processed
     *
     * @param p_val
     *         Value to add
     */
    public void add(final long p_val) {
        getThreadLocalValue().add(p_val);
    }

    /**
     * Stop throughput measurement. Ensure a call to start is preceding
     */
    public void stop() {
        getThreadLocalValue().stop();
    }

    /**
     * Stop throughput measurement. Ensure a call to start is preceding
     *
     * @param p_val
     *         Value to add (e.g. data processed)
     */
    public void stop(final long p_val) {
        getThreadLocalValue().stop(p_val);
    }

    @Override
    public String dataToString(final String p_indent, final boolean p_extended) {
        // TODO limit if more than e.g. 10 threads -> parameter

        StringBuilder builder = new StringBuilder();

        int entries = m_numberEntries.get();

        for (int i = 0; i < m_pool.size(); i++) {
            for (int j = 0; j < m_pool.get(i).length; j++) {
                if (m_pool.get(i)[j] != null) {
                    builder.append(p_indent);
                    builder.append("id ");
                    builder.append((i + 1) * j);
                    builder.append(": ");
                    builder.append(m_pool.get(i)[j].dataToString("", p_extended));

                    if (--entries > 0) {
                        builder.append('\n');
                    }
                }
            }
        }

        return builder.toString();
    }

    @Override
    public String generateCSVHeader(final char p_delim) {
        return new Throughput(m_class, "dummy", m_base).generateCSVHeader(p_delim);
    }

    @Override
    public String toCSV(final char p_delim) {
        StringBuilder builder = new StringBuilder();

        int entries = m_numberEntries.get();

        for (int i = 0; i < m_pool.size(); i++) {
            for (int j = 0; j < m_pool.get(i).length; j++) {
                if (m_pool.get(i)[j] != null) {
                    builder.append(m_pool.get(i)[j].toCSV(p_delim));

                    if (--entries > 0) {
                        builder.append('\n');
                    }
                }
            }
        }

        return builder.toString();
    }

    /**
     * Get the throughput object for the current thread
     */
    private Throughput getThreadLocalValue() {
        long threadId = Thread.currentThread().getId();

        if (threadId >= m_pool.size() * MS_BLOCK_SIZE_POOL) {
            m_poolLock.lock();

            while (threadId >= m_pool.size() * MS_BLOCK_SIZE_POOL) {
                m_pool.add(new Throughput[MS_BLOCK_SIZE_POOL]);
            }

            m_poolLock.unlock();
        }

        Throughput value = m_pool.get((int) (threadId / MS_BLOCK_SIZE_POOL))[(int) (threadId % MS_BLOCK_SIZE_POOL)];

        if (value == null) {
            value = new Throughput(m_class, m_name + '-' + Thread.currentThread().getId() + '-' +
                    Thread.currentThread().getName(), m_base);
            m_pool.get((int) (threadId / MS_BLOCK_SIZE_POOL))[(int) (threadId % MS_BLOCK_SIZE_POOL)] = value;
            m_numberEntries.incrementAndGet();
        }

        return value;
    }
}
