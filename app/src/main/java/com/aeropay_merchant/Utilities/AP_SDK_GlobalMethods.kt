package com.aeropay_merchant.Utilities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import com.aeropay_merchant.R
import com.aeropay_merchant.activity.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.regions.Regions
import com.androidadvance.topsnackbar.TSnackbar
import java.util.regex.Pattern
import com.google.firebase.iid.FirebaseInstanceId
import java.text.SimpleDateFormat
import java.util.*


class AP_SDK_GlobalMethods {

    fun userCognitoLoginHandler(context: Context?, view: View?, userName: String, password: String)
    {
        var cognitoUserPool = CognitoUserPool(context, AP_SDK_ConstantsStrings().aws_userpool_id, AP_SDK_ConstantsStrings().aws_client_id, AP_SDK_ConstantsStrings().aws_client_secret_id,  Regions.US_EAST_1)
        var cognitoUser = cognitoUserPool.getUser()

        var authentication = object : AuthenticationHandler {

            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                idToken = userSession!!.idToken.jwtToken
                (context as AP_SDK_SignInCredentialActivity).onCognitoSuccess()
            }

            override fun onFailure(exception: Exception?) {
                view!!.isClickable = true
                view!!.isEnabled = true
                (context as AP_SDK_SignInCredentialActivity).onCognitoFailure()
            }

            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation?, UserId: String?) {
                var authenticationDetails = AuthenticationDetails(userName,password,null)
                authenticationContinuation!!.setAuthenticationDetails(authenticationDetails)
                authenticationContinuation.continueTask()
            }

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {

            }

            override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                var code = continuation!!.parameters.attributeName
                continuation!!.setMfaCode("1111")
                continuation!!.continueTask();
            }
        }
        cognitoUser.getSessionInBackground(authentication)
    }

    fun showLoader(ctx: Context) {
        if(loader == null){
            loader = Dialog(ctx)
            loader!!.setContentView(com.aeropay_merchant.R.layout.ap_sdk_loader_layout)
            loader!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loader?.setCancelable(false)
            loader!!.show()
        }
    }

    fun dismissLoader() {
        if(loader != null){
            loader!!.dismiss()
            loader = null
        }
    }

    fun showDialog(context: Context?) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.ap_sdk_custom_dialog)
        val autoLoginBtn = dialog.findViewById(R.id.autoLoginImage) as ImageView
        val pinLoginBtn = dialog.findViewById(R.id.pinLoginImage) as ImageView
        val cancelImageView = dialog.findViewById(R.id.pinLoginText) as ImageView

        autoLoginBtn.setOnClickListener(View.OnClickListener {
            AP_SDK_PrefKeeper.isLoggedIn = true
            AP_SDK_PrefKeeper.isPinEnabled = false
            dialog.dismiss()
        })

        pinLoginBtn.setOnClickListener(View.OnClickListener {
            var pinValue = AP_SDK_PrefKeeper.pinValue
            if(pinValue.equals(AP_SDK_ConstantsStrings().noValue)){
                var intent = Intent(context,AP_SDK_PinEnterActivity::class.java)
                intent.putExtra(AP_SDK_ConstantsStrings().isPinActivityName,1)
                (context as AP_SDK_HomeActivity).launchActivity(AP_SDK_PinEnterActivity::class.java,intent)
                //(context as AP_SDK_HomeActivity).launchActivity(SetPinLogin::class.java)
            }
            else {
                AP_SDK_PrefKeeper.isPinEnabled = true
                AP_SDK_PrefKeeper.isLoggedIn = false
            }
            dialog.dismiss()
        })

        cancelImageView.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        dialog.show()
    }

    fun autoLoginAction(context : Context?,username : String, password: String, isEntryPoint : String){
        var cognitoUserPool = CognitoUserPool(context, AP_SDK_ConstantsStrings().aws_userpool_id, AP_SDK_ConstantsStrings().aws_client_id, AP_SDK_ConstantsStrings().aws_client_secret_id,  Regions.US_EAST_1)
        var cognitoUser = cognitoUserPool.getUser()

        var authentication = object : AuthenticationHandler {

            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                idToken = userSession!!.idToken.jwtToken
                if(isEntryPoint.equals(AP_SDK_ConstantsStrings().isValidatePinActivity)){
                    (context as AP_SDK_PinEnterActivity).onCognitoSuccess()
                }
                else if(isEntryPoint.equals(AP_SDK_ConstantsStrings().isSplashActivity)){
                    (context as AP_SDK_SplashActivity).onCognitoSuccess()
                }
                else if(isEntryPoint.equals(AP_SDK_ConstantsStrings().isSignInActivity)){
                    (context as AP_SDK_SignInScreenActivity).onCognitoSuccess()
                }
            }

            override fun onFailure(exception: Exception?) {
                if(isEntryPoint.equals(AP_SDK_ConstantsStrings().isValidatePinActivity)){
                    (context as AP_SDK_PinEnterActivity).onCognitoFailure()
                }
                else if(isEntryPoint.equals(AP_SDK_ConstantsStrings().isSplashActivity)){
                    (context as AP_SDK_SplashActivity).onCognitoFailure()
                }
                else if(isEntryPoint.equals(AP_SDK_ConstantsStrings().isSignInActivity)){
                    (context as AP_SDK_SignInScreenActivity).onCognitoFailure()
                }
            }

            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation?, UserId: String?) {
                var authenticationDetails = AuthenticationDetails(username,password,null)
                authenticationContinuation!!.setAuthenticationDetails(authenticationDetails)
                authenticationContinuation.continueTask()
            }

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {

            }

            override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                var code = continuation!!.parameters.attributeName
                continuation!!.setMfaCode("1111")
                continuation!!.continueTask();
            }
        }
        cognitoUser.getSessionInBackground(authentication)
    }

    fun isValidEmailId(email: String): Boolean {
        var isEmail = Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches()
        return isEmail
    }

    fun getDeviceToken(context : Context?) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful)
                AP_SDK_PrefKeeper.deviceToken = task.result!!.token
            var a = AP_SDK_PrefKeeper.deviceToken
            Log.d("Aeropay token",a )
        }
    }

    fun createSnackBar(view: View? ,message: String?){
        var snackbar = TSnackbar.make(view!!, message!!, TSnackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.BLACK)
        snackbar.setIconRight(android.R.drawable.ic_menu_close_clear_cancel, 36F)
        snackbar.setIconPadding(8)
        snackbar.setMaxWidth(3000)
        var snackbarView = snackbar.getView()
        snackbarView.setBackgroundColor(Color.parseColor("#34c1d7"))
        var textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.BLACK)
        textView.textSize = 18F
        snackbar.show()
    }

    fun checkConnection(mContext : Context?): Boolean {
        val connectivityManager = mContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}