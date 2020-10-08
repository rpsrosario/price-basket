package dev.vacant.pricebasket;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static dev.vacant.pricebasket.FormattingUtil.formatMoney;

/**
 * Command Line Application for pricing item baskets.
 * <p>
 * This CLI application reads the configuration of the shop's catalog and
 * special offers from data files. If no data files exist in the working
 * directory then the default data files (bundled with the application) will be
 * used instead. These default data files will be persisted to disk in order to
 * provide a basis for external configuration.
 * <p>
 * All of the parameters to the application are taken as case-insensitive names
 * of the items to be added to the item basket for pricing. The output of the
 * program consists of one line for the subtotal, one or more lines for any
 * applicable special offers (or a special message if none is applicable) and
 * one line for the total of the basket.
 * <p>
 * All prices used by the application are in GBP.
 */
public class PriceBasket {
    public static void main(String[] args) throws IOException {
        Basket basket = new Basket();
        for (String name : args)
            basket.addItem(name);

        Basket.Price price = basket.price();
        System.out.println("Subtotal: " + formatMoney(price.getSubtotal()));

        Set<Map.Entry<String, BigDecimal>> offers = price.getOffers().entrySet();
        if (offers.isEmpty()) {
            System.out.println("(no offers available)");
        } else {
            for (Map.Entry<String, BigDecimal> offer : offers) {
                System.out.println(offer.getKey() + ": " + formatMoney(offer.getValue().negate()));
            }
        }

        System.out.println("Total: " + formatMoney(price.getTotal()));
    }
}
