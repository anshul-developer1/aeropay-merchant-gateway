package com.aeropay_merchant.Model

class AP_SDK_AeropayModelManager {

    companion object modelManager {
        var objModelManagerAPSDK: AP_SDK_AeropayModelManager? = null
    }

    var merchantLocationsModelAPSDK: AP_SDK_FetchMerchantLocationModel =
        AP_SDK_FetchMerchantLocationModel()
    var merchantDevicesModelAPSDK: AP_SDK_FetchMerchantDevicesList =
        AP_SDK_FetchMerchantDevicesList()
    var merchantProfileModelAPSDK: AP_SDK_FetchMerchantProfileModel =
        AP_SDK_FetchMerchantProfileModel()
    var APSDKRegisterMerchantDevices: AP_SDK_RegisterMerchantDeviceResponse =
        AP_SDK_RegisterMerchantDeviceResponse()
    var createSyncPayloadAPSDK: AP_SDK_SubscriptionPayload =
        AP_SDK_SubscriptionPayload()
    var APSDKSubscriptionPayloadForList: AP_SDK_SubscriptionPayloadDataForList =
        AP_SDK_SubscriptionPayloadDataForList()

    constructor() {}

    fun getInstance(): AP_SDK_AeropayModelManager {

        if (objModelManagerAPSDK != null) {
            return objModelManagerAPSDK as AP_SDK_AeropayModelManager

        } else {
            objModelManagerAPSDK = AP_SDK_AeropayModelManager()
            return objModelManagerAPSDK as AP_SDK_AeropayModelManager
        }
    }

}