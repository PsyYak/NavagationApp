package com.example.navagationapp.Data;

import java.util.Date;

public class Donation extends Donor{

    private float donationAmount;
    private String receiptNumber;
    private String comments;
    private Date dateCompleted;
    private boolean isCompleted;
    private String donationID;

    public Donation(){}

    public Donation(float donationAmount, String receiptNumber, String comments, Date dateCompleted, boolean isCompleted,String donationID) {
        this.donationAmount = donationAmount;
        this.receiptNumber = receiptNumber;
        this.comments = comments;
        this.dateCompleted = dateCompleted;
        this.isCompleted = isCompleted;
        this.donationID = donationID;
    }

    public float getDonationAmount() {
        return donationAmount;
    }

    public void setDonationAmount(float donationAmount) {
        this.donationAmount = donationAmount;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }


    @Override
    public String getDonationID() {
        return donationID;
    }

    @Override
    public void setDonationID(String donationID) {
        this.donationID = donationID;
    }
}
