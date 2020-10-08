package dev.vacant.pricebasket;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Package of all the special offers available at our store.
 * <p>
 * The special offers are read from the corresponding data file ({@value
 * DATA_FILE}). Each offer is specified through a rule that dictates when the
 * offer is applicable as well as what it includes as a discount. Parsers for
 * each rule are registered through Java's {@link ServiceLoader service provider}
 * mechanism.
 */
public class OfferPackage {
    private static final String DATA_FILE = "offers.list";

    private final Catalog catalog;
    private final List<OfferRule> offers;
    private final Iterable<OfferParser> parsers;

    /**
     * Creates a new special offer package for the item catalog supplied, using
     * the data reader supplied for reading the corresponding data file.
     *
     * @param catalog The backing item catalog.
     * @param reader  The data reader used for reading the data file.
     * @param parsers The parsers available in the system.
     * @throws IOException If an I/O error occurs.
     */
    public OfferPackage(Catalog catalog,
                        DataReader reader,
                        Iterable<OfferParser> parsers
    ) throws IOException {
        requireNonNull(reader, "reader is required");
        this.catalog = requireNonNull(catalog, "catalog is required");
        this.parsers = requireNonNull(parsers, "parsers is required");

        try (LineNumberReader fileReader = reader.newLineNumberReader(DATA_FILE)) {
            offers = parseDataFile(fileReader);
        }
    }

    /**
     * Creates a new special offer package for the item catalog supplied, using
     * the default data reader for reading the corresponding data file.
     *
     * @param catalog The backing item catalog.
     * @throws IOException If an I/O error occurs.
     */
    public OfferPackage(Catalog catalog) throws IOException {
        this(catalog, new DataReader(), ServiceLoader.load(OfferParser.class));
    }

    /**
     * Retrieves the list of available offers.
     *
     * @return All available offers.
     */
    public List<OfferRule> getAvailableOffers() {
        return Collections.unmodifiableList(offers);
    }

    /**
     * Retrieves the list of offers applicable to a given item basket.
     *
     * @param basket The item basket.
     * @return The list of offer that are applicable to the given basket.
     */
    public List<OfferRule> getApplicableOffers(Basket basket) {
        return offers.stream()
                .filter(it -> it.isApplicable(basket))
                .collect(Collectors.toList());
    }

    private List<OfferRule> parseDataFile(LineNumberReader reader) throws IOException {
        List<OfferRule> offers = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#')
                continue;

            OfferRule offer = null;
            for (OfferParser parser : parsers) {
                OfferRule parsedOffer = parser.parseRule(catalog, line);
                if (parsedOffer == null)
                    continue;
                if (offer != null) {
                    int lineNumber = reader.getLineNumber();
                    String message = "Ambiguous offer rule: " + line;
                    throw new CorruptDataFileException(lineNumber, message);
                }
                offer = parsedOffer;
            }

            if (offer == null) {
                int lineNumber = reader.getLineNumber();
                String message = "Unsupported offer rule: " + line;
                throw new CorruptDataFileException(lineNumber, message);
            }

            offers.add(offer);
        }

        return offers;
    }
}
