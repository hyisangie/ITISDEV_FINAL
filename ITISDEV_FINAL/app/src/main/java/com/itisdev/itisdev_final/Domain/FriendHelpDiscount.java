package com.itisdev.itisdev_final.Domain;

public class FriendHelpDiscount {
    private int id;
    private int voucherId;
    private float availedAmount;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVoucherId() { return voucherId; }
    public void setVoucherId(int voucherId) { this.voucherId = voucherId; }
    public float getAvailedAmount() { return availedAmount; }
    public void setAvailedAmount(float availedAmount) { this.availedAmount = availedAmount; }
}