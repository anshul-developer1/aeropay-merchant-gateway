package com.aeropay_merchant.activity

import AP.model.MerchantLocationDevices
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import com.aeropay_merchant.Model.AP_SDK_AeropayModelManager
import com.aeropay_merchant.R
import com.aeropay_merchant.Utilities.AP_SDK_ConstantsStrings
import com.aeropay_merchant.Utilities.AP_SDK_GlobalMethods
import com.aeropay_merchant.Utilities.AP_SDK_PrefKeeper
import com.aeropay_merchant.adapter.AP_SDK_HomeCardRecyclerView
import com.aeropay_merchant.communication.AP_SDK_AWSConnectionManager
import com.aeropay_merchant.communication.AP_SDK_DefineID


class AP_SDK_SettingsScreenActivity : BaseActivity() {

    lateinit var save : ImageView
    lateinit var deviceNameSpinner : Spinner
    lateinit var storeLocationSpinner : Spinner
    var storeLocation : String? = null
    var deviceName : String? = null
    lateinit var backButton : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ap_sdk_activity_settings_screen)

        initialiseControls()
    }

    // inflating UI controls
    private fun initialiseControls() {
        save = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.back_button)
        storeLocationSpinner = findViewById(R.id.storeSpinner)
        deviceNameSpinner = findViewById(R.id.deviceSpinner)
        deviceNameSpinner.visibility = View.GONE

        createStoreSpinner(this)

        if(AP_SDK_PrefKeeper.storeName.equals(AP_SDK_ConstantsStrings().noValue)|| AP_SDK_PrefKeeper.deviceName.equals(
                AP_SDK_ConstantsStrings().noValue)){
            backButton.visibility = View.GONE
        }
        else{
            backButton.setOnClickListener(View.OnClickListener {
                finish()
            })
        }
    }

    // creating spinner to select store from dropdown list
    private fun createStoreSpinner(context: Context?) {

        var objModelManager = AP_SDK_AeropayModelManager().getInstance()
        var modelOutPut = objModelManager.merchantLocationsModelAPSDK

        var arraySize = modelOutPut.locations.size - 1
        var storeListName: MutableList<String> = ArrayList()

        storeListName.add(0,"Select store")
        var count = 1

        for(i in 0..arraySize){
            storeListName.add(count,modelOutPut.locations[i].name)
            count++
        }

        if (storeLocationSpinner != null) {
            val arrayAdapter = ArrayAdapter<String>(context!!,R.layout.ap_sdk_spinner_layout, storeListName!!)
            storeLocationSpinner?.adapter = arrayAdapter

            storeLocationSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    if(position != 0)
                    {
                        storeLocation = storeListName[position]
                        var arrayPosition = position - 1
                        (context as AP_SDK_SettingsScreenActivity).onStoreSelectedEvent(arrayPosition)
                        var objModelManager = AP_SDK_AeropayModelManager().getInstance()
                        var listSize = objModelManager.APSDKSubscriptionPayloadForList.payloadList.size
                        var cardSize = objModelManager.createSyncPayloadAPSDK.payloadList.size

                        if(!(objModelManager.createSyncPayloadAPSDK.payloadList == null)){
                            for (i in 0..cardSize - 1) {
                                objModelManager.createSyncPayloadAPSDK.payloadList[i].expirationTime = "0"
                            }
                            objModelManager.createSyncPayloadAPSDK.payloadList.clear()
                            cardAdapterAPSDK.setValues(objModelManager.createSyncPayloadAPSDK.payloadList)
                        }
                        if(!(objModelManager.APSDKSubscriptionPayloadForList.payloadList == null)){
                            for (i in 0..listSize - 1) {
                                objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].expirationTime = "0"
                            }
                            objModelManager.APSDKSubscriptionPayloadForList.payloadList.clear()
                            homeListAdapter.setValues(objModelManager.APSDKSubscriptionPayloadForList.payloadList)
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
        }
    }

    // creating spinner to select devices from dropdown list
    fun createDeviceSpinner() {
        deviceNameSpinner.visibility = View.VISIBLE

        var objModelManager = AP_SDK_AeropayModelManager().getInstance()
        var modelOutPut = objModelManager.merchantDevicesModelAPSDK

        if(modelOutPut.devices != null){

            var arraySize = modelOutPut.devices.size - 1
            var storeListName: MutableList<String> = ArrayList()
            var count = 0

            for(i in 0..arraySize){
                storeListName.add(count,modelOutPut.devices[i].name as String)
                count++
            }

            if (deviceNameSpinner != null) {
                val devicesAdapter = ArrayAdapter<String>(this,R.layout.ap_sdk_spinner_layout, storeListName!!)
                deviceNameSpinner?.adapter = devicesAdapter
                deviceNameSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        deviceName = storeListName[position]
                        AP_SDK_PrefKeeper.merchantDeviceId = modelOutPut.devices[position].merchantLocationDeviceId.intValueExact()
                        AP_SDK_PrefKeeper.merchantLocationId = modelOutPut.devices[position].merchantLocationId.intValueExact()
                        AP_SDK_PrefKeeper.merchantLocationDeviceId = modelOutPut.devices[position].merchantLocationDeviceId.intValueExact()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }
            }
        }
    }

    fun onSaveButtonClick(view: View) {
        if(storeLocation.isNullOrEmpty() || deviceName.isNullOrEmpty()){
            showMsgToast("Please select your store location and AP_SDK_Device name")
        }
        else{
            AP_SDK_PrefKeeper.storeName = storeLocation
            AP_SDK_PrefKeeper.deviceName = deviceName
            AP_SDK_PrefKeeper.minorId = -1
            launchActivity(AP_SDK_HomeActivity::class.java)
        }
    }

    // saving store name and device name on save button click
    fun onStoreSelectedEvent(position : Int){
        if(AP_SDK_GlobalMethods().checkConnection(this)){
            var objModelManager = AP_SDK_AeropayModelManager().getInstance()

            var merchantLocation = MerchantLocationDevices()
            var merchantLocationValue = objModelManager.merchantLocationsModelAPSDK.locations[position].merchantLocationId as Double

            merchantLocation.locationId =  merchantLocationValue.toBigDecimal()

            var awsConnectionManager = AP_SDK_AWSConnectionManager(this)
            awsConnectionManager.hitServer(AP_SDK_DefineID().FETCH_MERCHANT_DEVICES,this,merchantLocation)
        }
        else{
            showMsgToast("Please check your Internet Connection")
        }
    }
}
