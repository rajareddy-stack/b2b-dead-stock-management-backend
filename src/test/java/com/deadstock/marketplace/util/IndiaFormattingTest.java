package com.deadstock.marketplace.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndiaFormattingTest {

    private final IndiaFormatting indiaFormatting = new IndiaFormatting();

    @Test
    void formatsCurrencyUsingIndianDigitGrouping() {
        assertEquals("₹1,25,000.00", indiaFormatting.currency(new BigDecimal("125000")));
        assertEquals("₹99.50", indiaFormatting.currency(new BigDecimal("99.5")));
        assertEquals("-₹12,34,567.89", indiaFormatting.currency(new BigDecimal("-1234567.89")));
    }

    @Test
    void formatsExtendedStockValueUsingIndianDigitGrouping() {
        assertEquals("₹1,25,000.00", indiaFormatting.stockValue(new BigDecimal("2500"), 50));
        assertEquals("—", indiaFormatting.stockValue(null, 50));
    }

    @Test
    void calculatesDaysSinceListingWasPublished() {
        assertEquals(2, indiaFormatting.daysListed(LocalDateTime.now().minusDays(2)));
        assertEquals(0, indiaFormatting.daysListed(null));
    }
}
