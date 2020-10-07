package dev.vacant.pricebasket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ItemId Unit Tests")
class ItemIdTest {
    @ParameterizedTest(name = "Normalizing {0}")
    @CsvSource({
            "Apples,            APPLES",
            "apples,            APPLES",
            "aPpLeS,            APPLES",
            "APPLES,            APPLES",
            "\tAPPLES\t,        APPLES",
            "Sugar Cane,        SUGAR CANE",
            "Sugar CANE,        SUGAR CANE",
            "SuGAR Cane,        SUGAR CANE",
            "SuGAR\tCane,       SUGAR CANE",
            "SuGAR\t \tCane,    SUGAR CANE",
    })
    void testItemIdNormalization(String name, String expectedId) {
        final ItemId itemId = new ItemId(name);
        assertEquals(expectedId, itemId.toString());
    }
}
