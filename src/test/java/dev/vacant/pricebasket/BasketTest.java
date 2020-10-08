package dev.vacant.pricebasket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Basket Unit Tests")
public class BasketTest {

    @ParameterizedTest(name = "Adding non-existent item to basket - {0}")
    @ValueSource(strings = {"PEARS", "SUGAR CANE", "MAPLES", "PAPER"})
    void testNonExistentItem(String item) {
        final Catalog catalog = buildMockCatalog();
        final OfferPackage offerPackage = buildMockOfferPackage();
        final Basket basket = new Basket(catalog, offerPackage);
        assertThrows(IllegalArgumentException.class, () -> basket.addItem(item));
    }

    @ParameterizedTest(name = "Pricing for {0} (no special offers)")
    @CsvSource({
            "'',                         0.00, 0.00",
            "'APPLES',                   1.00, 1.00",
            "'APPLES, APPLES, APPLES',   3.00, 3.00",
            "'APPLES, BANANAS',          1.80, 1.80",
            "'APPLES, BANANAS, BANANAS', 2.60, 2.60",
            "'BANANAS',                  0.80, 0.80",
    })
    void testBasketPricingWithoutOffers(String items, String expectedSubtotal, String expectedTotal) {
        final Catalog catalog = buildMockCatalog();
        final OfferPackage offerPackage = buildMockOfferPackage();
        final Basket basket = new Basket(catalog, offerPackage);
        Arrays.stream(items.split(","))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .forEach(basket::addItem);

        final Basket.Price price = basket.price();
        assertAll(
                () -> assertEquals(new BigDecimal(expectedSubtotal), price.getSubtotal()),
                () -> assertEquals(new BigDecimal(expectedTotal), price.getTotal()),
                () -> assertTrue(price.getOffers().isEmpty())
        );
    }

    @ParameterizedTest(name = "Pricing for {0} (with special offers)")
    @CsvSource({
            "'',                         0.00, 0.00",
            "'APPLES',                   1.00, 0.50",
            "'APPLES, APPLES, APPLES',   3.00, 2.50",
            "'APPLES, BANANAS',          1.80, 1.30",
            "'APPLES, BANANAS, BANANAS', 2.60, 2.10",
            "'BANANAS',                  0.80, 0.30",
    })
    void testBasketPricingWithOffers(String items, String expectedSubtotal, String expectedTotal) {
        final Catalog catalog = buildMockCatalog();
        final OfferPackage offerPackage = buildMockOfferPackage(new ApplicableOffer());
        final Basket basket = new Basket(catalog, offerPackage);
        Arrays.stream(items.split(","))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .forEach(basket::addItem);

        final Basket.Price price = basket.price();
        assertAll(
                () -> assertEquals(new BigDecimal(expectedSubtotal), price.getSubtotal()),
                () -> assertEquals(new BigDecimal(expectedTotal), price.getTotal()),
                () -> assertEquals("Always applicable", price.getOffers().firstKey())
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

    private OfferPackage buildMockOfferPackage(OfferRule... appliedOffers) {
        OfferPackage offerPackage = mock(OfferPackage.class);
        when(offerPackage.getApplicableOffers(any())).thenReturn(asList(appliedOffers));
        return offerPackage;
    }
}
