package de.hhu.bsinfo.dxutils;

import de.hhu.bsinfo.dxutils.shell.CommandRunner;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Determines if the specified file lies within a network mount.
     *
     * @param p_file The file to check.
     * @return True, if the file lies within a network mount; false else
     */
    public static boolean isNetworkMount(final @NotNull File p_file) {
        try {
            return CommandRunner.execute("stat -f -c %T " + p_file.getAbsolutePath()).equals("nfs");
        } catch (IOException | InterruptedException p_e) {
            p_e.printStackTrace();
            return false;
        }
    }
}
