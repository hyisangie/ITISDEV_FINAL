package com.itisdev.itisdev_final.Domain;

public class FriendHelpDiscount {
    private String userId;
    private String voucherId;
    private float availedAmount;

    // Getters and Setters
    public String getId() { return userId; }
    public void setId(String userId) { this.userId = userId; }
    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    public float getAvailedAmount() { return availedAmount; }
    public void setAvailedAmount(float availedAmount) { this.availedAmount = availedAmount; }
}