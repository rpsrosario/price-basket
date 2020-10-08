package dev.vacant.pricebasket;

import dev.vacant.pricebasket.BundleOffer.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("BundleOffer Unit Tests")
class BundleOfferTest {
    @ParameterizedTest(name = "{index}. Invalid Rule")
    @ValueSource(strings = {
            // empty rules & comments
            "", " ", "\t\t", "  \t\n\n",
            "# This is a comment",
            "  # This is a comment (not in the beginning of the line)",

            // Missing elements
            "Apples",
            "Apples 0.10",
            "Apples 0.10 per",
            "Apples 0.10 per 2",
            "Apples 0.10 2 Bananas",
            "Apples 0.10 Bananas",
            "Apples Bananas",
            "0.10 per 2 Bananas",
            "per 2 Bananas",
            "2 Bananas",

            // Non-exiting items
            "Sugar Cane 0.10 per 2 Bananas",
            "Apples 0.10 per 2 Sugar Cane",

            "Apples 0,10 per 2 Bananas",   // invalid number separator
            "Apples 0.10a per 2 Bananas",  // invalid number
            "Apples 0.105 per 2 Bananas",  // unsupported precision
            "Apples -0.10 per 2 Bananas",  // negative direct discount
            "Apples 1.10 per 2 Bananas",   // direct discount above item price
            "Apples 0.10 per 0 Bananas",   // zero min amount
            "Apples 0.10 per -1 Bananas",  // negative min amount
            "Apples 0.10 for 2 Bananas",   // wrong keyword
    })
    void testInvalidRule(String rule) {
        final Catalog catalog = buildMockCatalog();
        final Parser parser = new Parser();
        assertNull(parser.parseRule(catalog, rule));
    }

    @ParameterizedTest(name = "Discount for {0} apples and {1} bananas")
    @CsvSource({
            "0, 0, false, 0.00",
            "0, 1, false, 0.00",
            "0, 2, false, 0.00",
            "0, 3, false, 0.00",
            "1, 0, false, 0.00",
            "2, 0, false, 0.00",
            "3, 0, false, 0.00",
            "1, 1, false, 0.00",
            "1, 2, true,  0.10",
            "1, 3, true,  0.10",
            "1, 4, true,  0.10",
            "2, 2, true,  0.10",
            "2, 3, true,  0.10",
            "2, 4, true,  0.20",
    })
    void testValidRule(int discountedAmount,
                       int requiredAmount,
                       boolean isApplicable,
                       String expectedDiscount
    ) {
        final ItemId apples = new ItemId("apples");
        final ItemId bananas = new ItemId("bananas");
        final Catalog catalog = buildMockCatalog();
        final Basket basket = buildMockBasket(apples, discountedAmount, bananas, requiredAmount);
        final Parser parser = new Parser();
        final BundleOffer rule = parser.parseRule(catalog, "Apples 0.10 per 2 Bananas");

        assertAll(
                () -> assertEquals(isApplicable, rule.isApplicable(basket)),
                () -> assertEquals(new BigDecimal(expectedDiscount), rule.calculateDiscount(basket)),
                () -> assertEquals("Apples and Bananas bundle", rule.getDescription())
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

    private Basket buildMockBasket(ItemId discountedId,
                                   int discountedAmount,
                                   ItemId requiredId,
                                   int requiredAmount
    ) {
        Basket basket = mock(Basket.class);
        when(basket.getAmountOf(discountedId)).thenReturn(discountedAmount);
        when(basket.getAmountOf(requiredId)).thenReturn(requiredAmount);
        return basket;
    }
}
