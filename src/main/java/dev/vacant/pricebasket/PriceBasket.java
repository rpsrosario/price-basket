package dev.vacant.pricebasket;

import java.io.IOException;

public class PriceBasket {
    public static void main(String[] args) throws IOException {
        Basket basket = new Basket();
        for (String name : args) {
            basket.addItem(name);
        }
        System.out.println("Subtotal: £" + basket.getSubtotal());
    }
}
