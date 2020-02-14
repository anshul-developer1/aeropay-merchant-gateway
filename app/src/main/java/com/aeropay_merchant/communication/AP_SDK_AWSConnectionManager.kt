package com.aeropay_merchant.communication

import AP.AeroPayStagingClient
import android.content.Context
import AP.model.*
import android.os.AsyncTask
import com.aeropay_merchant.Utilities.AP_SDK_ConstantsStrings
import com.aeropay_merchant.Utilities.AP_SDK_GlobalMethods
import com.aeropay_merchant.activity.idToken
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.google.gson.Gson
import android.content.pm.ActivityInfo
import android.app.Activity
import android.content.res.Configuration
import com.aeropay_merchant.Model.*
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory
import java.lang.Exception


class AP_SDK_AWSConnectionManager  {

    var mContext: Context? = null

    constructor(context: Context) {
        this.mContext = context
    }

    fun hitServer(requestID: Int, callbackHandlerAPSDK: AP_SDK_ICommunicationHandler, requestObject : Any?) {
        object : AsyncTask<Void, Void, Any>() {

            override fun onPreExecute() {
                super.onPreExecute()
                lockScreenOrientation()
                AP_SDK_GlobalMethods().showLoader(mContext!!)
            }

            override fun doInBackground(vararg p0: Void?): Any? {

                try {
                    var credentialsProvider = object : CognitoCachingCredentialsProvider(
                        mContext,
                        AP_SDK_ConstantsStrings().aws_identitypool_id,
                        Regions.US_EAST_1
                    ) {}

                    var logins = HashMap<String, String>()
                    logins.put(AP_SDK_ConstantsStrings().userPoolLoginType, idToken);
                    credentialsProvider.setLogins(logins);

                    var factory =
                        object : ApiClientFactory() {}.credentialsProvider(credentialsProvider)

                    var client = factory.build(AeroPayStagingClient::class.java)

                    var output: Any? = null

                    if (requestID.equals(AP_SDK_DefineID().FETCH_MERCHANT_PROFILE)) {
                        output = client.fetchMerchantPost()
                    } else if (requestID.equals(AP_SDK_DefineID().FETCH_MERCHANT_LOCATIONS)) {
                        output = client.fetchMerchantLocationsPost()
                    } else if (requestID.equals(AP_SDK_DefineID().FETCH_MERCHANT_DEVICES)) {
                        var merchantLocationDevice = requestObject as MerchantLocationDevices
                        output = client.fetchMerchantLocationDevicesPost(merchantLocationDevice)
                    } else if (requestID.equals(AP_SDK_DefineID().REGISTER_MERCHANT_LOCATION_DEVICE)) {
                        var registerDevice = requestObject as RegisterMerchantDevice
                        output = client.registerMerchantLocationDevicePost(registerDevice)
                    } else if (requestID.equals(AP_SDK_DefineID().FETCH_MERCHANT_PROCESS_TRANSACTION)) {
                        var processTransaction = requestObject as ProcessTransaction
                        output = client.sendBillTransactionPost(processTransaction)
                    }
                    return output as Any
                }
                catch (e : Exception){
                    return null
                }
            }

            override fun onPostExecute(result: Any?) {
                super.onPostExecute(result)
                AP_SDK_GlobalMethods().dismissLoader()
                if (result != null) {
                    var objModelManager = AP_SDK_AeropayModelManager().getInstance()
                    if (requestID.equals(AP_SDK_DefineID().FETCH_MERCHANT_PROFILE)) {
                        var output = result as MerchantResponse
                        var statusCode = output.success.toString()
                        if (statusCode.equals("1")) {
                            var stringOutput = Gson().toJson(output)
                            objModelManager.merchantProfileModelAPSDK = Gson().fromJson(
                                stringOutput,
                                AP_SDK_FetchMerchantProfileModel::class.java
                            )
                            callbackHandlerAPSDK.onSuccess(requestID)
                        } else {
                            var errorMessage = output.error.toString()
                            callbackHandlerAPSDK.onFailure(requestID, errorMessage)
                        }
                    } else if (requestID.equals(AP_SDK_DefineID().FETCH_MERCHANT_LOCATIONS)) {
                        var output = result as MerchantLocationsResponse
                        var statusCode = output.success.toString()
                        if (statusCode.equals("1")) {
                            var stringOutput = Gson().toJson(output)
                            objModelManager.merchantLocationsModelAPSDK = Gson().fromJson(
                                stringOutput,
                                AP_SDK_FetchMerchantLocationModel::class.java
                            )
                            callbackHandlerAPSDK.onSuccess(requestID)
                        } else {
                            callbackHandlerAPSDK.onFailure(requestID, "API Failure")
                        }
                    } else if (requestID.equals(AP_SDK_DefineID().FETCH_MERCHANT_DEVICES)) {
                        var output = result as MerchantLocationDevicesResponse
                        var statusCode = output.success.toString()
                        if (statusCode.equals("1")) {
                            var stringOutput = Gson().toJson(output)
                            objModelManager.merchantDevicesModelAPSDK = Gson().fromJson(
                                stringOutput,
                                AP_SDK_FetchMerchantDevicesList::class.java
                            )
                            callbackHandlerAPSDK.onSuccess(requestID)
                        } else {
                            callbackHandlerAPSDK.onFailure(requestID, "API Failure")
                        }
                    } else if (requestID.equals(AP_SDK_DefineID().REGISTER_MERCHANT_LOCATION_DEVICE)) {
                        var output = result as RegisterMerchantDeviceResponse
                        var statusCode = output.success.toString()
                        if (statusCode.equals("1")) {
                            var stringOutput = Gson().toJson(output)
                            objModelManager.APSDKRegisterMerchantDevices = Gson().fromJson(
                                stringOutput,
                                AP_SDK_RegisterMerchantDeviceResponse::class.java
                            )
                            callbackHandlerAPSDK.onSuccess(requestID)
                        } else {
                            var errorMessage = output.error.toString()
                            callbackHandlerAPSDK.onFailure(requestID, errorMessage)
                        }
                    } else if (requestID.equals(AP_SDK_DefineID().FETCH_MERCHANT_PROCESS_TRANSACTION)) {
                        var output = result as StandardResponse
                        var statusCode = output.success.toString()
                        if (statusCode.equals("1")) {
                            callbackHandlerAPSDK.onSuccess(requestID)
                        } else {
                            var errorMessage = output.error.toString()
                            callbackHandlerAPSDK.onFailure(requestID, errorMessage)
                        }
                    }
                    unlockScreenOrientation()
                }
            else{
                callbackHandlerAPSDK.onFailure(-1,"Something went wrong.Please try again.")
            }
            }
        }.execute()}

    private fun lockScreenOrientation() {
        val currentOrientation = mContext!!.getResources().getConfiguration().orientation
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            (mContext as Activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } else {
            (mContext as Activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }
    }

    private fun unlockScreenOrientation() {
        (mContext as Activity).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)
    }
}