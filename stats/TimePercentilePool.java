package de.hhu.bsinfo.dxutils.stats;

import java.util.ArrayList;

/**
 * (Thread safe) TimePercentile operation using a pool with per thread local TimePercentile operations
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 25.05.2018
 */
public class TimePercentilePool extends OperationPool {
    private static final int SLOT_SIZE = 100000;

    private ArrayList<long[]> m_slots = new ArrayList<>();
    private int m_index;

    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    public TimePercentilePool(final Class<?> p_class, final String p_name) {
        super(TimePercentile.class, p_class, p_name);
    }

    /**
     * Get the counter of all threads summed up
     *
     * @return Counter value
     */
    public long getCounter() {
        long val = 0;

        for (AbstractOperation[] opArr : m_pool) {
            for (AbstractOperation op : opArr) {
                if (op != null) {
                    val += ((TimePercentile) op).getCounter();
                }
            }
        }

        return val;
    }

    /**
     * Get the total value of all threads summed up
     *
     * @return Total value
     */
    public long getTotalValue() {
        long val = 0;

        for (AbstractOperation[] opArr : m_pool) {
            for (AbstractOperation op : opArr) {
                if (op != null) {
                    val += ((TimePercentile) op).getTotalValue();
                }
            }
        }

        return val;
    }

    /**
     * Get the average value of all threads
     *
     * @return Average value
     */
    public long getAvg() {
        long counter = getCounter();

        return counter == 0 ? 0 : getTotalValue() / counter;
    }

    /**
     * Get the average value
     *
     * @param p_prefix
     *         Prefix to apply
     */
    public double getAvg(final Time.Prefix p_prefix) {
        return getAvg() / (double) Value.MS_PREFIX_TABLE[Value.Base.B_10.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the min value
     *
     * @return Min value
     */
    public long getMin() {
        long min = Long.MAX_VALUE;

        for (AbstractOperation[] opArr : m_pool) {
            for (AbstractOperation op : opArr) {
                if (op != null) {
                    long val = ((TimePercentile) op).getMin();

                    if (val < min) {
                        min = val;
                    }
                }
            }
        }

        return min;
    }

    /**
     * Get the min value
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Min value scaled to specified prefix
     */
    public double getMin(final Time.Prefix p_prefix) {
        return getMin() / (double) Value.MS_PREFIX_TABLE[Value.Base.B_10.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Get the max value
     *
     * @return Max value
     */
    public long getMax() {
        long max = Long.MIN_VALUE;

        for (AbstractOperation[] opArr : m_pool) {
            for (AbstractOperation op : opArr) {
                if (op != null) {
                    long val = ((TimePercentile) op).getMax();

                    if (val > max) {
                        max = val;
                    }
                }
            }
        }

        return max;
    }

    /**
     * Get the max value
     *
     * @param p_prefix
     *         Prefix to apply
     * @return Max value scaled to specified prefix
     */
    public double getMax(final Time.Prefix p_prefix) {
        return getMax() / (double) Value.MS_PREFIX_TABLE[Value.Base.B_10.ordinal()][p_prefix.ordinal()];
    }

    /**
     * Sort all registered values (ascending). Call this before getting any percentile values
     * to update the internal state.
     */
    public void sortValues() {
        // in order to evaluate the percentile across multiple threads, we have to gather the data from all threads
        // then sort everything in a single list (depending on how much data is registers, this might require
        // a lot of additional memory and processing time)
        m_slots.clear();
        m_index = 0;

        for (AbstractOperation[] opArr : m_pool) {
            for (AbstractOperation op : opArr) {
                if (op != null) {
                    TimePercentile percOp = (TimePercentile) op;

                    for (long l = 0; l < (percOp.m_percentile.m_slots.size() - 1) * ValuePercentile.SLOT_SIZE +
                            percOp.m_percentile.m_index - 1; l++) {
                        long elem = percOp.m_percentile.m_slots.get((int) (l / ValuePercentile.SLOT_SIZE))[(int) (l %
                                ValuePercentile.SLOT_SIZE)];

                        long[] arr;

                        if (m_index % SLOT_SIZE == 0) {
                            arr = new long[SLOT_SIZE];
                            m_slots.add(arr);
                            m_index = 0;
                        } else {
                            arr = m_slots.get(m_slots.size() - 1);
                        }

                        arr[m_index++] = elem;
                    }
                }
            }
        }

        if (m_slots.isEmpty()) {
            return;
        }

        quickSort(0, (m_slots.size() - 1) * SLOT_SIZE + m_index - 1);
    }

    /**
     * Get the score for the Xth percentile of all recorded values
     *
     * @param p_percentile
     *         the percentile
     * @return Score of specified percentile
     */
    public long getPercentileScore(final float p_percentile) {
        if (p_percentile <= 0.0 || p_percentile >= 1.0) {
            throw new IllegalArgumentException("Percentile must be in (0.0, 1.0)!");
        }

        if (m_slots.isEmpty()) {
            return 0;
        }

        int size = (m_slots.size() - 1) * SLOT_SIZE + m_index;
        long index = (long) Math.ceil(p_percentile * size) - 1;

        return m_slots.get((int) (index / SLOT_SIZE))[(int) (index % SLOT_SIZE)];
    }

    /**
     * Get the score for the Xth percentile of all recorded values
     *
     * @param p_percentile
     *         the percentile
     * @param p_prefix
     *         Prefix to apply to score
     * @return Score of specified percentile scaled to specified prefix
     */
    public double getPercentileScore(final float p_percentile, final Time.Prefix p_prefix) {
        return getPercentileScore(p_percentile) / Time.MS_PREFIX_TABLE[p_prefix.ordinal()];
    }

    /**
     * Record a single value
     *
     * @param p_valueNs
     *         Time value in ns to record (separate from start/stop)
     */
    public void record(final long p_valueNs) {
        ((TimePercentile) getThreadLocalValue()).record(p_valueNs);
    }

    /**
     * Quicksort implementation.
     *
     * @param p_lowerIndex
     *         the lower index
     * @param p_higherIndex
     *         the higher index
     */
    private void quickSort(int p_lowerIndex, int p_higherIndex) {
        int i = p_lowerIndex;
        int j = p_higherIndex;
        int index = p_lowerIndex + (p_higherIndex - p_lowerIndex) / 2;
        long pivot = m_slots.get(index / SLOT_SIZE)[index % SLOT_SIZE];

        while (i <= j) {
            while (m_slots.get(i / SLOT_SIZE)[i % SLOT_SIZE] < pivot) {
                i++;
            }

            while (m_slots.get(j / SLOT_SIZE)[j % SLOT_SIZE] > pivot) {
                j--;
            }

            if (i <= j) {
                long temp = m_slots.get(i / SLOT_SIZE)[i % SLOT_SIZE];
                m_slots.get(i / SLOT_SIZE)[i % SLOT_SIZE] = m_slots.get(j / SLOT_SIZE)[j % SLOT_SIZE];
                m_slots.get(j / SLOT_SIZE)[j % SLOT_SIZE] = temp;

                i++;
                j--;
            }
        }

        if (p_lowerIndex < j) {
            quickSort(p_lowerIndex, j);
        }

        if (i < p_higherIndex) {
            quickSort(i, p_higherIndex);
        }
    }
}
