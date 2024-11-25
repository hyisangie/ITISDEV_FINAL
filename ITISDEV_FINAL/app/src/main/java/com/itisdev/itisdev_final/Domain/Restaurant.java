package com.itisdev.itisdev_final.Domain;

import java.util.List;
import java.util.Map;

public class Restaurant {
    private int id;
    private String name;
    private String description;
    private String tags;
    private String images;
    private int cuisineType;
    private String address;
    private String openingHours;
    private String contactDetails;
    private String listingDate;
    private boolean isActive;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public int getCuisineType() { return cuisineType; }
    public void setCuisineType(int cuisineType) { this.cuisineType = cuisineType; }
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
