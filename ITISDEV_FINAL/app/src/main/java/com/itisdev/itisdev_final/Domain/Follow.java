package com.itisdev.itisdev_final.Domain;

public class Follow {
    private int id;
    private int followUserId;
    private int followedUserId;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFollowUserId() { return followUserId; }
    public void setFollowUserId(int followUserId) { this.followUserId = followUserId; }
    public int getFollowedUserId() { return followedUserId; }
    public void setFollowedUserId(int followedUserId) { this.followedUserId = followedUserId; }
}