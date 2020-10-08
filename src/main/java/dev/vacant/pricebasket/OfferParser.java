package dev.vacant.pricebasket;

import java.util.ServiceLoader;

/**
 * Parser for the text file format used for configuring special offers.
 * <p>
 * The parsers are registered using Java's {@link ServiceLoader service provider}
 * mechanism and discovered at runtime. Each parser should parse exactly one
 * type of offer. Each offer must be describable in one line.
 */
public interface OfferParser {
    /**
     * Attempts to parse an offer from the textual rule supplied.
     *
     * @param catalog The catalog of items available in our shop.
     * @param rule    The textual rule for the offer, trimmed.
     * @return The parsed offer or {@code null} if this parser does not support
     * this rule.
     */
    OfferRule parseRule(Catalog catalog, String rule);
}
