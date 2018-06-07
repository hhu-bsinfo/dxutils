package de.hhu.bsinfo.dxutils.eval;

import de.hhu.bsinfo.dxutils.stats.ValuePercentile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Do multiple time measures in a Multi-Thread environment and write all
 * collected data to a specific folder.
 *
 * @author Florian Hucke (florian.hucke@hhu.de) on 06.03.18
 * @projectname dxram-memory
 */
@SuppressWarnings("SameParameterValue")
public final class MeasurementHelper {

    private final Measurement[] measurements;
    private final String basePath;
    private static boolean collectMiss = false;

    private long round = 1;

    /**
     * Constructor
     *
     * @param p_folder Folder to save the measurement results
     * @param p_descLine Description of the measurement
     * @param p_measurements Names of the measurements
     * @param p_collectMiss Collect data of failed operations
     */
    public MeasurementHelper(final String p_folder, final String p_baseFilename, final String p_descLine, final boolean p_collectMiss,
                             final String... p_measurements) {
        //folder exist checked by calling method
        basePath = (p_folder.endsWith("/")?p_folder:(p_folder + "/")) + p_baseFilename;

        try {
            //create dir
            Files.createDirectories(Paths.get(p_folder));

            //create description file
            FileChannel fc = FileChannel.open(Paths.get(basePath + ".desc"), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            fc.write(ByteBuffer.wrap(p_descLine.getBytes()));
            fc.close();

        } catch (IOException ignored) {}

        collectMiss = p_collectMiss;

        measurements = new Measurement[p_measurements.length];
        for (int i = 0; i < p_measurements.length; i++) {
            measurements[i] = new Measurement(p_measurements[i]);
        }
    }

    /**
     * Get a Measurement instance
     *
     * @param name Name of the measurement
     * @return The measurement instance of null if no suitable instance was found
     */
    public final Measurement getMeasurement(final String name) {
        for (Measurement m:measurements)
            if (m.name.equals(name))
                return m;

        return null;
    }

    /**
     * Start a new round.
     */
    public final void newRound() {
        for (Measurement m : measurements){
            m.resetMeasurement();
        }
        round++;
    }

    /**
     * Write collected stats to a file
     *
     * @throws IOException The FileChannel can throw a IOException
     */
    public final void writeStats(final String fileExtension, final char delim) throws IOException {
        assert measurements != null;

        FileChannel fc;
        if(round == 1) {
            fc = FileChannel.open(Paths.get(basePath + "_" + fileExtension + ".log"),
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

            fc.write(ByteBuffer.wrap(measurements[0].csvHeader(delim).getBytes()));
        } else {
            fc = FileChannel.open(Paths.get(basePath + "_" + fileExtension + ".log"),
                    StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        }

        fc.write(ByteBuffer.wrap((getCSVStats(round, delim)).getBytes()));
        fc.close();
    }

    /**
     * Get all stats of the measurements
     *
     * @param delim
     *          The separator
     * @return
 *              A string of all measurements
     */
    private String getCSVStats(final long round, final char delim) {
        StringBuilder out = new StringBuilder();
        String tmp;

        for (Measurement m:measurements){
            tmp = m.getExecutedSuccessfullyStats(delim);
            if(!tmp.isEmpty())
                out.append(round).append(delim).append(tmp);
            if (collectMiss){
                tmp = m.getNotExecutedSuccessfullyStats(delim);
                if(!tmp.isEmpty())
                    out.append(round).append(delim).append(tmp);
            }
        }

        return out.toString();
    }


    /**
     * Single measurement
     *
     * @author Florian Hucke (florian.hucke@hhu.de) on 06.03.18
     */
    public final class Measurement{
        private final String name;

        private final ValuePercentile percentileHit;
        private final ValuePercentile percentileMiss;

        private AtomicLong hit;
        private AtomicLong hit_accu_time;
        private AtomicLong hit_best;
        private AtomicLong hit_worst;

        private AtomicLong miss;
        private AtomicLong miss_accu_time;
        private AtomicLong miss_best;
        private AtomicLong miss_worst;

        /**
         * Constructor
         *
         * @param p_name
         *          Name of the measure
         */
        Measurement(final String p_name) {
            name = p_name;

            percentileHit = new ValuePercentile(MeasurementHelper.Measurement.class, name);
            percentileMiss = new ValuePercentile(MeasurementHelper.Measurement.class, name);

            init();
        }

        /**
         * Reset all variables to their initial state
         */
        void resetMeasurement() {
            percentileHit.deleteValues();
            percentileMiss.deleteValues();

            init();
        }

        /**
         * Initialize all variables
         */
        private void init() {
            hit = new AtomicLong(0);
            hit_accu_time = new AtomicLong(0);
            hit_best = new AtomicLong(Long.MAX_VALUE);
            hit_worst = new AtomicLong(0);

            if (collectMiss) {
                miss = new AtomicLong(0);
                miss_accu_time = new AtomicLong(0);
                miss_best = new AtomicLong(Long.MAX_VALUE);
                miss_worst = new AtomicLong(0);
            }

        }

        /**
         * Add a measured Time.
         *
         * @param ok Check if a hit or a miss occurred
         * @param deltaTime Time delta
         */
        public final void addTime(final boolean ok, final long deltaTime) {
            if(ok){
                hit.incrementAndGet();
                hit_accu_time.getAndAdd(deltaTime);
                hit_best.accumulateAndGet(deltaTime, Math::min);
                hit_worst.accumulateAndGet(deltaTime, Math::max);

                synchronized (percentileHit){
                    percentileHit.record(deltaTime);
                }
            } else if (collectMiss){
                miss.incrementAndGet();
                miss_accu_time.getAndAdd(deltaTime);
                miss_best.accumulateAndGet(deltaTime, Math::min);
                miss_worst.accumulateAndGet(deltaTime, Math::max);

                synchronized (percentileMiss) {
                    percentileMiss.record(deltaTime);
                }
            }
        }

        @Override
        public String toString() {
            return getStats();
        }

        /**
         * Create a string of all hit and miss operations with the best, the worst and the average time.
         * This information about miss operations are only showed when at least one miss operation occurred
         *
         * @return
         *          A String with stats
         */
        final String getStats(){
            char delim = ',';
            return getExecutedSuccessfullyStats(delim) +
                    ((collectMiss)?getNotExecutedSuccessfullyStats(delim):"");

        }

        /**
         * Get the statistics of successfully completed tests in CSV format
         *
         * @param delim
         *          The separator
         * @return
         *          The stats in CSV format
         */
        final String getExecutedSuccessfullyStats(final char delim) {
            StringBuilder out = new StringBuilder();
            if(hit.get() > 0) {
                out.append(name).append(delim).append(hit.get()).append(delim)
                        .append(hit_best.get()).append(delim).append(hit_worst.get())
                        .append(delim);

                out.append(hit_accu_time.get() / hit.get());

                out.append(delim).append(percentileHit.toCSV(delim)).append('\n');
            }

            return out.toString();
        }

        /**
         * Get the statistics of successfully completed tests in CSV format
         *
         * @param delim
         *          The separator
         * @return
         *          The stats in CSV format
         */
        final String getNotExecutedSuccessfullyStats(final char delim) {
            StringBuilder out = new StringBuilder();
            if(miss.get() > 0) {
                out.append(name).append("(miss)").append(delim).append(miss.get()).append(delim)
                        .append(miss_best.get()).append(delim).append(miss_worst.get())
                        .append(delim);

                out.append(miss_accu_time.get() / miss.get());

                out.append(delim).append(percentileMiss.toCSV(delim)).append('\n');
            }

            return out.toString();
        }

        /**
         * Create a CSV header.
         *
         * @param delim
         *          The separator
         * @return
         *          A header for the CSV file
         */
        private String csvHeader(final char delim){
            return "round" + delim +"name" + delim + "operation" + delim + "best" + delim + "worst" + delim + "average"
                    + delim + percentileHit.generateCSVHeader(delim) + '\n';
        }
    }
}
