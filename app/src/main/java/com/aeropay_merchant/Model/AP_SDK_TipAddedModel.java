package com.aeropay_merchant.Model;

import com.google.gson.annotations.SerializedName;

public class AP_SDK_TipAddedModel {

    @SerializedName("totalAmount")
    private String totalAmount;
    @SerializedName("tipAmount")
    private String tipAmount;
    @SerializedName("label")
    private String label;

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(String tipAmount) {
        this.tipAmount = tipAmount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
