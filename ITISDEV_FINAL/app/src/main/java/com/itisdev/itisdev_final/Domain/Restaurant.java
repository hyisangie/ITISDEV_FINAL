package com.itisdev.itisdev_final.Domain;

import java.util.List;
import java.util.Map;

public class Restaurant {
    private int categoryId;
    private String description;
    private boolean bestRestaurant;
    private int id;
    private String name;
    private int locationId;
    private int priceRangeId;
    private double rating;
    private String address;
    private String openingHour;
    private String phoneNumber;
    private List<String> photos;

    public Restaurant() {
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getBestRestaurant() {
        return bestRestaurant;
    }

    public void setBestRestaurant(boolean bestRestaurant) {
        this.bestRestaurant = bestRestaurant;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getPriceRangeId() {
        return priceRangeId;
    }

    public void setPriceRangeId(int priceRangeId) {
        this.priceRangeId = priceRangeId;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(String openingHour) {
        this.openingHour = openingHour;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }
}
