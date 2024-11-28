package com.itisdev.itisdev_final.Domain;


import java.util.ArrayList;

public class Restaurant {
    private String id;
    private String name;
    private String description;

    private Float rating;
    private String tags;
    private ArrayList<String> images;
    private String cuisineType;
    private String address;
    private String openingHours;
    private String contactDetails;
    private String listingDate;
    private boolean isActive;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Float getRating() {
        return rating;
    }
    public void setRating(Float rating) {
        this.rating = rating;
    }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public ArrayList<String> getImages() { return images; }
    public void setImages(ArrayList<String> images) { this.images = images; }
    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public String getContactDetails() { return contactDetails; }
    public void setContactDetails(String contactDetails) { this.contactDetails = contactDetails; }
    public String getListingDate() { return listingDate; }
    public void setListingDate(String listingDate) { this.listingDate = listingDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
