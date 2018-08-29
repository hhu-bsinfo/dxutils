package de.hhu.bsinfo.dxutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Helper class for file system related tasks
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 29.08.2018
 */
public final class FileSystemUtils {
    /**
     * Private constructor, utility class.
     */
    private FileSystemUtils() {

    }

    /**
     * Delete a folder recursively
     *
     * @param p_file
     *         Folder to delete
     * @throws IOException
     *         On error
     */
    public static void deleteRecursively(final File p_file) throws IOException {
        Path path = Paths.get(p_file.getAbsolutePath());

        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
