package dev.vacant.pricebasket;

import java.math.BigDecimal;

/**
 * Rule of how to apply a special offer.
 * <p>
 * Each offer rule is configured in a dedicated data file using a line-by-line
 * textual format. A dedicated parser must be registered for each type of offer
 * rule supported.
 */
public interface OfferRule {
    /**
     * Checks if the current special offer is applicable to the item basket
     * supplied.
     *
     * @param basket The item basket.
     * @return {@code true} if the offer is applicable, {@code false} otherwise.
     */
    boolean isApplicable(Basket basket);

    /**
     * Calculates the applicable discount from this special offer.
     *
     * @param basket The item basket the offer applies to.
     * @return The total discount to be applied to the item basked.
     */
    BigDecimal calculateDiscount(Basket basket);

    /**
     * Retrieves a description of the current offer.
     *
     * @return The offer's description.
     */
    String getDescription();
}
