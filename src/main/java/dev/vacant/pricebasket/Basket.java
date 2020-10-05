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
    private final Map<String, Item> items;
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
        if (catalog.getPriceFor(name) == null)
            throw new IllegalArgumentException(name + " doesn't exist in the catalog");

        if (items.containsKey(name)) {
            items.get(name).addAnother();
        } else {
            items.put(name, new Item(name));
        }
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
        return items.values().stream()
                .map(Item::price)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    private class Item {
        private final String name;
        private int amount;

        private Item(String name) {
            this.name = name;
            this.amount = 1;
        }

        private void addAnother() {
            amount++;
        }

        private BigDecimal price() {
            BigDecimal price = catalog.getPriceFor(name);
            return price.multiply(new BigDecimal(amount)).setScale(2, RoundingMode.UNNECESSARY);
        }
    }
}
