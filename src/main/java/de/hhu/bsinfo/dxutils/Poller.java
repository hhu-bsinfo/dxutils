package de.hhu.bsinfo.dxutils;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Poller {

    /**
     * Polls the specified supplier function until the return value is not null.
     *
     * @param p_supplier The supplier function.
     * @param p_interval The poll interval.
     * @param p_timeUnit The interval's time unit.
     * @return The polled value.
     */
    public static <T> T blockingPoll(Supplier<T> p_supplier, long p_interval, TimeUnit p_timeUnit) {
        return blockingPoll(p_supplier, p_interval, p_timeUnit, Objects::nonNull);
    }

    /**
     * Polls the specified supplier function until the specified predicate is met.
     *
     * @param p_supplier The supplier function.
     * @param p_interval The poll interval.
     * @param p_timeUnit The interval's time unit.
     * @param p_predicate The predicate.
     * @return The polled value.
     */
    public static <T> T blockingPoll(Supplier<T> p_supplier, long p_interval, TimeUnit p_timeUnit, Predicate<T> p_predicate) {
        T value = p_supplier.get();
        if (p_predicate.test(value)) {
            return value;
        }

        do {
            LockSupport.parkNanos(p_timeUnit.toNanos(p_interval));
            value = p_supplier.get();
        } while (!p_predicate.test(value));

        return value;
    }

}
