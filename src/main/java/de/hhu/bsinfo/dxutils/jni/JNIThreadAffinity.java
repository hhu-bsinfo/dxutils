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

package de.hhu.bsinfo.dxutils.jni;

/**
 * @author Kevin Beineke, kevin.beineke@hhu.de, 16.01.2018
 */
public final class JNIThreadAffinity {

    /**
     * Static class, private constructor.
     */
    private JNIThreadAffinity() {
    }

    /**
     * Set CPU core affinity for calling thread to given core.
     *
     * @param p_core
     *         the core to pin to.
     * @return 0 if successful, != 0 otherwise
     */
    public static native int pinToCore(final int p_core);

    /**
     * Set CPU core affinity for calling thread to given core set.
     *
     * @param p_coreSet
     *         a set of cores to pin to.
     * @return 0 if successful, != 0 otherwise
     */
    public static native int pinToCoreSet(final int[] p_coreSet);

    /**
     * Increase scheduling priority of calling thread by moving it to the realtime queue with highest priority.
     * Requires root access.
     *
     * @return 0 if successful, != 0 otherwise
     */
    public static native int moveToRealtimeQueue();
}
