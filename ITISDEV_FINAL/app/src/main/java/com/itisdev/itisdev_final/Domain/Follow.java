package com.itisdev.itisdev_final.Domain;

public class Follow {
    private String id;
    private String followUserId;
    private String followedUserId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFollowUserId() { return followUserId; }
    public void setFollowUserId(String followUserId) { this.followUserId = followUserId; }
    public String getFollowedUserId() { return followedUserId; }
    public void setFollowedUserId(String followedUserId) { this.followedUserId = followedUserId; }
}