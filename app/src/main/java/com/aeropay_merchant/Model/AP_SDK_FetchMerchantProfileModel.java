
package com.aeropay_merchant.Model;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class AP_SDK_FetchMerchantProfileModel {

    @SerializedName("merchant")
    private AP_SDK_Merchant mAPSDKMerchant;
    @SerializedName("success")
    private Object mSuccess;

    public AP_SDK_Merchant getMerchant() {
        return mAPSDKMerchant;
    }

    public void setMerchant(AP_SDK_Merchant APSDKMerchant) {
        mAPSDKMerchant = APSDKMerchant;
    }

    public Object getSuccess() {
        return mSuccess;
    }

    public void setSuccess(Object success) {
        mSuccess = success;
    }

}
