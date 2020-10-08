package dev.vacant.pricebasket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.vacant.pricebasket.FormattingUtil.formatItem;

/**
 * Rule for specifying discounts on one item based on the purchase of a minimum
 * amount of a different item.
 * <p>
 * The syntax for the rule is {@code 'DISCOUNTED_ITEM_ID DISCOUNT per MIN_AMOUNT
 * REQUIRED_ITEM_ID'}. This syntax states that for every {@code MIN_AMOUNT} items
 * with the {@code REQUIRED_ITEM_ID} one of the {@code DISCOUNTED_ITEM_ID} will
 * have a discount of {@code DISCOUNT}.
 * <p>
 * This rule will not accept any negative values nor will it accept a minimum
 * amount of {@code 0} required items. Furthermore, the rule won't accept a
 * discount of more than the total price for the item.
 * <p>
 * Note that this rule only applies the discount if the discounted item is in
 * the basket.
 */
public class BundleOffer implements OfferRule {
    private static final Pattern RULE_FORMAT = Pattern.compile(
            "(?<discountedId>.*?)\\s+(?<discount>\\d+\\.\\d+)\\s+" +
                    "(?i:per)\\s+(?<amount>\\d+)\\s+(?<requiredId>.*?)"
    );

    private final ItemId discountedItemId;
    private final ItemId requiredItemId;
    private final BigDecimal discount;
    private final int minAmount;

    private BundleOffer(ItemId discountedItemId,
                        ItemId requiredItemId,
                        BigDecimal discount,
                        int minAmount
    ) {
        this.discountedItemId = discountedItemId;
        this.requiredItemId = requiredItemId;
        this.discount = discount;
        this.minAmount = minAmount;
    }

    @Override
    public boolean isApplicable(Basket basket) {
        return basket.getAmountOf(requiredItemId) >= minAmount
                && basket.getAmountOf(discountedItemId) > 0;
    }

    @Override
    public BigDecimal calculateDiscount(Basket basket) {
        int count = basket.getAmountOf(requiredItemId) / minAmount;
        count = Math.min(count, basket.getAmountOf(discountedItemId));
        return discount.multiply(new BigDecimal(count))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public String getDescription() {
        return formatItem(discountedItemId) + " and " + formatItem(requiredItemId) + " bundle";
    }

    public static class Parser implements OfferParser {
        @Override
        public BundleOffer parseRule(Catalog catalog, String rule) {
            Matcher matcher = RULE_FORMAT.matcher(rule.trim());
            if (!matcher.matches())
                return null;

            try {
                ItemId discountedId = new ItemId(matcher.group("discountedId"));
                ItemId requiredId = new ItemId(matcher.group("requiredId"));
                if (catalog.getPriceFor(discountedId) == null
                        || catalog.getPriceFor(requiredId) == null) {
                    return null;
                }

                BigDecimal value = new BigDecimal(matcher.group("discount"))
                        .setScale(2, RoundingMode.UNNECESSARY);
                int amount = Integer.parseInt(matcher.group("amount"));

                if (value.compareTo(catalog.getPriceFor(discountedId)) > 0)
                    return null;
                if (amount == 0)
                    return null;

                return new BundleOffer(discountedId, requiredId, value, amount);
            } catch (NumberFormatException | ArithmeticException ignored) {
                return null;
            }
        }
    }
}
