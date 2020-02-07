
package com.aeropay_merchant.Model;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class AP_SDK_Device {

    @SerializedName("majorID")
    private Object mMajorID;
    @SerializedName("merchantLocationDeviceId")
    private BigDecimal mMerchantLocationDeviceId;
    @SerializedName("merchantLocationId")
    private BigDecimal mMerchantLocationId;
    @SerializedName("merchantName")
    private Object mMerchantName;
    @SerializedName("minorID")
    private Object mMinorID;
    @SerializedName("name")
    private Object mName;
    @SerializedName("platform")
    private Object mPlatform;
    @SerializedName("storeName")
    private Object mStoreName;

    public Object getMajorID() {
        return mMajorID;
    }

    public void setMajorID(String majorID) {
        mMajorID = majorID;
    }

    public BigDecimal getMerchantLocationDeviceId() {
        return mMerchantLocationDeviceId;
    }

    public void setMerchantLocationDeviceId(BigDecimal merchantLocationDeviceId) {
        mMerchantLocationDeviceId = merchantLocationDeviceId;
    }

    public BigDecimal getMerchantLocationId() {
        return mMerchantLocationId;
    }

    public void setMerchantLocationId(BigDecimal merchantLocationId) {
        mMerchantLocationId = merchantLocationId;
    }

    public Object getMerchantName() {
        return mMerchantName;
    }

    public void setMerchantName(Object merchantName) {
        mMerchantName = merchantName;
    }

    public Object getMinorID() {
        return mMinorID;
    }

    public void setMinorID(Object minorID) {
        mMinorID = minorID;
    }

    public Object getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Object getPlatform() {
        return mPlatform;
    }

    public void setPlatform(Object platform) {
        mPlatform = platform;
    }

    public Object getStoreName() {
        return mStoreName;
    }

    public void setStoreName(Object storeName) {
        mStoreName = storeName;
    }

}
