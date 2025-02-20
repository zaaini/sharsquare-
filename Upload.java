package com.example.ss;

public class Upload {
    private String mImageUri;
    private double latitude;
    private double longitude;

    // Default constructor for Firebase deserialization
    public Upload() { }

    // Constructor for all fields
    public Upload(String mImageUri, double latitude, double longitude) {
        this.mImageUri = mImageUri;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getmImageUri() {
        return mImageUri;
    }

    public void setmImageUri(String mImageUri) {
        this.mImageUri = mImageUri;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
