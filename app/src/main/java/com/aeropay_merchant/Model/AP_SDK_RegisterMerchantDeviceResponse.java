
package com.aeropay_merchant.Model;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class AP_SDK_RegisterMerchantDeviceResponse {

    @SerializedName("majorID")
    private Double mMajorID;
    @SerializedName("minorID")
    private Double mMinorID;
    @SerializedName("success")
    private Object mSuccess;
    @SerializedName("UUID")
    private Object mUUID;

    public Double getMajorID() {
        return mMajorID;
    }

    public void setMajorID(Double majorID) {
        mMajorID = majorID;
    }

    public Double getMinorID() {
        return mMinorID;
    }

    public void setMinorID(Double minorID) {
        mMinorID = minorID;
    }

    public Object getSuccess() {
        return mSuccess;
    }

    public void setSuccess(Object success) {
        mSuccess = success;
    }

    public Object getUUID() {
        return mUUID;
    }

    public void setUUID(Object uUID) {
        mUUID = uUID;
    }

}
