
package com.aeropay_merchant.Model;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class AP_SDK_FetchMerchantLocationModel {

    @SerializedName("locations")
    private List<AP_SDK_Location> mAPSDKLocations;
    @SerializedName("success")
    private Object mSuccess;

    public List<AP_SDK_Location> getLocations() {
        return mAPSDKLocations;
    }

    public void setLocations(List<AP_SDK_Location> APSDKLocations) {
        mAPSDKLocations = APSDKLocations;
    }

    public Object getSuccess() {
        return mSuccess;
    }

    public void setSuccess(Object success) {
        mSuccess = success;
    }

}
