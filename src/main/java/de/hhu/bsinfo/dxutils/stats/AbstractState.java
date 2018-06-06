package de.hhu.bsinfo.dxutils.stats;

/**
 * Base class for all statistics operations that wrap some sort of internal state
 * This allows printing any state of variables along with the "normal" statistics for debugging
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 05.03.2018
 */
public abstract class AbstractState extends AbstractOperation {
    /**
     * Constructor
     *
     * @param p_class
     *         Class that contains the operation
     * @param p_name
     *         Name for the operation
     */
    protected AbstractState(Class<?> p_class, String p_name) {
        super(p_class, p_name);
    }
}
