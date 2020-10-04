package dev.vacant.pricebasket;

import java.io.IOException;

/**
 * A data file is corrupted.
 * <p>
 * The meaning of a data file being corrupted is specific to the data file being
 * processed, however it indicates that the file is not in the intended format
 * and therfore cannot be properly processed.
 */
public class CorruptDataFileException extends IOException {
    /**
     * Creates a new exception with the information supplied.
     *
     * @param lineNumber The line number in which the file is corrupt.
     * @param message    The descriptive message of the corruption.
     */
    public CorruptDataFileException(int lineNumber, String message) {
        super(formatMessage(lineNumber, message));
    }

    /**
     * Creates a new exception with the information supplied and wrapping the
     * underlying error.
     *
     * @param lineNumber The line number in which the file is corrupt.
     * @param message    The descriptive message of the corruption.
     * @param cause      The underlying error being wrapped.
     */
    public CorruptDataFileException(int lineNumber, String message, Throwable cause) {
        super(formatMessage(lineNumber, message), cause);
    }

    private static String formatMessage(int lineNumber, String message) {
        return message + " (line " + lineNumber + ")";
    }
}
