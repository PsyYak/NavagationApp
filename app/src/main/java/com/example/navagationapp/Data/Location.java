package com.example.navagationapp.Data;

import com.google.android.gms.maps.model.LatLng;

public class Location {

    private String title;
    private String phoneNumber;
    private String address;
    private LatLng lat;

    // empty constructor
    public Location(){

    }

    public Location(String title, String phoneNumber, String address, LatLng lat) {
        this.title = title;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.lat = lat;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLat() {
        return lat;
    }

    public void setLat(LatLng lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "Location{" +
                "title='" + title + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", lat=" + lat +
                '}';
    }
}
