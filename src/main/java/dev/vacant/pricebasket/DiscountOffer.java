package dev.vacant.pricebasket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.vacant.pricebasket.FormattingUtil.formatItem;
import static dev.vacant.pricebasket.FormattingUtil.formatMoney;

/**
 * Rule for specifying a direct discount on an item. The discount can be
 * specified either as a percentage of the total price or as an absolute amount
 * to discount.
 * <p>
 * The general syntax for the rule is {@code 'ITEM_ID DISCOUNT'}, where the
 * discount can be either a percentage discount ({@code VALUE%}) or just
 * the total value to discount ({@code VALUE}).
 * <p>
 * This rule will not accept any negative values (as all values should be
 * specified in their absolute value) or values that are higher than the total
 * price of the item (e.g. a percentage of over 100%).
 */
public class DiscountOffer implements OfferRule {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private static final Pattern RULE_FORMAT = Pattern.compile(
            "(?<id>.*?)\\s+(?<discount>\\d+%|\\d+\\.\\d+)"
    );

    private final Catalog catalog;
    private final ItemId itemId;
    private final BigDecimal discount;
    private final boolean directDiscount;

    private DiscountOffer(Catalog catalog,
                          ItemId itemId,
                          BigDecimal discount,
                          boolean directDiscount
    ) {
        this.catalog = catalog;
        this.itemId = itemId;
        this.discount = discount;
        this.directDiscount = directDiscount;
    }

    @Override
    public boolean isApplicable(Basket basket) {
        return basket.getAmountOf(itemId) > 0;
    }

    @Override
    public BigDecimal calculateDiscount(Basket basket) {
        BigDecimal value = discount;
        if (!directDiscount) {
            value = catalog.getPriceFor(itemId).multiply(discount);
        }
        return value.multiply(new BigDecimal(basket.getAmountOf(itemId)))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public String getDescription() {
        if (directDiscount) {
            return formatItem(itemId) + " " + formatMoney(discount) + " off";
        } else {
            int percentage = discount.multiply(ONE_HUNDRED).intValue();
            return formatItem(itemId) + " " + percentage + "% off";
        }
    }

    public static class Parser implements OfferParser {
        @Override
        public DiscountOffer parseRule(Catalog catalog, String rule) {
            Matcher matcher = RULE_FORMAT.matcher(rule.trim());
            if (!matcher.matches())
                return null;

            ItemId itemId = new ItemId(matcher.group("id"));
            if (catalog.getPriceFor(itemId) == null)
                return null;

            String discount = matcher.group("discount");
            try {
                if (discount.endsWith("%")) {
                    BigDecimal percentage = new BigDecimal(discount.replace("%", ""));
                    if (percentage.compareTo(ONE_HUNDRED) > 0)
                        return null;

                    percentage = percentage.setScale(2, RoundingMode.UNNECESSARY);
                    percentage = percentage.divide(ONE_HUNDRED, BigDecimal.ROUND_HALF_EVEN);
                    return new DiscountOffer(catalog, itemId, percentage, false);
                } else {
                    BigDecimal value = new BigDecimal(discount);
                    if (value.compareTo(catalog.getPriceFor(itemId)) > 0)
                        return null;
                    value = value.setScale(2, RoundingMode.UNNECESSARY);
                    return new DiscountOffer(catalog, itemId, value, true);
                }
            } catch (NumberFormatException | ArithmeticException ignored) {
                return null;
            }
        }
    }
}
