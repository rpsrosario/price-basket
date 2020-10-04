package dev.vacant.pricebasket;

import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Catalog of all the existing items in our shop.
 * <p>
 * Every existing item must be registered in the catalog, along with its current
 * base price (in GBP). The catalog itself is read from the associated data file
 * ({@value #DATA_FILE}).
 * <p>
 * The catalog data file is a simple text file where each non-blank, non-comment
 * line is an item entry (the item name followed by its price). Every entry must
 * specify an unique item, where the item's name is case insensitive.
 */
public class Catalog {
    public static final String DATA_FILE = "catalog.list";

    private final Map<String, BigDecimal> data;

    /**
     * Creates a new catalog using the specified data reader for retrieving the
     * catalog's information.
     *
     * @param reader The data reader to use.
     * @throws IOException If an I/O error occurs.
     */
    public Catalog(DataReader reader) throws IOException {
        requireNonNull(reader, "reader is required");
        try (LineNumberReader fileReader = reader.newLineNumberReader(DATA_FILE)) {
            data = parseDataFile(fileReader);
        }
    }

    /**
     * Creates a new catalog using the default data reader for retrieving the
     * catalog's information.
     *
     * @throws IOException If an I/O error occurs.
     */
    public Catalog() throws IOException {
        this(new DataReader());
    }

    /**
     * Retrieves the names of all of the items in the catalog.
     *
     * @return A set of the names of all the items in the catalog.
     */
    public Set<String> getAllItems() {
        return data.keySet();
    }

    /**
     * Retrieves the price for the specified item, if it's available in the
     * catalog.
     *
     * @param item The name of the item to price.
     * @return The price of the item, if it exists in the catalog, otherwise
     * {@code null}.
     */
    public BigDecimal getPriceFor(String item) {
        return data.get(item.trim().toUpperCase());
    }

    @Override
    public String toString() {
        return data.toString();
    }

    private Map<String, BigDecimal> parseDataFile(LineNumberReader reader) throws IOException {
        Map<String, BigDecimal> data = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#')
                continue;

            String[] fragments = line.split("\\s+");
            if (fragments.length < 2) {
                int lineNumber = reader.getLineNumber();
                String message = "Entries must have both the item name and the price";
                throw new CorruptDataFileException(lineNumber, message);
            }

            try {
                // Ensures proper format and scale of the price
                BigDecimal price = new BigDecimal(fragments[fragments.length - 1])
                        .setScale(2, RoundingMode.UNNECESSARY);

                // Sanitize the item's name to an uppercase string with a single
                // space as word delimiter
                String item = Arrays.stream(fragments)
                        .limit(fragments.length - 1)
                        .map(String::toUpperCase)
                        .collect(Collectors.joining(" "));

                if (data.containsKey(item)) {
                    int lineNumber = reader.getLineNumber();
                    String message = "Duplicate entry found for " + item;
                    throw new CorruptDataFileException(lineNumber, message);
                }

                data.put(item, price);
            } catch (NumberFormatException | ArithmeticException cause) {
                int lineNumber = reader.getLineNumber();
                String message = "Malformed price";
                throw new CorruptDataFileException(lineNumber, message, cause);
            }
        }

        return data;
    }
}
