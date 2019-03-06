package de.hhu.bsinfo.dxutils.loader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Filip Krakowski, filip.krakowski@uni-duesseldorf.de, 05.03.2019
 */
public class JarClassLoader extends URLClassLoader {

    private static final Logger LOGGER = LogManager.getFormatterLogger(JarClassLoader.class);

    private static final String JAR_SUFFIX = ".jar";

    /**
     * The base directory to search within for jar archives.
     */
    private final Path m_baseDir;

    public JarClassLoader(Path p_baseDir, ClassLoader p_parent) {
        super(new URL[0], p_parent);
        m_baseDir = p_baseDir;
        init();
    }

    public JarClassLoader(Path p_baseDir) {
        super(new URL[0]);
        m_baseDir = p_baseDir;
        init();
    }

    public JarClassLoader(Path p_baseDir, ClassLoader p_parent, URLStreamHandlerFactory p_factory) {
        super(new URL[0], p_parent, p_factory);
        m_baseDir = p_baseDir;
        init();
    }

    /**
     * Initializes this classloader instance.
     */
    protected void init() {
        try {
            Files.list(m_baseDir).forEach(this::add);
        } catch (IOException e) {
            LOGGER.warn("Couldn't load plugins", e);
        }
    }

    /**
     * Adds a new jar file to the class loader.
     *
     * @param p_path The jar file's path.
     */
    public void add(final Path p_path) {
        if (!p_path.toString().endsWith(JAR_SUFFIX)) {
            LOGGER.warn("%s is not a java archive", p_path);
            return;
        }

        try {
            addURL(p_path.toUri().toURL());
        } catch (MalformedURLException e) {
            LOGGER.warn("Could not add the specified jar file", e);
        }
    }
}
