package com.aeropay_merchant.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.aeropay_merchant.R
import com.aeropay_merchant.Utilities.*
import com.aeropay_merchant.communication.AP_SDK_AWSConnectionManager
import com.aeropay_merchant.communication.AP_SDK_DefineID

class AP_SDK_SplashActivity : BaseActivity() {

    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ap_sdk_activity_splash)
        AP_SDK_PrefKeeper.init(this)
        navigateToScreen()
    }

    //method to check for auto login/pin login or normal login and redirection
    private fun navigateToScreen() {
        if(AP_SDK_PrefKeeper.isLoggedIn){
            var usernameEncryptValue = AP_SDK_PrefKeeper.username
            var passwordEncryptValue = AP_SDK_PrefKeeper.password

            if(AP_SDK_GlobalMethods().checkConnection(this)){
                AP_SDK_GlobalMethods().showLoader(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var usernameIvEncryptValue = AP_SDK_PrefKeeper.usernameIV
                    var passwordIvEncryptValue = AP_SDK_PrefKeeper.passwordIV
                    var username = AP_SDK_KeystoreManager.decryptText(usernameEncryptValue!!,usernameIvEncryptValue!!,AP_SDK_KeystoreManager.ALIAS.USERNAME)
                    var password = AP_SDK_KeystoreManager.decryptText(passwordEncryptValue!!,passwordIvEncryptValue!!,AP_SDK_KeystoreManager.ALIAS.PWD)

                    AP_SDK_GlobalMethods().autoLoginAction(this@AP_SDK_SplashActivity,username!!,password!!, AP_SDK_ConstantsStrings().isSplashActivity)
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    var username = KeyStoreHelper.decrypt(AP_SDK_KeystoreManager.ALIAS.USERNAME,usernameEncryptValue)
                    var password = KeyStoreHelper.decrypt(AP_SDK_KeystoreManager.ALIAS.PWD,passwordEncryptValue)
                    AP_SDK_GlobalMethods().autoLoginAction(this@AP_SDK_SplashActivity,username!!,password!!,AP_SDK_ConstantsStrings().isSplashActivity)
                }
            }
            else{
                showMsgToast("Please check your internet connection")
            }
        }
        else{
            mDelayHandler = Handler()
            mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
        }

    }

    //to show Splash for 3 seconds
    internal val mRunnable: Runnable = Runnable {
         if(AP_SDK_PrefKeeper.isPinEnabled) {
             var intent = Intent(this@AP_SDK_SplashActivity,AP_SDK_PinEnterActivity::class.java)
             intent.putExtra(AP_SDK_ConstantsStrings().isPinActivityName,3)
             launchActivity(AP_SDK_PinEnterActivity::class.java,intent)
         }
        else {
             launchActivity(AP_SDK_SignInScreenActivity::class.java)
             finish()
         }
    }

    // to get callback for login success
    fun onCognitoSuccess(){
        var awsConnectionManager = AP_SDK_AWSConnectionManager(this)
        awsConnectionManager.hitServer(AP_SDK_DefineID().FETCH_MERCHANT_PROFILE,this,null)
    }

    // to get callback for login failure
    fun onCognitoFailure(){
        AP_SDK_GlobalMethods().dismissLoader()
        Toast.makeText(this,"Auto-Login was not successfull", Toast.LENGTH_LONG).show()
        launchActivity(AP_SDK_SignInScreenActivity::class.java)
    }
}
