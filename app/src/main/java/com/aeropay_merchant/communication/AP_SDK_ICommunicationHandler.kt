package com.aeropay_merchant.communication

interface AP_SDK_ICommunicationHandler {

    fun onSuccess(outputParms: Int)

    fun onFailure(outputParms: Int)

}