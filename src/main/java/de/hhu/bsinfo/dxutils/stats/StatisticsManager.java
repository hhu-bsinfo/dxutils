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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manager for statistics (operations). Ensure that all operations you created are registered with this manager. The
 * manager handles (periodic) printing to a PrintStream (e.g. System.out) of the current state of all operations
 * either as debug output or CSV formatted tables.
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public final class StatisticsManager extends Thread {
    private static StatisticsManager ms_instance;

    private int m_printIntervalMs;
    private ReentrantLock m_lock = new ReentrantLock(false);
    private Condition m_condition = m_lock.newCondition();

    private ReentrantLock m_opsLock = new ReentrantLock(false);
    private Map<Class<?>, ArrayList<AbstractOperation>> m_operationsByClasses = new HashMap<>();

    /**
     * Get the StatisticsManager singleton
     */
    public static StatisticsManager get() {
        if (ms_instance == null) {
            ms_instance = new StatisticsManager();
        }

        return ms_instance;
    }

    /**
     * Stop periodic printing (if active)
     * Same as setPrintInterval(0)
     */
    public void stopPeriodicPrinting() {
        setPrintInterval(0);
    }

    /**
     * Set the print interval for a dedicated thread that prints the current state of all operations to stdout
     *
     * @param p_intervalMs
     *         Interval in ms for printing (0 to disable)
     */
    public void setPrintInterval(final int p_intervalMs) {
        m_printIntervalMs = p_intervalMs;

        if (m_printIntervalMs > 0) {
            m_lock.lock();

            m_condition.signalAll();

            m_lock.unlock();
        }
    }

    /**
     * Returns a list of registered operation for a certain class
     * @param p_class Class
     * @return list of registered operations
     */
    public ArrayList<AbstractOperation> getClassStatistics(final Class<?> p_class) {
        return m_operationsByClasses.get(p_class);
    }

    /**
     * Returns all registered operations
     * @return map of registered operations (for each class)
     */
    public Map<Class<?>, ArrayList<AbstractOperation>> getAllStatistics() {
        return m_operationsByClasses;
    }

    /**
     * Register a new operation. This must be called for every operation to include it in all outputs the manager
     * is handling (e.g. periodic printing, printing to file)
     *
     * @param p_class
     *         Class the operation was created in
     * @param p_operation
     *         The operation to register
     */
    public void registerOperation(final Class<?> p_class, final AbstractOperation p_operation) {
        m_opsLock.lock();

        ArrayList<AbstractOperation> list = m_operationsByClasses.putIfAbsent(p_class,
                new ArrayList<AbstractOperation>());

        if (list == null) {
            list = m_operationsByClasses.get(p_class);
        }

        boolean found = false;

        for (AbstractOperation op : list) {
            if (op.equals(p_operation)) {
                found = true;
                break;
            }
        }

        if (!found) {
            list.add(p_operation);
        }

        m_opsLock.unlock();
    }

    /**
     * De-register a operation
     *
     * @param p_class
     *         Class the operation was created in
     * @param p_operation
     *         The operation to remove
     */
    public void deregisterOperation(final Class<?> p_class, final AbstractOperation p_operation) {
        m_opsLock.lock();

        ArrayList<AbstractOperation> list = m_operationsByClasses.get(p_class);

        if (list != null) {
            list.remove(p_operation);
        }

        m_opsLock.unlock();
    }

    /**
     * Print all registered statistics operations to a stream
     *
     * @param p_stream
     *         Stream to print to (e.g. System.out)
     */
    public void printStatistics(final PrintStream p_stream) {
        printStatistics(p_stream, false);
    }

    /**
     * Print all registered statistics operations to a stream
     *
     * @param p_stream
     *         Stream to print to (e.g. System.out)
     * @param p_extended
     *         Set to true to tell the manager to print statistics that also
     *         require some processing/pre-calculation (e.g. percentile).
     */
    public void printStatistics(final PrintStream p_stream, final boolean p_extended) {
        StringBuilder builder = new StringBuilder();

        builder.append("\n================================================= Statistics ============================" +
                "=====================\n");

        m_opsLock.lock();

        for (Map.Entry<Class<?>, ArrayList<AbstractOperation>> entry : m_operationsByClasses.entrySet()) {
            builder.append(">>> ");
            builder.append(entry.getKey().getSimpleName());

            for (AbstractOperation op : entry.getValue()) {
                builder.append("\n  ");
                builder.append(op.getOperationNameSimple());
                builder.append(": ");
                builder.append('\n');

                String data = op.dataToString("    ", p_extended);

                if (data.isEmpty()) {
                    builder.append("    **** None ****");
                } else {
                    builder.append(data);
                }
            }

            builder.append('\n');
        }

        m_opsLock.unlock();

        builder.append("============================================================================================" +
                "==================\n");

        p_stream.print(builder);
    }

    /**
     * Print all registered statistics operations to a stream (formated as CSV tables)
     *
     * @param p_stream
     *         Stream to print to (e.g. System.out)
     */
    public void printStatisticTables(final PrintStream p_stream) {
        StringBuilder builder = new StringBuilder();

        builder.append("\n================================================= Statistics (CSV) ========================" +
                "=========================\n");

        m_opsLock.lock();

        builder.append(">>> Value\n");
        printStatisticTablesOf(builder, Value.class);
        builder.append("\n\n");

        builder.append(">>> ValuePool\n");
        printStatisticTablesOf(builder, ValuePool.class);
        builder.append("\n\n");

        builder.append(">>> Time\n");
        printStatisticTablesOf(builder, Time.class);
        builder.append("\n\n");

        builder.append(">>> TimePool\n");
        printStatisticTablesOf(builder, TimePool.class);
        builder.append("\n\n");

        builder.append(">>> Throughput\n");
        printStatisticTablesOf(builder, Throughput.class);
        builder.append("\n\n");

        builder.append(">>> Ratio\n");
        printStatisticTablesOf(builder, Ratio.class);
        builder.append('\n');

        builder.append(">>> TimePercentile\n");
        printStatisticTablesOf(builder, TimePercentile.class);
        builder.append('\n');

        m_opsLock.unlock();

        builder.append("============================================================================================" +
                "==================\n");

        p_stream.print(builder);
    }

    @Override
    public void run() {
        while (true) {
            if (m_printIntervalMs > 0) {
                printStatistics(System.out);

                try {
                    Thread.sleep(m_printIntervalMs);
                } catch (InterruptedException ignored) {

                }
            } else {
                m_lock.lock();

                try {
                    m_condition.await();
                } catch (InterruptedException ignored) {

                } finally {
                    m_lock.unlock();
                }
            }
        }
    }

    /**
     * Hidden constructor (singleton)
     */
    private StatisticsManager() {
        super("StatisticsManager");

        start();
    }

    /**
     * Print the statistics tables of a specific type (e.g. Value, Time)
     *
     * @param p_builder
     *         Builder to append the output to
     * @param p_class
     *         Class of operation to append to builder
     */
    private void printStatisticTablesOf(final StringBuilder p_builder,
            final Class<? extends AbstractOperation> p_class) {
        boolean first = true;

        for (Map.Entry<Class<?>, ArrayList<AbstractOperation>> entry : m_operationsByClasses.entrySet()) {
            for (AbstractOperation op : entry.getValue()) {
                if (op.getClass().equals(p_class)) {
                    // Add header
                    if (first) {
                        first = false;

                        p_builder.append("# ");
                        p_builder.append(op.generateCSVHeader(';'));
                    }

                    String csv = op.toCSV(';');

                    if (!csv.isEmpty()) {
                        p_builder.append('\n');
                        p_builder.append(op.toCSV(';'));
                    }
                }
            }
        }
    }
}
