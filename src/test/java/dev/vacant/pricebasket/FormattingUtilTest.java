package dev.vacant.pricebasket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static dev.vacant.pricebasket.FormattingUtil.formatItem;
import static dev.vacant.pricebasket.FormattingUtil.formatMoney;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FormattingUtil Unit Tests")
class FormattingUtilTest {
    @ParameterizedTest(name = "Formatting Money {0} to {1}")
    @CsvSource({
            "1.00,   £1.00",
            "1.25,   £1.25",
            "12.5,   £12.50",
            "125,    £125.00",
            "-1.25,  £-1.25",
            "0,      £0.00",
            "0.05,   5p",
            "0.50,   50p",
            "-0.05,  -5p,"
    })
    void testFormatMoney(String money, String expected) {
        final String actual = formatMoney(new BigDecimal(money));
        assertEquals(expected, actual);
    }
    @ParameterizedTest(name = "Formatting Item {0} to {1}")
    @CsvSource({
            "apples, Apples",
            "Apples, Apples",
            "ApPlEs, Apples",
            "APPLES, Apples",
            "sugar cane, Sugar Cane",
            "Sugar Cane, Sugar Cane",
            "Sugar CANE, Sugar Cane",
            "SUGAR Cane, Sugar Cane",
            "SUGAR CANE, Sugar Cane",
    })
    void testFormatItem(String name, String expected) {
        final String actual = formatItem(new ItemId(name));
        assertEquals(expected, actual);
    }
}
