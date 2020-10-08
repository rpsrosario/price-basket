package dev.vacant.pricebasket;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.util.Objects.requireNonNull;

/**
 * Utility class for formatting objects for displaying to the user.
 */
public class FormattingUtil {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    private FormattingUtil() {
        // Utility class
    }

    /**
     * Formats the specified amount of money (in GBP) according to the following
     * rules:
     * <ul>
     * <li>If the amount is above {@code 1.00} then it is prepended with "£",</li>
     * <li>Otherwise the fractional amount is suffixed with "p".</li>
     * </ul>
     *
     * @param money The amount to format as money (in GBP).
     * @return The formatted monetary amount.
     */
    public static String formatMoney(BigDecimal money) {
        requireNonNull(money, "money is required");
        money = money.setScale(2, RoundingMode.UNNECESSARY);
        if (money.compareTo(BigDecimal.ZERO) != 0 && money.longValue() == 0) {
            money = money.multiply(ONE_HUNDRED).setScale(0, RoundingMode.UNNECESSARY);
            return money + "p";
        } else {
            return "£" + money;
        }
    }

    /**
     * Capitalizes all of the words in the supplied item ID.
     *
     * @param itemId The item ID to format.
     * @return The formatted item ID.
     */
    public static String formatItem(ItemId itemId) {
        requireNonNull(itemId, "itemId is required");
        StringBuilder builder = new StringBuilder(itemId);

        boolean lastWasCapital = false;
        for (int i = 0; i < builder.length(); i++) {
            char c = builder.charAt(i);
            if (Character.isWhitespace(c)) {
                lastWasCapital = false;
            } else if (lastWasCapital) {
                builder.setCharAt(i, Character.toLowerCase(c));
            } else {
                builder.setCharAt(i, Character.toUpperCase(c));
                lastWasCapital = true;
            }
        }

        return builder.toString();
    }
}
