package dev.vacant.pricebasket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Basket Unit Tests")
public class BasketTest {

    @ParameterizedTest(name = "Adding non-existent item to basket - {0}")
    @ValueSource(strings = {"PEARS", "SUGAR CANE", "MAPLES", "PAPER"})
    void testNonExistentItem(String item) {
        final Catalog catalog = buildMockCatalog();
        final Basket basket = new Basket(catalog);
        assertThrows(IllegalArgumentException.class, () -> basket.addItem(item));
    }

    @ParameterizedTest(name = "Subtotal for {0}")
    @CsvSource({
            "'',                         0.00",
            "'APPLES',                   1.00",
            "'APPLES, APPLES, APPLES',   3.00",
            "'APPLES, BANANAS',          1.80",
            "'APPLES, BANANAS, BANANAS', 2.60",
            "'BANANAS',                  0.80",
    })
    void testBasketSubtotal(String items, String expectedSubtotal) {
        final Catalog catalog = buildMockCatalog();
        final Basket basket = new Basket(catalog);
        Arrays.stream(items.split(","))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .forEach(basket::addItem);
        assertEquals(new BigDecimal(expectedSubtotal), basket.getSubtotal());
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
}
