package com.deadstock.marketplace.model.enums;

public enum ItemCondition {
    BRAND_NEW_SURPLUS("Brand New Surplus"),
    OVERSTOCKED("Overstocked Inventory"),
    FACTORY_SECONDS("Factory B-Grade / Seconds"),
    OPEN_BOX("Open Box / Unused"),
    REMANUFACTURED("Refurbished / Certified");

    private final String displayName;

    ItemCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
