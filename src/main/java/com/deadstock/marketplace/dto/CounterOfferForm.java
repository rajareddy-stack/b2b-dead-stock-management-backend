package com.deadstock.marketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CounterOfferForm {

    @NotNull(message = "Counter price per unit is required")
    @DecimalMin(value = "0.01", message = "Counter price must be greater than 0")
    private BigDecimal counterPrice;

    private String sellerMessage;

    public CounterOfferForm() {
    }

    public BigDecimal getCounterPrice() {
        return counterPrice;
    }

    public void setCounterPrice(BigDecimal counterPrice) {
        this.counterPrice = counterPrice;
    }

    public String getSellerMessage() {
        return sellerMessage;
    }

    public void setSellerMessage(String sellerMessage) {
        this.sellerMessage = sellerMessage;
    }
}
