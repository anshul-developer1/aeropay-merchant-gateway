package com.aeropay_merchant.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.aeropay_merchant.R
import com.aeropay_merchant.Utilities.AP_SDK_ConstantsStrings
import com.aeropay_merchant.Utilities.AP_SDK_PrefKeeper

class AP_SDK_FastLoginActivity : BaseActivity() {


    lateinit var save : ImageView
    lateinit var autoLoginToggle : ToggleButton
    lateinit var pinLoginToggle : ToggleButton
    lateinit var updateTextView: TextView
    lateinit var backButton : ImageView

    var isLogin : Boolean = false
    var isPin : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ap_sdk_activity_fast_login)

        initialiseControls()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        setInitialToggleStage()
    }

    // setting listeners on UI for View click
    private fun setListeners() {
        autoLoginToggle.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                    if(p1) {
                        isLogin = true
                        isPin = false
                        pinLoginToggle.isChecked = false
                    }
                    else {
                        isLogin = false
                    }
            }
        })

        pinLoginToggle.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                if(p1) {
                    moveToSetPin()
                }
                else {
                    isPin = false
                }
            }
        })

        updateTextView.setOnClickListener(View.OnClickListener {
            moveToSetPin()
        })
        backButton.setOnClickListener(View.OnClickListener {
            finish()
        })
    }

    // inflating UI controls
    private fun initialiseControls() {
        save = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.back_button)
        autoLoginToggle = findViewById(R.id.autoLoginToggle)
        pinLoginToggle = findViewById(R.id.pinLoginToggle)
        updateTextView = findViewById(R.id.updateText)

        setInitialToggleStage()
    }

    // setting toggle button initial state
    private fun setInitialToggleStage() {
            var isLoginEnabled = AP_SDK_PrefKeeper.isLoggedIn
            if(isLoginEnabled){
                autoLoginToggle.isChecked = true
                isLogin = true
            }
            else{
                autoLoginToggle.isChecked = false
                isLogin = false
            }

            var isPinEnabled = AP_SDK_PrefKeeper.isPinEnabled
            if(isPinEnabled){
                pinLoginToggle.isChecked = true
                isPin = true
            }
            else{
                pinLoginToggle.isChecked = false
                isPin = false
            }
    }

    // save button click event
    fun onSaveButtonClick(view: View) {
        AP_SDK_PrefKeeper.isLoggedIn = isLogin
        AP_SDK_PrefKeeper.isPinEnabled = isPin
        launchActivity(AP_SDK_NavigationMenuActivity::class.java)
        finish()
    }

    // move to set Pin Page
    fun moveToSetPin(){
        var intent = Intent(this@AP_SDK_FastLoginActivity,AP_SDK_PinEnterActivity::class.java)
        intent.putExtra(AP_SDK_ConstantsStrings().isPinActivityName,1)
        launchActivity(AP_SDK_PinEnterActivity::class.java,intent)
    }

}
