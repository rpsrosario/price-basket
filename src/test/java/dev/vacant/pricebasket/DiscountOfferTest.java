package dev.vacant.pricebasket;

import dev.vacant.pricebasket.DiscountOffer.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DiscountOffer Unit Tests")
class DiscountOfferTest {
    @ParameterizedTest(name = "{index}. Invalid Rule")
    @ValueSource(strings = {
            // empty rules & comments
            "", " ", "\t\t", "  \t\n\n",
            "# This is a comment",
            "  # This is a comment (not in the beginning of the line)",

            "Sugar Cane",               // non-existent item
            "Apples",                   // missing price
            "1.00",                     // missing item name
            "Apples 1,00",              // invalid number separator
            "Apples 1.00a",             // invalid number
            "Apples 1.005",             // unsupported precision
            "Apples -1.00",             // negative direct discount
            "Apples 1.10",              // direct discount above item price
            "Apples 10 %",              // percentage not with value
            "Apples 101%",              // percentage above item price
            "Apples 10.1%",             // non-integer percentage
            "Apples -10%",              // negative percentage
            "Apples io%",               // percentage is not a number
    })
    void testInvalidRule(String rule) {
        final Catalog catalog = buildMockCatalog();
        final Parser parser = new Parser();
        assertNull(parser.parseRule(catalog, rule));
    }

    @ParameterizedTest(name = "Percentage discount for {0} apples")
    @CsvSource({
            "0, false,  0.00",
            "1, true,   0.10",
            "3, true,   0.30",
    })
    void testValidRulePercentage(int amount, boolean isApplicable, String expectedDiscount) {
        final ItemId apples = new ItemId("apples");
        final Catalog catalog = buildMockCatalog();
        final Basket basket = buildMockBasket(apples, amount);
        final Parser parser = new Parser();
        final DiscountOffer rule = parser.parseRule(catalog, "apples 10%");

        assertAll(
                () -> assertEquals(isApplicable, rule.isApplicable(basket)),
                () -> assertEquals(new BigDecimal(expectedDiscount), rule.calculateDiscount(basket)),
                () -> assertEquals("Apples 10% off", rule.getDescription())
        );
    }

    @ParameterizedTest(name = "Direct discount for {0} apples")
    @CsvSource({
            "0, false,  0.00",
            "1, true,   0.10",
            "3, true,   0.30",
    })
    void testValidRuleDirectDiscount(int amount, boolean isApplicable, String expectedDiscount) {
        final ItemId apples = new ItemId("apples");
        final Catalog catalog = buildMockCatalog();
        final Basket basket = buildMockBasket(apples, amount);
        final Parser parser = new Parser();
        final DiscountOffer rule = parser.parseRule(catalog, "apples 0.10");

        assertAll(
                () -> assertEquals(isApplicable, rule.isApplicable(basket)),
                () -> assertEquals(new BigDecimal(expectedDiscount), rule.calculateDiscount(basket)),
                () -> assertEquals("Apples 10p off", rule.getDescription())
        );
    }

    private Catalog buildMockCatalog() {
        Map<ItemId, BigDecimal> data = new HashMap<>();
        data.put(new ItemId("apples"), new BigDecimal("1.00"));
        data.put(new ItemId("bananas"), new BigDecimal("0.80"));

        Catalog catalog = mock(Catalog.class);
        when(catalog.getAllItems()).thenReturn(data.keySet());
        when(catalog.getPriceFor(any())).thenAnswer(invocation -> {
            ItemId itemId = invocation.getArgument(0);
            return data.get(itemId);
        });
        return catalog;
    }

    private Basket buildMockBasket(ItemId itemId, int amount) {
        Basket basket = mock(Basket.class);
        when(basket.getAmountOf(itemId)).thenReturn(amount);
        return basket;
    }
}
