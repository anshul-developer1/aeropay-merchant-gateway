package com.aeropay_merchant.ViewModel

import androidx.lifecycle.ViewModel
import com.amazonaws.amplify.generated.graphql.OnCreateMerchantSyncSubscription
import com.apollographql.apollo.api.Response

class AP_SDK_HomeViewModel : ViewModel() {

    val userName: ArrayList<String> = ArrayList()
    val userListDetails: ArrayList<String> = ArrayList()
    var numberOfConsumers : Int? = 0
    var userEntered: String? = ""
    lateinit var mResponse: Response<OnCreateMerchantSyncSubscription.Data>

    fun setValues(response: Response<OnCreateMerchantSyncSubscription.Data>){
        userName.add(response.data()!!.onCreateMerchantSync()!!.payload()!!)
    }

    fun getValues() : ArrayList<String>{
        return userName
    }

    fun setListValues(response: Response<OnCreateMerchantSyncSubscription.Data>){
        userListDetails.add(response.data()!!.onCreateMerchantSync()!!.payload()!!)
    }

    fun getListValues() : ArrayList<String>{
        return userListDetails
    }

    fun setPayload(response: Response<OnCreateMerchantSyncSubscription.Data>){
        mResponse = response
    }

    fun getPayload() : Response<OnCreateMerchantSyncSubscription.Data>{
        return mResponse
    }

}