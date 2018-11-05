package de.hhu.bsinfo.dxutils;

/**
 * A memory efficient bitfield for flagging NodeIDs
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 17.01.2017
 */
public class NodeIDBitfield {
    private byte[] m_field;

    /**
     * Constructor
     */
    public NodeIDBitfield() {
        m_field = new byte[(NodeID.MAX_ID + 1) / 8 + 1];
    }

    /**
     * Flag the field of a specific node ID
     *
     * @param p_nodeId
     *         Node ID to flag
     * @param p_state
     *         Flag to set
     * @return Returns previously set flag
     */
    public boolean set(final short p_nodeId, final boolean p_state) {
        int tmp = p_nodeId & 0xFFFF;

        boolean prev = (m_field[tmp / 8] & 0xFF & (1 << (tmp % 8))) > 0;

        if (p_state) {
            m_field[tmp / 8] = (byte) ((m_field[tmp / 8] & 0xFF) | (1 << (tmp % 8)));
        } else {
            m_field[tmp / 8] = (byte) ((m_field[tmp / 8] & 0xFF) & ~(1 << (tmp % 8)));
        }

        return prev;
    }

    /**
     * Get the flag set for a specific node ID
     *
     * @param p_nodeId
     *         Node ID to get flag of
     * @return Flag of node ID specified
     */
    public boolean get(final short p_nodeId) {
        int tmp = p_nodeId & 0xFFFF;

        return (m_field[tmp / 8] & 0xFF & (1 << (tmp % 8))) > 0;
    }
}
