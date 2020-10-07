package dev.vacant.pricebasket;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Identifier for an item in our shop.
 * <p>
 * All items are identified by their normalized name. The normalization process
 * replaces all contiguous whitespaces by a single space character, trimming any
 * surrounding spaces, and uses only uppercase characters.
 */
public class ItemId implements CharSequence {
    private final String name;

    /**
     * Creates a new item ID based on the item name supplied.
     *
     * @param name The un-normalized name of the item.
     */
    public ItemId(String name) {
        this.name = normalize(name);
        if (this.name.isEmpty())
            throw new IllegalArgumentException("Item ID must not be blank");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemId)) return false;
        ItemId other = (ItemId) o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int length() {
        return name.length();
    }

    @Override
    public char charAt(int i) {
        return name.charAt(i);
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return name.subSequence(beginIndex, endIndex);
    }

    private static String normalize(String name) {
        return Arrays.stream(name.trim().split("\\s+"))
                .map(String::toUpperCase)
                .collect(Collectors.joining(" "));
    }
}
