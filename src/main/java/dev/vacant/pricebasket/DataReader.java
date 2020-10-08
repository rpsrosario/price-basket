package dev.vacant.pricebasket;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * Utility for reading data files.
 * <p>
 * Default data files are packaged with the application, however the user can
 * override this by providing its own data files in the file system (relative to
 * the working directory). This class ensures a proper handling of the data file
 * location discovery.
 */
public class DataReader {
    private final FileSystem fileSystem;

    /**
     * Creates a new data file reader backed by the file system provided.
     *
     * @param fileSystem The file system to use for locating data files.
     */
    public DataReader(FileSystem fileSystem) {
        this.fileSystem = requireNonNull(fileSystem, "fileSystem is required");
    }

    /**
     * Creates a new data file reader backed by the default file system.
     */
    public DataReader() {
        this(FileSystems.getDefault());
    }

    /**
     * Creates a new reader (with line number support) for a data file.
     * <p>
     * This method will return a reader for the data file with the most
     * precedence that currently exists. This means that if a data file exists
     * in the backing file system then that one will be returned instead of the
     * default one packaged with the application. When a file doesn't exist in
     * the backing file system it will be created with the default contents.
     *
     * @param filePath The relative path to the data file
     * @return The line number reader for the data file.
     * @throws IOException If an I/O error occurs.
     */
    public LineNumberReader newLineNumberReader(String filePath) throws IOException {
        InputStream stream = newInputStream(filePath);
        return new LineNumberReader(new InputStreamReader(stream, UTF_8));
    }

    private InputStream newInputStream(String filePath) throws IOException {
        Path path = fileSystem.getPath(filePath);
        if (!Files.exists(path)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
                 OutputStream out = Files.newOutputStream(path)) {
                // Only happens if the paths/resources are misconfigured
                assert in != null;

                byte[] buffer = new byte[32 * 1024];
                int count;
                while ((count = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, count);
                }
            }
        }
        return Files.newInputStream(path);
    }
}
