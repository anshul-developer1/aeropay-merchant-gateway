package com.aeropay_merchant.activity

import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import com.aeropay_merchant.Model.AP_SDK_AeropayModelManager
import com.aeropay_merchant.R
import com.aeropay_merchant.Utilities.*
import com.aeropay_merchant.adapter.AP_SDK_NavigationMenuAdapter
import com.aeropay_merchant.communication.AP_SDK_AWSConnectionManager
import com.aeropay_merchant.communication.AP_SDK_DefineID

class AP_SDK_NavigationMenuActivity : BaseActivity() {

    lateinit var menuListView : ListView
    lateinit var logout : Button
    lateinit var merchantName : TextView
    lateinit var merchantEmail : TextView
    lateinit var merchantStore : TextView
    lateinit var backButton : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ap_sdk_activity_navigation_menu)
        initialiseControls()
        setListeners()

    }

    // setting on click listeners for UI
    private fun setListeners() {
        menuListView.setOnItemClickListener { parent, view, position, id ->
            if(position == 0){
                if(AP_SDK_GlobalMethods().checkConnection(this)){
                    var awsConnectionManager = AP_SDK_AWSConnectionManager(this)
                    awsConnectionManager.hitServer(AP_SDK_DefineID().FETCH_MERCHANT_LOCATIONS,this,null)
                }
                else{
                    showMsgToast("Please check your Internet Connection")
                }

            }
            else if(position == 1){
                launchActivity(AP_SDK_FastLoginActivity::class.java)
            }
        }

        backButton.setOnClickListener(View.OnClickListener {
            finish()
        })
    }


    // inflating UI controls
    private fun initialiseControls() {
        menuListView = findViewById(R.id.itemsList)
        logout = findViewById(R.id.logoutButton)
        merchantName = findViewById(R.id.merchantNameText)
        merchantEmail = findViewById(R.id.merchantEmail)
        merchantStore = findViewById(R.id.storeNameText)
        backButton = findViewById(R.id.back_button)


        var itemsNameArray =  resources?.getStringArray(R.array.navigation_items)
        var itemsImageArray =  arrayOf(R.drawable.settings, R.drawable.timer)
        val itemsListView = AP_SDK_NavigationMenuAdapter(this,itemsNameArray!!,itemsImageArray)
        menuListView ?.adapter = itemsListView

        var objModelManager = AP_SDK_AeropayModelManager().getInstance()
        merchantName.text = objModelManager.merchantProfileModelAPSDK.merchant.name.toString()

        var usernameEncryptValue = AP_SDK_PrefKeeper.username

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var usernameIvEncryptValue = AP_SDK_PrefKeeper.usernameIV
            var username = AP_SDK_KeystoreManager.decryptText(usernameEncryptValue!!,usernameIvEncryptValue!!, AP_SDK_KeystoreManager.ALIAS.USERNAME)
            merchantEmail.text = username
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            var usernameEncryptValue = AP_SDK_PrefKeeper.username
            var username = KeyStoreHelper.decrypt(AP_SDK_KeystoreManager.ALIAS.USERNAME,usernameEncryptValue)
            merchantEmail.text = username
        }

        var storeName = AP_SDK_PrefKeeper.storeName
        if(storeName.equals(AP_SDK_ConstantsStrings().noValue))
        {
            merchantStore.text = ""
        }
        else{
            merchantStore.text = storeName
        }
    }

    // sign out button click event
    fun onLogoutButtonClick(view: View) {
        if(AP_SDK_PrefKeeper.isPinEnabled || AP_SDK_PrefKeeper.isLoggedIn){
            finishAffinity()
            launchActivity(AP_SDK_SignInScreenActivity::class.java)
        }
        else{
            var usernameEncryptValue = AP_SDK_PrefKeeper.username

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var usernameIvEncryptValue = AP_SDK_PrefKeeper.usernameIV
                AP_SDK_PrefKeeper.clear()
                var username = AP_SDK_KeystoreManager.decryptText(usernameEncryptValue!!,usernameIvEncryptValue!!, AP_SDK_KeystoreManager.ALIAS.USERNAME)
                savingCredentialInkeystore(username)
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                var usernameEncryptValue = AP_SDK_PrefKeeper.username
                AP_SDK_PrefKeeper.clear()
                var username = KeyStoreHelper.decrypt(AP_SDK_KeystoreManager.ALIAS.USERNAME,usernameEncryptValue)
                savingCredentialInkeystore(username)
            }

            finishAffinity()
            launchActivity(AP_SDK_SignInScreenActivity::class.java)
        }
    }


    private fun savingCredentialInkeystore(username: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var usernameArray = AP_SDK_KeystoreEncryptor().encryptText(AP_SDK_KeystoreManager.ALIAS.USERNAME, username!!)
            AP_SDK_PrefKeeper.username = Base64.encodeToString(usernameArray, Base64.DEFAULT)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            KeyStoreHelper.createKeys(this, AP_SDK_KeystoreManager.ALIAS.USERNAME)
            KeyStoreHelper.createKeys(this, AP_SDK_KeystoreManager.ALIAS.PWD)
            AP_SDK_PrefKeeper.username = KeyStoreHelper.encrypt(AP_SDK_KeystoreManager.ALIAS.USERNAME, username)
        }
    }
}
