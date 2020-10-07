package dev.vacant.pricebasket;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Collection of items to be purchased by a customer.
 * <p>
 * A basket of items keeps track of the items a customer wants to purchase as
 * well as their amounts. It can then provide several pieces of metadata
 * surrounding that information (e.g. the subtotal of the items in the basket).
 */
public class Basket {
    private final Map<ItemId, Integer> items;
    private final Catalog catalog;

    /**
     * Creates a new basket for the item catalog supplied.
     *
     * @param catalog The item catalog to use for this basket.
     */
    public Basket(Catalog catalog) {
        this.catalog = requireNonNull(catalog, "catalog is required");
        this.items = new HashMap<>();
    }

    /**
     * Creates a new basket for the default item catalog.
     *
     * @throws IOException If an I/O error occurs.
     */
    public Basket() throws IOException {
        this(new Catalog());
    }

    /**
     * Adds one unit of the item with the given name to the basket.
     *
     * @param name The name of the item to add.
     * @throws IllegalArgumentException If no item exists in the catalog with
     *                                  the given name.
     */
    public void addItem(String name) {
        ItemId itemId = new ItemId(name);
        if (catalog.getPriceFor(itemId) == null)
            throw new IllegalArgumentException(itemId + " doesn't exist in the catalog");
        items.compute(itemId, (id, amount) -> (amount == null ? 0 : amount) + 1);
    }

    /**
     * Calculates the subtotal for the basket.
     * <p>
     * The subtotal of the basket is the sum of all of the prices of the items
     * in the basket, before applying any potential discounts.
     *
     * @return The subtotal of the basket, in GBP.
     */
    public BigDecimal getSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<ItemId, Integer> basketEntry : items.entrySet()) {
            BigDecimal price = catalog.getPriceFor(basketEntry.getKey());
            BigDecimal amount = new BigDecimal(basketEntry.getValue());
            total = total.add(price.multiply(amount));
        }
        return total.setScale(2, RoundingMode.UNNECESSARY);
    }
}
