package dev.vacant.pricebasket;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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
    private final OfferPackage offerPackage;

    /**
     * Creates a new basket for the item catalog supplied.
     *
     * @param catalog      The item catalog to use for this basket.
     * @param offerPackage The package of special offer available.
     */
    public Basket(Catalog catalog, OfferPackage offerPackage) {
        this.catalog = requireNonNull(catalog, "catalog is required");
        this.offerPackage = requireNonNull(offerPackage, "offerPackage is required");
        this.items = new HashMap<>();
    }

    /**
     * Creates a new basket for the default item catalog and the default package
     * of special offers.
     *
     * @throws IOException If an I/O error occurs.
     */
    public Basket() throws IOException {
        this.catalog = new Catalog();
        this.offerPackage = new OfferPackage(catalog);
        this.items = new HashMap<>();
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
     * Retreives the amount of items with a specific ID that are added to this
     * basket.
     *
     * @param itemId The ID of the items to check.
     * @return The amount of items with the given ID in the basket.
     */
    public int getAmountOf(ItemId itemId) {
        return items.getOrDefault(itemId, 0);
    }

    /**
     * Prices the current basket.
     * <p>
     * Pricing of a given basket produces several different pieces of metadata
     * which are bundled into one result class.
     *
     * @return The metadata associated with the pricing of this basket.
     */
    public Price price() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<ItemId, Integer> basketEntry : items.entrySet()) {
            BigDecimal price = catalog.getPriceFor(basketEntry.getKey());
            BigDecimal amount = new BigDecimal(basketEntry.getValue());
            subtotal = subtotal.add(price.multiply(amount));
        }
        subtotal = subtotal.setScale(2, RoundingMode.UNNECESSARY);

        NavigableMap<String, BigDecimal> offers = new TreeMap<>();
        for (OfferRule offer : offerPackage.getApplicableOffers(this)) {
            offers.put(offer.getDescription(), offer.calculateDiscount(this));
        }

        BigDecimal total = subtotal;
        for (BigDecimal discount : offers.values()) {
            total = total.subtract(discount);
        }
        total = total.max(BigDecimal.ZERO).setScale(2, RoundingMode.UNNECESSARY);

        return new Price(subtotal, total, offers);
    }

    /**
     * Metadata pertaining to pricing of an item basket.
     */
    public static class Price {
        private final BigDecimal subtotal;
        private final BigDecimal total;
        private final NavigableMap<String, BigDecimal> offers;

        private Price(BigDecimal subtotal,
                      BigDecimal total,
                      NavigableMap<String, BigDecimal> offers
        ) {
            this.subtotal = subtotal;
            this.total = total;
            this.offers = Collections.unmodifiableNavigableMap(offers);
        }

        /**
         * Retrieves the total price of the items in the basket, before applying
         * any special offers.
         *
         * @return The subtotal of the item basket.
         */
        public BigDecimal getSubtotal() {
            return subtotal;
        }

        /**
         * Retrieves the total price of the items in the basket, after applying
         * any special offers.
         *
         * @return The total of the item basket.
         */
        public BigDecimal getTotal() {
            return total;
        }

        /**
         * Retrieves a map of all of the special offers applied to the basket.
         * The keys of the map are the descriptions of the offers while the
         * values are the discounted value from that special offer.
         *
         * @return The special offers applied to the item basket.
         */
        public NavigableMap<String, BigDecimal> getOffers() {
            return offers;
        }
    }
}
