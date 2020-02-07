
package com.aeropay_merchant.Model;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class AP_SDK_FetchMerchantDevicesList {

    @SerializedName("devices")
    private List<AP_SDK_Device> mAPSDKDevices;
    @SerializedName("success")
    private Object mSuccess;

    public List<AP_SDK_Device> getDevices() {
        return mAPSDKDevices;
    }

    public void setDevices(List<AP_SDK_Device> APSDKDevices) {
        mAPSDKDevices = APSDKDevices;
    }

    public Object getSuccess() {
        return mSuccess;
    }

    public void setSuccess(Object success) {
        mSuccess = success;
    }

}
