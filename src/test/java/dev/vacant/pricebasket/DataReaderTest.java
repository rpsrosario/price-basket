package dev.vacant.pricebasket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@DisplayName("DataReader Unit Tests")
class DataReaderTest {
    FileSystemProvider provider;
    FileSystem fileSystem;
    Path path;

    LineNumberReader reader;

    @BeforeEach
    void setupMockedPath() {
        provider = mock(FileSystemProvider.class);
        fileSystem = mock(FileSystem.class);
        path = mock(Path.class);

        when(fileSystem.getPath(anyString())).thenReturn(path);
        when(fileSystem.provider()).thenReturn(provider);
        when(path.getFileSystem()).thenReturn(fileSystem);
    }

    @AfterEach
    void cleanupInputStream() throws IOException {
        if (reader != null)
            reader.close();
    }

    @Test
    @DisplayName("Read file from file system when path exists")
    void testExistingPath() throws IOException {
        byte[] bytes = "File System Contents".getBytes(UTF_8);
        final InputStream stream = new ByteArrayInputStream(bytes);
        when(provider.newInputStream(path)).thenReturn(stream);

        assertTrue(Files.exists(path), "Failed to setup existing path");

        final DataReader dataReader = new DataReader(fileSystem);
        reader = dataReader.newLineNumberReader("testfiles/datafile");

        assertEquals("File System Contents", reader.readLine());
    }

    @Test
    @DisplayName("When path doesn't exist create it with default contents")
    void testNonExistingPath() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(provider.newOutputStream(path)).thenReturn(out);
        when(provider.newInputStream(path))
                .thenAnswer(invocation -> new ByteArrayInputStream(out.toByteArray()));

        doThrow(IOException.class).when(provider).readAttributes(eq(path), any(Class.class));
        doThrow(IOException.class).when(provider).readAttributes(eq(path), anyString());
        doThrow(IOException.class).when(provider).checkAccess(path);

        assertFalse(Files.exists(path), "Failed to setup non-existing path");

        final DataReader dataReader = new DataReader(fileSystem);
        reader = dataReader.newLineNumberReader("testfiles/datafile");

        assertEquals("Data File Contents", reader.readLine());
        assertEquals("Data File Contents", new String(out.toByteArray(), UTF_8).trim());
    }
}
