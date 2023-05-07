package com.example.suchen.Model;

import org.osmdroid.util.GeoPoint;

public class BookmarkLocationModel {
    private String id;
    private String userId;
    private String locationName;
    private String createdAt;
    private GeoPoint geoPoint;

    public BookmarkLocationModel() {
    }


    public BookmarkLocationModel(String id, String userId, String locationName, String createdAt, GeoPoint geoPoint) {
        this.id = id;
        this.userId = userId;
        this.locationName = locationName;
        this.createdAt = createdAt;
        this.geoPoint = geoPoint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "BookmarkLocation{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", locationName='" + locationName + '\'' +
                ", geoPoint=" + geoPoint +
                '}';
    }
}
