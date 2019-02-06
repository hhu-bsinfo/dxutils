package de.hhu.bsinfo.dxutils.serialization;

public class ClassUtil {

    /**
     * Creates an instance of the specified class using its default constructor.
     *
     * @param p_class The class to instantiate.
     * @param <T> The class type.
     * @return An instance of the specified class.
     */
    public static <T> T createInstance(final String p_class) {
        try {
            Class clazz = Class.forName(p_class);
            return (T) clazz.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            return null;
        }
    }
}
