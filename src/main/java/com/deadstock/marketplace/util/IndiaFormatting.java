package com.deadstock.marketplace.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component("india")
public class IndiaFormatting {

    public String currency(BigDecimal amount) {
        if (amount == null) {
            return "—";
        }

        BigDecimal rounded = amount.setScale(2, RoundingMode.HALF_UP);
        String value = rounded.abs().toPlainString();
        int decimalIndex = value.indexOf('.');
        String integerPart = decimalIndex < 0 ? value : value.substring(0, decimalIndex);
        String decimalPart = decimalIndex < 0 ? "00" : value.substring(decimalIndex + 1);
        String grouped = groupIndianDigits(integerPart);

        return (rounded.signum() < 0 ? "-₹" : "₹") + grouped + "." + decimalPart;
    }

    public String stockValue(BigDecimal unitPrice, int quantity) {
        return unitPrice == null ? currency(null) : currency(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    public long daysListed(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDate.now()));
    }

    private String groupIndianDigits(String digits) {
        if (digits.length() <= 3) {
            return digits;
        }

        String lastThree = digits.substring(digits.length() - 3);
        String remaining = digits.substring(0, digits.length() - 3);
        StringBuilder groups = new StringBuilder();
        while (!remaining.isEmpty()) {
            int start = Math.max(0, remaining.length() - 2);
            if (!groups.isEmpty()) {
                groups.insert(0, ',');
            }
            groups.insert(0, remaining.substring(start));
            remaining = remaining.substring(0, start);
        }
        return groups + "," + lastThree;
    }
}
