package dev.vacant.pricebasket;

import java.math.BigDecimal;

/**
 * Dummy special offer that is always applicable.
 */
class ApplicableOffer implements OfferRule {
    @Override
    public boolean isApplicable(Basket basket) {
        return true;
    }

    @Override
    public BigDecimal calculateDiscount(Basket basket) {
        return new BigDecimal("0.50");
    }

    @Override
    public String getDescription() {
        return "Always applicable";
    }

    public static class Parser implements OfferParser {
        @Override
        public ApplicableOffer parseRule(Catalog catalog, String rule) {
            if (rule.equalsIgnoreCase("applicable"))
                return new ApplicableOffer();
            return null;
        }
    }
}
