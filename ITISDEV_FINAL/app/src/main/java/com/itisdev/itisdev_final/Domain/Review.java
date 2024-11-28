package com.itisdev.itisdev_final.Domain;

public class Review {
    private String id;
    private String userId;
    private String restaurantId;
    private float rating;
    private String description;
    private String image;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String images) { this.image = images; }
}