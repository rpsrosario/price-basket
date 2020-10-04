package dev.vacant.pricebasket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Catalog Unit Tests")
class CatalogTest {

    @ParameterizedTest(name = "{index}. Empty Catalog")
    @ValueSource(strings = {
            "", " ", "\t\t", "  \t\n\n",
            "# This is a comment",
            "  # This is a comment (not in the beginning of the line)"
    })
    void testEmptyCatalog(String dataContents) throws IOException {
        final DataReader dataReader = buildMockDataReader(dataContents);
        final Catalog catalog = new Catalog(dataReader);
        assertTrue(catalog.getAllItems().isEmpty());
    }

    @ParameterizedTest(name = "{index}. Corrupt Catalog")
    @ValueSource(strings = {
            "Apples",                   // missing price
            "1.00",                     // missing item name
            "Apples 1,00",              // invalid number separator
            "Apples 1.00a",             // invalid number
            "Apples 1.005",             // unsupported precision
            "Apples 1.00\nApples 2.00", // duplicate entry (same case)
            "apples 1.00\nAPPLES 2.00", // duplicate entry (different case)
    })
    void testCorruptCatalog(String dataContents) throws IOException {
        final DataReader dataReader = buildMockDataReader(dataContents);
        assertThrows(CorruptDataFileException.class, () -> new Catalog(dataReader));
    }

    @ParameterizedTest(name = "{index}. Sanitized Item Name")
    @CsvSource({
            "'apples 1.00',         APPLES",
            "'Apples 1.00',         APPLES",
            "'APPLES 1.00',         APPLES",
            "'ApPlEs 1.00',         APPLES",
            "'\t\t Apples 1.00\t',  APPLES",
            "'Sugar Cane 2.50',     SUGAR CANE",
            "'Sugar CANE 2.50',     SUGAR CANE",
            "'SuGar Cane 2.50',     SUGAR CANE",
            "'Sugar\tCane 2.50',    SUGAR CANE",
            "'Sugar\t \tCane 2.50', SUGAR CANE",
    })
    void testItemSanitization(String dataContents, String sanitizedName) throws IOException {
        final DataReader dataReader = buildMockDataReader(dataContents);
        final Catalog catalog = new Catalog(dataReader);
        assertEquals(Collections.singleton(sanitizedName), catalog.getAllItems());
    }

    @ParameterizedTest(name = "Pricing Non-Existing {0}")
    @ValueSource(strings = {
            "Red Apples",
            "Aples",
            "Maples",
            "Toilet Paper",
            "Sugar",
            "Cane",
    })
    void testPricingNonExistingItem(String item) throws IOException {
        final DataReader dataReader = buildMockDataReader(
                "Apples 1.00\nBananas 0.50\nSugar Cane 2.50"
        );
        final Catalog catalog = new Catalog(dataReader);
        assertNull(catalog.getPriceFor(item));
    }

    @ParameterizedTest(name = "Pricing {0} yields {1}")
    @CsvSource({
            "apples, 1.00",
            "Apples, 1.00",
            "APPLES, 1.00",
            "ApPlEs, 1.00",
            "Sugar Cane, 2.50",
            "Sugar CANE, 2.50",
            "SuGar Cane, 2.50",
    })
    void testPricingExistingItem(String item, String price) throws IOException {
        final DataReader dataReader = buildMockDataReader(
                "Apples 1.00\nBananas 0.50\nSugar Cane 2.50"
        );
        final Catalog catalog = new Catalog(dataReader);
        assertEquals(new BigDecimal(price), catalog.getPriceFor(item));
    }

    @ParameterizedTest(name = "{index}. Price Has Scale of 2")
    @ValueSource(strings = { "1", "01", "1.1", "1.10", "1.01", "1.010", "1e20" })
    void testPriceScale(String price) throws IOException {
        final DataReader dataReader = buildMockDataReader("Apples " + price);
        final Catalog catalog = new Catalog(dataReader);
        assertEquals(2, catalog.getPriceFor("Apples").scale());
    }

    private DataReader buildMockDataReader(String dataContents) throws IOException {
        DataReader dataReader = mock(DataReader.class);
        LineNumberReader reader = new LineNumberReader(new StringReader(dataContents));
        when(dataReader.newLineNumberReader(anyString())).thenReturn(reader);
        return dataReader;
    }
}
