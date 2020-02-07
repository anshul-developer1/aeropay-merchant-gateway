package com.aeropay_merchant.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AP_SDK_CreateSyncPayload {
    @SerializedName("userName")
    private String userName;
    @SerializedName("APStatus")
    private String status;
    @SerializedName("transactionId")
    private String transactionId;
    @SerializedName("profileImage")
    private String profileImage;
    @SerializedName("expirationTime")
    private String expirationTime;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }
}
