
package com.aeropay_merchant.Model;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class AP_SDK_Location {

    @SerializedName("address")
    private Object mAddress;
    @SerializedName("city")
    private Object mCity;
    @SerializedName("latitude")
    private Object mLatitude;
    @SerializedName("longitude")
    private Object mLongitude;
    @SerializedName("merchantLocationId")
    private Object mMerchantLocationId;
    @SerializedName("merchantName")
    private String mMerchantName;
    @SerializedName("name")
    private String mName;
    @SerializedName("postalCode")
    private Object mPostalCode;
    @SerializedName("state")
    private Object mState;

    public Object getAddress() {
        return mAddress;
    }

    public void setAddress(Object address) {
        mAddress = address;
    }

    public Object getCity() {
        return mCity;
    }

    public void setCity(Object city) {
        mCity = city;
    }

    public Object getLatitude() {
        return mLatitude;
    }

    public void setLatitude(Object latitude) {
        mLatitude = latitude;
    }

    public Object getLongitude() {
        return mLongitude;
    }

    public void setLongitude(Object longitude) {
        mLongitude = longitude;
    }

    public Object getMerchantLocationId() {
        return mMerchantLocationId;
    }

    public void setMerchantLocationId(Object merchantLocationId) {
        mMerchantLocationId = merchantLocationId;
    }

    public String getMerchantName() {
        return mMerchantName;
    }

    public void setMerchantName(String merchantName) {
        mMerchantName = merchantName;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Object getPostalCode() {
        return mPostalCode;
    }

    public void setPostalCode(Object postalCode) {
        mPostalCode = postalCode;
    }

    public Object getState() {
        return mState;
    }

    public void setState(Object state) {
        mState = state;
    }

}
