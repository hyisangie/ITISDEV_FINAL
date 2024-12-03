package com.itisdev.itisdev_final.Domain;

public class ClaimedVoucher {
    private String id;
    private String userId;
    private String voucherId;
    private boolean used;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    public boolean getUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}