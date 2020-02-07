package com.aeropay_merchant.Model;


import androidx.lifecycle.ViewModel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AP_SDK_SubscriptionPayloadDataForList extends ViewModel {

    @SerializedName("createSyncPayloadAPSDK")
    private List<AP_SDK_CreateSyncPayload> payloadList;

    public List<AP_SDK_CreateSyncPayload> getPayloadList() {
        return payloadList;
    }

    public void setPayloadList(List<AP_SDK_CreateSyncPayload> payloadList) {
        this.payloadList = payloadList;
    }
}
