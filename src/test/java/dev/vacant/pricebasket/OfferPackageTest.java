package dev.vacant.pricebasket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("OfferPackage Unit Tests")
class OfferPackageTest {

    @ParameterizedTest(name = "{index}. Empty Offer Package")
    @ValueSource(strings = {
            "", " ", "\t\t", "  \t\n\n",
            "# This is a comment",
            "  # This is a comment (not in the beginning of the line)"
    })
    void testEmptyOfferPackage(String dataContents) throws IOException {
        final Catalog catalog = mock(Catalog.class);
        final DataReader dataReader = buildMockDataReader(dataContents);
        final Iterable<OfferParser> parsers = emptyList();
        final OfferPackage offerPackage = new OfferPackage(catalog, dataReader, parsers);
        assertTrue(offerPackage.getAvailableOffers().isEmpty());
    }

    @Test
    @DisplayName("Offer Package is corrupt on unknown rule")
    void testUnkownRule() throws IOException {
        final Catalog catalog = mock(Catalog.class);
        final DataReader dataReader = buildMockDataReader("unknown rule");
        final Iterable<OfferParser> parsers = emptyList();
        assertThrows(CorruptDataFileException.class, () -> new OfferPackage(catalog, dataReader, parsers));
    }

    @Test
    @DisplayName("Offer Package is corrupt on ambiguous rule")
    void testAmbiguousRule() throws IOException {
        final Catalog catalog = mock(Catalog.class);
        final DataReader dataReader = buildMockDataReader("unknown rule");
        final Iterable<OfferParser> parsers = asList(
                new ApplicableOffer.Parser(),
                new ApplicableOffer.Parser()
        );
        assertThrows(CorruptDataFileException.class, () -> new OfferPackage(catalog, dataReader, parsers));
    }

    @Test
    @DisplayName("Only Applicable Offer is available")
    void testOnlyApplicableIsAvailable() throws IOException {
        final Catalog catalog = mock(Catalog.class);
        final DataReader dataReader = buildMockDataReader("applicable");
        final Iterable<OfferParser> parsers = singletonList(new ApplicableOffer.Parser());

        final OfferPackage offerPackage = new OfferPackage(catalog, dataReader, parsers);
        final List<OfferRule> offers = offerPackage.getAvailableOffers();

        assertEquals(1, offers.size());
        assertEquals(ApplicableOffer.class, offers.get(0).getClass());
    }

    @Test
    @DisplayName("Only Not Applicable Offer is available")
    void testOnlyNotApplicableIsAvailable() throws IOException {
        final Catalog catalog = mock(Catalog.class);
        final DataReader dataReader = buildMockDataReader("not applicable");
        final Iterable<OfferParser> parsers = singletonList(new NotApplicableOffer.Parser());

        final OfferPackage offerPackage = new OfferPackage(catalog, dataReader, parsers);
        final List<OfferRule> offers = offerPackage.getAvailableOffers();

        assertEquals(1, offers.size());
        assertEquals(NotApplicableOffer.class, offers.get(0).getClass());
    }

    @Test
    @DisplayName("Both offers are available")
    void testBothOffersAreAvailable() throws IOException {
        final Catalog catalog = mock(Catalog.class);
        final DataReader dataReader = buildMockDataReader(
                "applicable\nnot applicable"
        );
        final Iterable<OfferParser> parsers = asList(
                new ApplicableOffer.Parser(),
                new NotApplicableOffer.Parser()
        );

        final OfferPackage offerPackage = new OfferPackage(catalog, dataReader, parsers);
        final List<OfferRule> offers = offerPackage.getAvailableOffers();

        assertEquals(2, offers.size());
        assertEquals(ApplicableOffer.class, offers.get(0).getClass());
        assertEquals(NotApplicableOffer.class, offers.get(1).getClass());
    }

    @Test
    @DisplayName("Filtering Applicable Offers")
    void testFilteringApplicableOffers() throws IOException {
        final Catalog catalog = mock(Catalog.class);
        final DataReader dataReader = buildMockDataReader(
                "applicable\nnot applicable"
        );
        final Iterable<OfferParser> parsers = asList(
                new ApplicableOffer.Parser(),
                new NotApplicableOffer.Parser()
        );

        final Basket basket = mock(Basket.class);
        final OfferPackage offerPackage = new OfferPackage(catalog, dataReader, parsers);
        final List<OfferRule> offers = offerPackage.getApplicableOffers(basket);

        assertEquals(1, offers.size());
        assertEquals(ApplicableOffer.class, offers.get(0).getClass());
    }

    private DataReader buildMockDataReader(String dataContents) throws IOException {
        DataReader dataReader = mock(DataReader.class);
        LineNumberReader reader = new LineNumberReader(new StringReader(dataContents));
        when(dataReader.newLineNumberReader(anyString())).thenReturn(reader);
        return dataReader;
    }

}
