package com.aeropay_merchant.activity

import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import com.aeropay_merchant.R
import com.aeropay_merchant.Utilities.*
import com.aeropay_merchant.communication.AP_SDK_AWSConnectionManager
import com.aeropay_merchant.communication.AP_SDK_DefineID
import com.aeropay_merchant.view.AP_SDK_CustomTextView

class AP_SDK_PinEnterActivity : BaseActivity() {

    var userEntered: String? = null
    var userPin = ""
    lateinit var backButton : ImageView
    var activityName : Int? = null

    val PIN_LENGTH = 4
    var keyPadLockedFlag = false

    var pinBox0: TextView? = null
    var pinBox1: TextView? = null
    var pinBox2: TextView? = null
    var pinBox3: TextView? = null

    var view1: View? = null
    var view2: View? = null
    var view3: View? = null
    var view4: View? = null

    var pinBoxArray: Array<TextView?>? = null
    var pinConfirmationLayout: RelativeLayout? = null

    var button0: AP_SDK_CustomTextView? = null
    var button1: AP_SDK_CustomTextView? = null
    var button2: AP_SDK_CustomTextView? = null
    var button3: AP_SDK_CustomTextView? = null
    var button4: AP_SDK_CustomTextView? = null
    var button5: AP_SDK_CustomTextView? = null
    var button6: AP_SDK_CustomTextView? = null
    var button7: AP_SDK_CustomTextView? = null
    var button8: AP_SDK_CustomTextView? = null
    var button9: AP_SDK_CustomTextView? = null
    var buttonDelete: AP_SDK_CustomTextView? = null
    var screenHeading: AP_SDK_CustomTextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userEntered = ""
        setContentView(R.layout.ap_sdk_activity_validate_pin)

        screenHeading = findViewById(R.id.screenHeading)

        var intent = intent
        activityName = intent.getIntExtra(AP_SDK_ConstantsStrings().isPinActivityName,0)

        if(activityName == 1){
            screenHeading!!.text = "Set pin code"
            userPin = ""
        }
        else if(activityName == 2){
            screenHeading!!.text = "Confirm pin code"
            userPin = intent.getStringExtra("PIN VALUE")
        }
        else if(activityName == 3){
            screenHeading!!.text = "Login with pin"
            userPin = AP_SDK_PrefKeeper.pinValue!!
        }
        initialiseControls()
    }

    private fun initialiseControls() {
        view1 = findViewById<View>(R.id.pin1)
        view2 = findViewById<View>(R.id.pin2)
        view3 = findViewById<View>(R.id.pin3)
        view4 = findViewById<View>(R.id.pin4)

        val pinButtonHandler = View.OnClickListener { v ->
            if (keyPadLockedFlag == true) {
                return@OnClickListener
            }

            val pressedButton = v as AP_SDK_CustomTextView

            if (userEntered!!.length < PIN_LENGTH) {
                userEntered = userEntered + pressedButton.text
                pinBoxArray!![userEntered!!.length - 1]!!.text = "8"

                var length = userEntered!!.length

                if(length == 1){
                    view1!!.visibility = View.INVISIBLE
                }
                else if(length == 2){
                    view2!!.visibility = View.INVISIBLE
                }
                else if(length == 3){
                    view3!!.visibility = View.INVISIBLE
                }
                else if(length == 4){
                    view4!!.visibility = View.INVISIBLE
                }

                if(activityName == 1){
                if (userEntered!!.length == PIN_LENGTH) {
                    var intent = Intent(this@AP_SDK_PinEnterActivity,AP_SDK_PinEnterActivity::class.java)
                    intent.putExtra("PIN VALUE",userEntered)
                    intent.putExtra(AP_SDK_ConstantsStrings().isPinActivityName,2)
                    launchActivity(AP_SDK_PinEnterActivity::class.java,intent)
                }
                }

                else {
                    if (userEntered!!.length == PIN_LENGTH) {
                        if (userEntered == userPin) {
                            keyPadLockedFlag = true
                            LockKeyPadOperation().execute("")

                            if (activityName == 2) {
                                backButton.visibility = View.GONE
                                pinConfirmationLayout!!.visibility = View.VISIBLE
                                Handler().postDelayed({
                                    moveToHomeActivity()
                                }, 2000)
                            } else if (activityName == 3) {
                                AP_SDK_PrefKeeper.pinValue = userPin
                                var usernameEncryptValue = AP_SDK_PrefKeeper.username
                                var passwordEncryptValue = AP_SDK_PrefKeeper.password

                                AP_SDK_GlobalMethods().showLoader(this)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    var usernameIvEncryptValue = AP_SDK_PrefKeeper.usernameIV
                                    var passwordIvEncryptValue = AP_SDK_PrefKeeper.passwordIV
                                    var username = AP_SDK_KeystoreManager.decryptText(
                                        usernameEncryptValue!!,
                                        usernameIvEncryptValue!!,
                                        AP_SDK_KeystoreManager.ALIAS.USERNAME
                                    )
                                    var password = AP_SDK_KeystoreManager.decryptText(
                                        passwordEncryptValue!!,
                                        passwordIvEncryptValue!!,
                                        AP_SDK_KeystoreManager.ALIAS.PWD
                                    )

                                    AP_SDK_GlobalMethods().autoLoginAction(this@AP_SDK_PinEnterActivity, username!!, password!!, AP_SDK_ConstantsStrings().isValidatePinActivity)
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                    var username = KeyStoreHelper.decrypt(
                                        AP_SDK_KeystoreManager.ALIAS.USERNAME,
                                        usernameEncryptValue
                                    )
                                    var password = KeyStoreHelper.decrypt(
                                        AP_SDK_KeystoreManager.ALIAS.PWD,
                                        passwordEncryptValue
                                    )
                                    AP_SDK_GlobalMethods().autoLoginAction(
                                        this@AP_SDK_PinEnterActivity,
                                        username!!,
                                        password!!,
                                        AP_SDK_ConstantsStrings().isValidatePinActivity
                                    )
                                }
                            }
                        } else {
                            keyPadLockedFlag = true
                            LockKeyPadOperation().execute("")
                            Toast.makeText(this, "Please enter a valid Pin", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            } else {
                //Roll over
                pinBoxArray!![0]!!.text = ""
                pinBoxArray!![1]!!.text = ""
                pinBoxArray!![2]!!.text = ""
                pinBoxArray!![3]!!.text = ""

                userEntered = ""

                userEntered = userEntered + pressedButton.text

                pinBoxArray!![userEntered!!.length - 1]!!.text = "8"

            }
        }

        setListeners(pinButtonHandler)


        pinBox0 = findViewById<View>(R.id.pinBox0) as TextView
        pinBox1 = findViewById<View>(R.id.pinBox1) as TextView
        pinBox2 = findViewById<View>(R.id.pinBox2) as TextView
        pinBox3 = findViewById<View>(R.id.pinBox3) as TextView
        pinConfirmationLayout = findViewById<View>(R.id.pinConfirmationLayout) as RelativeLayout


        pinBoxArray = arrayOfNulls<TextView>(PIN_LENGTH)!!
        pinBoxArray!![0] = pinBox0
        pinBoxArray!![1] = pinBox1
        pinBoxArray!![2] = pinBox2
        pinBoxArray!![3] = pinBox3

        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener(View.OnClickListener {
            if(activityName == 3){
                launchActivity(AP_SDK_SignInScreenActivity::class.java)
                finishAffinity()
            }
            else {
                finish()
            }
        })

        buttonDelete = findViewById<View>(R.id.buttonDeleteBack) as AP_SDK_CustomTextView
        buttonDelete!!.setOnClickListener(View.OnClickListener {
            if (keyPadLockedFlag == true) {
                return@OnClickListener
            }

            if (userEntered!!.length > 0) {
                userEntered = userEntered!!.substring(0, userEntered!!.length - 1)
                pinBoxArray?.get(userEntered!!.length)!!.text = ""
            }

            var length = userEntered!!.length

            if(length == 0){
                view1!!.visibility = View.VISIBLE
            }
            else if(length == 1){
                view2!!.visibility = View.VISIBLE
            }
            else if(length == 2){
                view3!!.visibility = View.VISIBLE
            }
            else if(length == 3){
                view4!!.visibility = View.VISIBLE
            }
        }
        )
    }

    // navigate to screen and storing values in shared preference
    private fun moveToHomeActivity() {
        AP_SDK_PrefKeeper.pinValue = userPin
        AP_SDK_PrefKeeper.isPinEnabled = true
        AP_SDK_PrefKeeper.isLoggedIn = false
        launchActivity(AP_SDK_HomeActivity::class.java)
    }

    private fun setListeners(pinButtonHandler: View.OnClickListener) {

        button0 = findViewById<View>(R.id.button0) as AP_SDK_CustomTextView
        button0!!.setOnClickListener(pinButtonHandler)

        button1 = findViewById<View>(R.id.button1) as AP_SDK_CustomTextView
        button1!!.setOnClickListener(pinButtonHandler)

        button2 = findViewById<View>(R.id.button2) as AP_SDK_CustomTextView
        button2!!.setOnClickListener(pinButtonHandler)


        button3 = findViewById<View>(R.id.button3) as AP_SDK_CustomTextView
        button3!!.setOnClickListener(pinButtonHandler)

        button4 = findViewById<View>(R.id.button4) as AP_SDK_CustomTextView
        button4!!.setOnClickListener(pinButtonHandler)

        button5 = findViewById<View>(R.id.button5) as AP_SDK_CustomTextView
        button5!!.setOnClickListener(pinButtonHandler)

        button6 = findViewById<View>(R.id.button6) as AP_SDK_CustomTextView
        button6!!.setOnClickListener(pinButtonHandler)

        button7 = findViewById<View>(R.id.button7) as AP_SDK_CustomTextView
        button7!!.setOnClickListener(pinButtonHandler)

        button8 = findViewById<View>(R.id.button8) as AP_SDK_CustomTextView
        button8!!.setOnClickListener(pinButtonHandler)

        button9 = findViewById<View>(R.id.button9) as AP_SDK_CustomTextView
        button9!!.setOnClickListener(pinButtonHandler)
    }

    // Login success callback method
    fun onCognitoSuccess(){
        var awsConnectionManager = AP_SDK_AWSConnectionManager(this)
        awsConnectionManager.hitServer(AP_SDK_DefineID().FETCH_MERCHANT_PROFILE,this,null)
    }

    // Login failure callback method
    fun onCognitoFailure(){
        AP_SDK_GlobalMethods().dismissLoader()
        Toast.makeText(this,"Pin-Login was not successfull", Toast.LENGTH_LONG).show()
        launchActivity(AP_SDK_SignInScreenActivity::class.java)
    }

    // locking the keypad after PIN length is matched
    private inner class LockKeyPadOperation : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {
            for (i in 0..1) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }

            return "Executed"
        }

        override fun onPostExecute(result: String) {

            if(!(userEntered.equals(userPin))){
                view1!!.visibility = View.VISIBLE
                view2!!.visibility = View.VISIBLE
                view3!!.visibility = View.VISIBLE
                view4!!.visibility = View.VISIBLE

                pinBoxArray!!.get(0)!!.text = ""
                pinBoxArray!![1]!!.text = ""
                pinBoxArray!![2]!!.text = ""
                pinBoxArray!![3]!!.text = ""

                userEntered = ""

                keyPadLockedFlag = false
            }
        }

        override fun onPreExecute() {}

        override fun onProgressUpdate(vararg values: Void) {}
    }


}