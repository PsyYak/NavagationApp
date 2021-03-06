package com.example.navagationapp.Data;

public class Donor extends Location{

    private String contactName;
    private String businessTitle;
    private String phoneNumber;
    private String address;
    private String donationID;

    public Donor(){}


    public Donor(String contactName, String businessTitle, String phoneNumber, String address, String donationID) {
        this.contactName = contactName;
        this.businessTitle = businessTitle;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.donationID = donationID;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getBusinessTitle() {
        return businessTitle;
    }

    public void setBusinessTitle(String businessTitle) {
        this.businessTitle = businessTitle;
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

    public String getDonationID() {
        return donationID;
    }

    public void setDonationID(String donationID) {
        this.donationID = donationID;
    }
}
