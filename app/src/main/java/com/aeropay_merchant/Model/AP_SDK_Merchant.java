
package com.aeropay_merchant.Model;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class AP_SDK_Merchant {

    @SerializedName("createBusiness")
    private Object mCreateBusiness;
    @SerializedName("customerId")
    private Object mCustomerId;
    @SerializedName("isAuthorized")
    private Object mIsAuthorized;
    @SerializedName("merchantId")
    private int mMerchantId;
    @SerializedName("name")
    private Object mName;

    public Object getCreateBusiness() {
        return mCreateBusiness;
    }

    public void setCreateBusiness(Object createBusiness) {
        mCreateBusiness = createBusiness;
    }

    public Object getCustomerId() {
        return mCustomerId;
    }

    public void setCustomerId(Object customerId) {
        mCustomerId = customerId;
    }

    public Object getIsAuthorized() {
        return mIsAuthorized;
    }

    public void setIsAuthorized(Object isAuthorized) {
        mIsAuthorized = isAuthorized;
    }

    public int getMerchantId() {
        return mMerchantId;
    }

    public void setMerchantId(int merchantId) {
        mMerchantId = merchantId;
    }

    public Object getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

}
