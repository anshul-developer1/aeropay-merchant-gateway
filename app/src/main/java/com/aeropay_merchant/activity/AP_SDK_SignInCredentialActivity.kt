package com.aeropay_merchant.activity

import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.aeropay_merchant.R
import com.aeropay_merchant.Utilities.*
import com.aeropay_merchant.communication.AP_SDK_AWSConnectionManager
import com.aeropay_merchant.communication.AP_SDK_DefineID
import com.aeropay_merchant.view.AP_SDK_CustomEditText
import android.widget.ImageView


class AP_SDK_SignInCredentialActivity : BaseActivity(){

    lateinit var userNameEditAPSDK : AP_SDK_CustomEditText
    lateinit var passwordEditAPSDK : AP_SDK_CustomEditText
    lateinit var signInButton : ImageView
    lateinit var userName : String
    lateinit var password : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ap_sdk_activity_sign_in_credential)

        initialiseControls()
    }

    private fun initialiseControls() {
        userNameEditAPSDK = findViewById(R.id.userEmail)
        passwordEditAPSDK = findViewById(R.id.userPassword)
        signInButton = findViewById(R.id.signInButton)

        var userNameValue = AP_SDK_PrefKeeper.username
        if(!(userNameValue.equals(AP_SDK_ConstantsStrings().noValue))){
            var usernameEncryptValue = AP_SDK_PrefKeeper.username

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var usernameIvEncryptValue = AP_SDK_PrefKeeper.usernameIV
                var username = AP_SDK_KeystoreManager.decryptText(usernameEncryptValue!!,usernameIvEncryptValue!!, AP_SDK_KeystoreManager.ALIAS.USERNAME)
                userNameEditAPSDK.setText(username)
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                var usernameEncryptValue = AP_SDK_PrefKeeper.username
                var username = KeyStoreHelper.decrypt(AP_SDK_KeystoreManager.ALIAS.USERNAME,usernameEncryptValue)
                userNameEditAPSDK.setText(username)
            }
        }
    }

    // check for email and password validations for Login
    fun createUserValidation(view: View) {
         userName = userNameEditAPSDK.text.toString()
         password = passwordEditAPSDK.text.toString()
         /*userName = "daniel.muller@aeropayments.com"
         password = "Password*12345"*/

        if(userName.trim().isNullOrEmpty() || password!!.trim().isNullOrEmpty()){
            Toast.makeText(this,"Please enter Email and password.",Toast.LENGTH_SHORT).show()
        }
        else if(userName.trim().isNullOrEmpty()){
            Toast.makeText(this,"Please enter your Email.",Toast.LENGTH_SHORT).show()
        }
        else if(password!!.trim().isNullOrEmpty()){
            Toast.makeText(this,"Please enter your password.",Toast.LENGTH_SHORT).show()
        }
        else if(!(AP_SDK_GlobalMethods().isValidEmailId(userName))){
            Toast.makeText(this,"Please enter a valid Email ID.",Toast.LENGTH_SHORT).show()
        }
        else {
            if (AP_SDK_GlobalMethods().checkConnection(this)) {
                AP_SDK_GlobalMethods().showLoader(this)
                view.isClickable = false
                view.isEnabled = false
                AP_SDK_GlobalMethods().userCognitoLoginHandler(this@AP_SDK_SignInCredentialActivity, view, userName, password)
            } else {
                showMsgToast("Please check your Internet Connection")
            }
        }
    }

    //callback for AWS Login success
    fun onCognitoSuccess(){
        savingCredentialInkeystore()
        var awsConnectionManager = AP_SDK_AWSConnectionManager(this)
        awsConnectionManager.hitServer(AP_SDK_DefineID().FETCH_MERCHANT_PROFILE,this,null)
    }

    //saving Login Credentials in Key Store
    private fun savingCredentialInkeystore() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var usernameArray = AP_SDK_KeystoreEncryptor().encryptText(AP_SDK_KeystoreManager.ALIAS.USERNAME, userName)
                AP_SDK_PrefKeeper.username = Base64.encodeToString(usernameArray, Base64.DEFAULT)
                var passwordArray = AP_SDK_KeystoreEncryptor().encryptText(AP_SDK_KeystoreManager.ALIAS.PWD, password)
                AP_SDK_PrefKeeper.password = Base64.encodeToString(passwordArray, Base64.DEFAULT)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                KeyStoreHelper.createKeys(this, AP_SDK_KeystoreManager.ALIAS.USERNAME)
                KeyStoreHelper.createKeys(this, AP_SDK_KeystoreManager.ALIAS.PWD)
                AP_SDK_PrefKeeper.username = KeyStoreHelper.encrypt(AP_SDK_KeystoreManager.ALIAS.USERNAME, userName)
                AP_SDK_PrefKeeper.password = KeyStoreHelper.encrypt(AP_SDK_KeystoreManager.ALIAS.PWD, password)
            }
        }

    //callback for AWS Login failure
    fun onCognitoFailure(){
        AP_SDK_GlobalMethods().dismissLoader()
        Toast.makeText(this,"Invalid username or password", Toast.LENGTH_LONG).show()
    }
}
