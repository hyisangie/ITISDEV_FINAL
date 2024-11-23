package com.itisdev.itisdev_final.Domain;

public class Food {
    private String description;
    private int restaurantId;
    private int id;
    private double price;
    private int priceId;
    private String imagePath;
    private double star;
    private String name;
    private int numberInCart;

    public Food() {
    }

    public Food(String description, int restaurantId, int id, double price, int priceId, String imagePath, double star, String name, int numberInCart) {
        this.description = description;
        this.restaurantId = restaurantId;
        this.id = id;
        this.price = price;
        this.priceId = priceId;
        this.imagePath = imagePath;
        this.star = star;
        this.name = name;
        this.numberInCart = numberInCart;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getPriceId() {
        return priceId;
    }

    public void setPriceId(int priceId) {
        this.priceId = priceId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public double getStar() {
        return star;
    }

    public void setStar(double star) {
        this.star = star;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberInCart() {
        return numberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        this.numberInCart = numberInCart;
    }


}
