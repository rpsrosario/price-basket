package dev.vacant.pricebasket;

import java.math.BigDecimal;

/**
 * Dummy special offer that is never applicable.
 */
class NotApplicableOffer implements OfferRule {
    @Override
    public boolean isApplicable(Basket basket) {
        return false;
    }

    @Override
    public BigDecimal calculateDiscount(Basket basket) {
        return BigDecimal.ZERO;
    }

    @Override
    public String getDescription() {
        return "Never applicable";
    }

    public static class Parser implements OfferParser {
        @Override
        public NotApplicableOffer parseRule(Catalog catalog, String rule) {
            if (rule.equalsIgnoreCase("not applicable"))
                return new NotApplicableOffer();
            return null;
        }
    }
}
