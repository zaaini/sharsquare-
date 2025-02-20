package com.example.ss;

import java.util.List;

public class homeModel {
    private List<String> imageUrls;
    private String latitude;
    private String longitude;
    private String price;
    private String sellOrExchange;
    private String brand;
    private String description;
    private String email;

    public homeModel(List<String> imageUrls, String latitude, String longitude, String price, String sellOrExchange, String brand, String description, String email) {
        this.imageUrls = imageUrls;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
        this.sellOrExchange = sellOrExchange;
        this.brand = brand;
        this.description = description;
        this.email = email;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getPrice() {
        return price;
    }

    public String getSellOrExchange() {
        return sellOrExchange;
    }

    public String getBrand() {
        return brand;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }
}
