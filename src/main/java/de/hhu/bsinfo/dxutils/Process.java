package de.hhu.bsinfo.dxutils;

import java.lang.management.ManagementFactory;

/**
 * Get the PID of the current process
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 13.07.2018
 */
public final class Process {
    /**
     * Private constructor, utility class.
     */
    private Process() {

    }

    /**
     * Get the PID of the currently running process
     *
     * @return PID of current process
     */
    public static int getCurrentPID() {
        return Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    }
}
