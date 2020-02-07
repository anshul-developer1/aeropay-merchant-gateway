package com.aeropay_merchant.activity

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.aeropay_merchant.R
import com.aeropay_merchant.Utilities.AP_SDK_ConstantsStrings
import com.aeropay_merchant.Utilities.AP_SDK_PrefKeeper
import com.aeropay_merchant.communication.AP_SDK_AWSConnectionManager
import com.aeropay_merchant.communication.AP_SDK_DefineID
import com.aeropay_merchant.communication.AP_SDK_ICommunicationHandler

var loader : Dialog? = null
lateinit var idToken : String

open class BaseActivity : AppCompatActivity() , AP_SDK_ICommunicationHandler{

    private var mToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ap_sdk_activity_base)
    }

    //move from one activity to other
    fun launchActivity(activityClass: Class<out BaseActivity>, intent : Intent? = null) {
        if(intent != null){
            startActivity(intent)
            overridePendingTransition(R.anim.right_enter, R.anim.left_exit)
        }
        else {
            startActivity(Intent(this, activityClass))
            overridePendingTransition(R.anim.left_enter, R.anim.right_exit)
        }
    }

    fun showMsgToast(msg: String) {
        if (mToast != null) {
            mToast?.cancel()
            mToast = null
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        mToast?.show()
    }

    // Network callback for success
    override fun onSuccess(outputParms: Int) {
        if(outputParms.equals(AP_SDK_DefineID().FETCH_MERCHANT_PROFILE)){
            if(AP_SDK_PrefKeeper.storeName.equals(AP_SDK_ConstantsStrings().noValue)|| AP_SDK_PrefKeeper.deviceName.equals(AP_SDK_ConstantsStrings().noValue)){
                var awsConnectionManager = AP_SDK_AWSConnectionManager(this)
                awsConnectionManager.hitServer(AP_SDK_DefineID().FETCH_MERCHANT_LOCATIONS,this,null)
            }
            else{
                launchActivity(AP_SDK_HomeActivity::class.java)
            }
        }
        else if(outputParms.equals(AP_SDK_DefineID().FETCH_MERCHANT_LOCATIONS)){
            launchActivity(AP_SDK_SettingsScreenActivity::class.java)
        }
        else if(outputParms.equals(AP_SDK_DefineID().FETCH_MERCHANT_DEVICES)){
            (this as AP_SDK_SettingsScreenActivity).createDeviceSpinner()
        }
        else if(outputParms.equals(AP_SDK_DefineID().FETCH_MERCHANT_PROCESS_TRANSACTION)){
            (this as AP_SDK_HomeActivity).sendProcessTransaction()
        }
        else if(outputParms.equals(AP_SDK_DefineID().REGISTER_MERCHANT_LOCATION_DEVICE)){
            (this as AP_SDK_HomeActivity).creatBeaconTransmission()
        }
    }

    // Network callback for failure
    override fun onFailure(outputParms: Int) {
        if(outputParms.equals(AP_SDK_DefineID().FETCH_MERCHANT_PROFILE)){
            showMsgToast("API Failure")
        }
        else if(outputParms.equals(AP_SDK_DefineID().FETCH_MERCHANT_LOCATIONS)){
            showMsgToast("API Failure")
        }
        else if(outputParms.equals(AP_SDK_DefineID().FETCH_MERCHANT_DEVICES)){
            showMsgToast("API Failure")
        }
        else if(outputParms.equals(AP_SDK_DefineID().REGISTER_MERCHANT_LOCATION_DEVICE)){
            showMsgToast("API Failure")
        }
        else if(outputParms.equals(AP_SDK_DefineID().FETCH_MERCHANT_PROCESS_TRANSACTION)){
            showMsgToast("API Failure")
        }
    }

    // removing the android native back press functionality
    override fun onBackPressed() {

    }
}
