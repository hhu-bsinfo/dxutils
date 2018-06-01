package de.hhu.bsinfo.dxutils.stats;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OperationPool extends AbstractOperation {
    private static final int MS_BLOCK_SIZE_POOL = 100;

    private final Class<? extends AbstractOperation> m_operationClass;
    private final Constructor<? extends AbstractOperation> m_operationClassConstructor;
    private final Lock m_poolLock = new ReentrantLock(false);
    private final AtomicInteger m_numberEntries = new AtomicInteger(0);

    protected final ArrayList<AbstractOperation[]> m_pool = new ArrayList<>();

    public OperationPool(final Class<? extends AbstractOperation> p_operationClass, final Class<?> p_class,
            final String p_name) {
        super(p_class, p_name);

        m_operationClass = p_operationClass;

        try {
            m_operationClassConstructor = m_operationClass.getConstructor(Class.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
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
        return createInstance(m_class, "dummy").generateCSVHeader(p_delim);
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
     * Get the TimePercentile object for the current thread
     */
    protected AbstractOperation getThreadLocalValue() {
        long threadId = Thread.currentThread().getId();

        if (threadId >= m_pool.size() * MS_BLOCK_SIZE_POOL) {
            m_poolLock.lock();

            while (threadId >= m_pool.size() * MS_BLOCK_SIZE_POOL) {
                m_pool.add(new AbstractOperation[MS_BLOCK_SIZE_POOL]);
            }

            m_poolLock.unlock();
        }

        AbstractOperation value = m_pool.get((int) (threadId / MS_BLOCK_SIZE_POOL))[(int) (threadId %
                MS_BLOCK_SIZE_POOL)];

        if (value == null) {
            value = createInstance(m_class, m_name + '-' + Thread.currentThread().getId() + '-' +
                    Thread.currentThread().getName());
            m_pool.get((int) (threadId / MS_BLOCK_SIZE_POOL))[(int) (threadId % MS_BLOCK_SIZE_POOL)] = value;
            m_numberEntries.incrementAndGet();
        }

        return value;
    }

    private AbstractOperation createInstance(final Class<?> p_class, final String p_name) {
        try {
            return m_operationClassConstructor.newInstance(p_class, p_name);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
