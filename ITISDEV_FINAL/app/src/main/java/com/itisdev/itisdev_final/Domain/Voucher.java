package com.itisdev.itisdev_final.Domain;

public class Voucher {
    private int id;
    private int restaurantId;
    private int type;
    private int amount;
    private int minSpend;
    private boolean isActive;
    private String description;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRestaurantId() { return restaurantId; }
    public void setRestaurantId(int restaurantId) { this.restaurantId = restaurantId; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public int getMinSpend() { return minSpend; }
    public void setMinSpend(int minSpend) { this.minSpend = minSpend; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}