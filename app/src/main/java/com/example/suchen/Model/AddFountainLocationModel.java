package com.example.suchen.Model;

import android.net.Uri;

import org.osmdroid.util.GeoPoint;

public class AddFountainLocationModel {

    private String objId;
    private String title;
    private String description;
    private GeoPoint location;
    private Boolean isActive;
    private String addedUserId;
    private String updatedUserId;
    private Uri imageUrl;

    public AddFountainLocationModel() {
    }

    public AddFountainLocationModel(String objId, String title, String description, GeoPoint location, Boolean isActive, String addedUserId, String updatedUserId, Uri imageUrl) {
        this.objId = objId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.isActive = isActive;
        this.addedUserId = addedUserId;
        this.updatedUserId = updatedUserId;
        this.imageUrl = imageUrl;
    }

    public AddFountainLocationModel(String title, String description, GeoPoint location, Boolean isActive, String addedUserId, Uri imageUrl) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.isActive = isActive;
        this.addedUserId = addedUserId;
        this.imageUrl = imageUrl;
    }
}
