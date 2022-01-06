package com.example.navagationapp.Data;

import com.google.android.gms.maps.model.LatLng;

public class Location {

    private LatLng lat;
    private String latitude;
    private String longitude;

    // empty constructor
    public Location(){}


    public Location(LatLng lat, String latitude, String longitude) {
        this.lat = lat;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng getLat() {
        return lat;
    }

    public void setLat(LatLng lat) {
        this.lat = lat;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
