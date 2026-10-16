package com.deadstock.marketplace.dto;

import com.deadstock.marketplace.model.enums.ItemCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ListingForm {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 64)
    @Pattern(regexp = "[A-Za-z0-9._/-]+", message = "SKU may only contain letters, digits, dots, underscores, slashes, and hyphens")
    private String sku;

    @NotBlank(message = "Description is required")
    @Size(max = 5000)
    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Original retail price is required")
    @DecimalMin(value = "0.01", message = "Original price must be greater than 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal originalPrice;

    @NotNull(message = "Discounted surplus price is required")
    @DecimalMin(value = "0.01", message = "Discounted price must be greater than 0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @NotNull(message = "Available quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Unit of measurement is required (e.g. kg, pieces, meters, pallets)")
    @Size(max = 30)
    private String unit;

    @NotNull(message = "Item condition is required")
    private ItemCondition condition;

    @NotBlank(message = "Pickup location (City, State) is required")
    @Size(max = 150)
    private String location;

    @Size(max = 500)
    @Pattern(regexp = "^https?://[^\\s]+$", message = "Image URL must use HTTP or HTTPS")
    private String imageUrl;

    private LocalDate expiryDate;

    @Size(max = 10, message = "A listing can have at most 10 tags")
    private List<@NotBlank @Size(max = 32) String> tags = new ArrayList<>();

    public ListingForm() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : tags;
    }
}
