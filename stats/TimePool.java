/*
 * Copyright (C) 2018 Heinrich-Heine-Universitaet Duesseldorf, Institute of Computer Science, Department Operating Systems
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package de.hhu.bsinfo.dxutils.stats;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * (Thread safe) time operation using a pool with per thread local time operations
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public class TimePool extends AbstractOperation {
    private static final int MS_BLOCK_SIZE_POOL = 100;

    private Time[][] m_pool = new Time[100][0];
    private Lock m_poolLock = new ReentrantLock(false);
    private int m_poolBlockPos;
    private AtomicInteger m_numberEntries = new AtomicInteger(0);

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    public TimePool(final Class<?> p_class, final String p_name) {
        super(p_class, p_name);
    }

    /**
     * Start time measurement
     */
    public void start() {
        getThreadLocalTime().start();
    }

    /**
     * Stop time measurement
     */
    public void stop() {
        getThreadLocalTime().stop();
    }

    @Override
    public String dataToString(final String p_indent) {
        // TODO limit if more than e.g. 10 threads -> parameter

        StringBuilder builder = new StringBuilder();

        int entries = m_numberEntries.get();

        for (int i = 0; i < m_poolBlockPos; i++) {
            for (int j = 0; j < m_pool[i].length; j++) {
                if (m_pool[i][j] != null) {
                    builder.append(p_indent);
                    builder.append("id ");
                    builder.append((i + 1) * j);
                    builder.append(": ");
                    builder.append(m_pool[i][j].dataToString(""));

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
        return new Time(m_class, "dummy").generateCSVHeader(p_delim);
    }

    @Override
    public String toCSV(final char p_delim) {
        StringBuilder builder = new StringBuilder();

        int entries = m_numberEntries.get();

        for (int i = 0; i < m_poolBlockPos; i++) {
            for (int j = 0; j < m_pool[i].length; j++) {
                if (m_pool[i][j] != null) {
                    builder.append(m_pool[i][j].toCSV(p_delim));

                    if (--entries > 0) {
                        builder.append('\n');
                    }
                }
            }
        }

        return builder.toString();
    }

    /**
     * Get the time object for the current thread
     */
    private Time getThreadLocalTime() {
        long threadId = Thread.currentThread().getId();

        if (threadId >= m_poolBlockPos * MS_BLOCK_SIZE_POOL) {
            m_poolLock.lock();

            while (threadId >= m_poolBlockPos * MS_BLOCK_SIZE_POOL) {
                m_pool[m_poolBlockPos++] = new Time[MS_BLOCK_SIZE_POOL];
            }

            m_poolLock.unlock();
        }

        Time time = m_pool[(int) (threadId / MS_BLOCK_SIZE_POOL)][(int) (threadId % MS_BLOCK_SIZE_POOL)];

        if (time == null) {
            time = new Time(m_class, m_name + '-' + Thread.currentThread().getId() + '-' +
                    Thread.currentThread().getName());
            m_pool[(int) (threadId / MS_BLOCK_SIZE_POOL)][(int) (threadId % MS_BLOCK_SIZE_POOL)] = time;
            m_numberEntries.incrementAndGet();
        }

        return time;
    }
}
