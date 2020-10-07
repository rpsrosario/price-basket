package dev.vacant.pricebasket;

import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern LINE_FORMAT = Pattern.compile("(?<id>.*?)\\s+(?<price>\\S+)");
    private static final String DATA_FILE = "catalog.list";

    private final Map<ItemId, BigDecimal> data;

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
     * Retrieves the IDs of all of the items in the catalog.
     *
     * @return A set of the IDs of all the items in the catalog.
     */
    public Set<ItemId> getAllItems() {
        return data.keySet();
    }

    /**
     * Retrieves the price for the specified item, if it's available in the
     * catalog.
     *
     * @param itemId The ID of the item to price.
     * @return The price of the item, if it exists in the catalog, otherwise
     * {@code null}.
     */
    public BigDecimal getPriceFor(ItemId itemId) {
        return data.get(itemId);
    }

    @Override
    public String toString() {
        return data.toString();
    }

    private Map<ItemId, BigDecimal> parseDataFile(LineNumberReader reader) throws IOException {
        Map<ItemId, BigDecimal> data = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#')
                continue;

            Matcher matcher = LINE_FORMAT.matcher(line);
            if (!matcher.matches()) {
                int lineNumber = reader.getLineNumber();
                String message = "Entries must have both the item id and the price";
                throw new CorruptDataFileException(lineNumber, message);
            }

            try {
                // Ensures proper format and scale of the price
                BigDecimal price = new BigDecimal(matcher.group("price"))
                        .setScale(2, RoundingMode.UNNECESSARY);

                ItemId itemId = new ItemId(matcher.group("id"));
                if (data.containsKey(itemId)) {
                    int lineNumber = reader.getLineNumber();
                    String message = "Duplicate entry found for " + itemId;
                    throw new CorruptDataFileException(lineNumber, message);
                }

                data.put(itemId, price);
            } catch (NumberFormatException | ArithmeticException cause) {
                int lineNumber = reader.getLineNumber();
                String message = "Malformed price";
                throw new CorruptDataFileException(lineNumber, message, cause);
            }
        }

        return data;
    }
}
