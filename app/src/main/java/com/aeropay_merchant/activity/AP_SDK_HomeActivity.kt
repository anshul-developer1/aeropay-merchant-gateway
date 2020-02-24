package com.aeropay_merchant.activity


import AP.model.ProcessTransaction
import AP.model.RegisterMerchantDevice
import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aeropay_merchant.Model.AP_SDK_AeropayModelManager
import com.aeropay_merchant.adapter.HomeListRecyclerView
import com.aeropay_merchant.communication.AP_SDK_AWSConnectionManager
import com.aeropay_merchant.communication.AP_SDK_DefineID
import org.altbeacon.beacon.*
import android.os.Build
import org.altbeacon.beacon.BeaconParser
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.*
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.aeropay_merchant.Model.AP_SDK_CreateSyncPayload
import com.aeropay_merchant.Model.AP_SDK_FetchMerchantProfileModel
import com.aeropay_merchant.Utilities.*
import com.aeropay_merchant.ViewModel.AP_SDK_HomeViewModel
import com.aeropay_merchant.adapter.AP_SDK_HomeCardRecyclerView
import com.aeropay_merchant.view.AP_SDK_CustomTextView
import com.amazonaws.amplify.generated.graphql.OnCreateMerchantSyncSubscription
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.earthling.atminput.ATMEditText
import com.earthling.atminput.Currency
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import okhttp3.internal.format
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


lateinit var cardAdapterAPSDK: AP_SDK_HomeCardRecyclerView
lateinit var homeListAdapter : HomeListRecyclerView


class AP_SDK_HomeActivity : BaseActivity(){

    lateinit var menuButton : ImageView
    lateinit var listViewRecycler : RecyclerView
    lateinit var cardViewRecycler : RecyclerView
    lateinit var readyToPay : TextView
    lateinit var aeropayTransparent : ImageView
    lateinit var headerLayout : RelativeLayout
    lateinit var beaconTransmitter: BeaconTransmitter
    lateinit var bottomFragment: AuthorizeSheetDialog
    lateinit var mReceiver: BroadcastReceiver
    lateinit var subscriptionWatcher: AppSyncSubscriptionCall<OnCreateMerchantSyncSubscription.Data>
    lateinit var APSDKHomeViewModel : AP_SDK_HomeViewModel
    lateinit var txnID : String
    var bleAdapter: BluetoothAdapter? = null
    var isBleSupported = false
    var isBillSend = true
    var selectedPosition : Int? = -1
    var bottomSheetPosition : Int? = -1
    val TAG = AP_SDK_SignInScreenActivity::class.java!!.getSimpleName()
    var objModelManager = AP_SDK_AeropayModelManager().getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        APSDKHomeViewModel = AP_SDK_HomeViewModel()
        setContentView(com.aeropay_merchant.R.layout.ap_sdk_activity_home)
        objModelManager.APSDKSubscriptionPayloadForList.payloadList = mutableListOf()
        objModelManager.createSyncPayloadAPSDK.payloadList = mutableListOf()
        initialiseControls()
        saveMerchantId()
        AP_SDK_GlobalMethods().getDeviceToken(applicationContext)
        setListeners()
        maintainUserLoginCount()
        bottomFragment = AuthorizeSheetDialog()

        var asd = AP_SDK_PrefKeeper.merchantLocationDeviceId

        var loginCount = AP_SDK_PrefKeeper.logInCount
        if(loginCount< 4){
            var isPin = AP_SDK_PrefKeeper.isPinEnabled
            var isLogin = AP_SDK_PrefKeeper.isLoggedIn
            if(!isPin && !isLogin)
                AP_SDK_GlobalMethods().showDialog(this)
        }

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                var action = p1!!.action
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(p1.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF){
                        setUIWithBT()
                        AP_SDK_GlobalMethods().createSnackBar(headerLayout, "To accept payment from Consumers, Please enable your Bluetooth.")
                    }
                    else if(p1.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
                        setUIWithBT()
                    }
                }
            }
        }
        registerReceiver(mReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        bleAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bleAdapter == null){
            AP_SDK_GlobalMethods().createSnackBar(headerLayout,"Device not supported BLE")
        }
        else{
            setUIWithBT()
            startSubscription()
        }
    }

    private fun saveMerchantId() {
        var objModelManager = AP_SDK_AeropayModelManager().getInstance().merchantProfileModelAPSDK
        var merchantId = objModelManager.merchant.merchantId

        AP_SDK_PrefKeeper.merchantId = merchantId
    }

    //setting onClick Listeners on views
    private fun setListeners() {
        menuButton.setOnClickListener(View.OnClickListener {
            it.isClickable = false
            it.isEnabled = false
            launchActivity(AP_SDK_NavigationMenuActivity::class.java)
        })
    }

    override fun onResume() {
        super.onResume()
        menuButton.isClickable = true
        menuButton.isEnabled = true
        cardAdapterAPSDK.setValues(objModelManager.createSyncPayloadAPSDK.payloadList)
        homeListAdapter = HomeListRecyclerView(objModelManager.APSDKSubscriptionPayloadForList.payloadList,this)
        listViewRecycler.adapter = homeListAdapter
        setUIWithBT()
    }

    // Bluetooth Connection Handling and UI Changes
    fun setUIWithBT(){
            var result = BeaconTransmitter.checkTransmissionSupported(this)
            if(result == 0){
                var isBleEnabled = bleAdapter?.isEnabled
                if(isBleEnabled!!){
                    isBleSupported = true
                    if(AP_SDK_PrefKeeper.minorId == -1){
                        createHitForUUID()
                    }
                    else{
                        startSharedAdvertisingBeaconWithString(AP_SDK_PrefKeeper.deviceUuid!!,AP_SDK_PrefKeeper.majorId,AP_SDK_PrefKeeper.minorId, "AP Stores")
                    }
                }
                else{
                    if(AP_SDK_GlobalMethods().checkConnection(this)){
                        var cardListLength = objModelManager.createSyncPayloadAPSDK.payloadList.size
                        if(cardListLength == 0){
                            cardViewRecycler.visibility = View.GONE
                            aeropayTransparent.visibility = View.VISIBLE
                            AP_SDK_GlobalMethods().createSnackBar(headerLayout, "To accept payment from Consumers, Please enable your Bluetooth.")
                        }
                    }
                }
            }
            else if(result == 5){
                cardViewRecycler.visibility = View.GONE
                aeropayTransparent.visibility = View.VISIBLE
                AP_SDK_GlobalMethods().createSnackBar(headerLayout,"To accept payment from Consumers, Please enable your Bluetooth.")
            }
            else{
                AP_SDK_GlobalMethods().createSnackBar(headerLayout,"BLE is not supported in your device.")
            }
    }

    // Registering Merchant Device and Request for UUID of Device
    fun createHitForUUID(){
        if(AP_SDK_GlobalMethods().checkConnection(this)){
            var registerMerchant = RegisterMerchantDevice()
            var deviceIntValue = AP_SDK_PrefKeeper.merchantDeviceId
            var deviceIdValue = deviceIntValue!!.toBigDecimal()

            registerMerchant.deviceId =  deviceIdValue
            registerMerchant.token = AP_SDK_PrefKeeper.deviceToken

            var awsConnectionManager = AP_SDK_AWSConnectionManager(this)
            awsConnectionManager.hitServer(AP_SDK_DefineID().REGISTER_MERCHANT_LOCATION_DEVICE,this,registerMerchant)
        }
        else{
            showMsgToast("Please check your Internet Connection")
        }
    }

    //setting up hardcoded Recycler Adapter
    private fun setupView() {
        homeListAdapter = HomeListRecyclerView(objModelManager.APSDKSubscriptionPayloadForList.payloadList,this)
        listViewRecycler.adapter = homeListAdapter
    }

    //inflating UI controls
    private fun initialiseControls() {
        menuButton = findViewById(com.aeropay_merchant.R.id.back_button)
        listViewRecycler = findViewById(com.aeropay_merchant.R.id.recyclerListView)
        cardViewRecycler = findViewById(com.aeropay_merchant.R.id.cardRecyclerView)
        readyToPay = findViewById(com.aeropay_merchant.R.id.readyToPayText)
        aeropayTransparent = findViewById(com.aeropay_merchant.R.id.aeropayTranparentLogo)
        headerLayout = findViewById(com.aeropay_merchant.R.id.bodyLayout)

        listViewRecycler.layoutManager = LinearLayoutManager(this)

        var text = "<font color=#06dab3>"+ APSDKHomeViewModel.numberOfConsumers.toString() +"</font> <font color=#232323>ready to pay : </font>" +  "<font color=#06dab3>"+ AP_SDK_PrefKeeper.deviceName
                readyToPay.setText(Html.fromHtml(text))
        cardViewRecycler.visibility = View.GONE
        cardAdapterAPSDK = AP_SDK_HomeCardRecyclerView(objModelManager.createSyncPayloadAPSDK.payloadList, this@AP_SDK_HomeActivity)
        cardViewRecycler.adapter = cardAdapterAPSDK
    }

    // to check the login count of this user on this device
    private fun maintainUserLoginCount() {
        var initialLoginCount = AP_SDK_PrefKeeper.logInCount
        var finalCount = initialLoginCount + 1
        AP_SDK_PrefKeeper.logInCount = finalCount
    }


    // Save majorId, minorId and UUID in Preferences.
    fun creatBeaconTransmission(){
        var registerMerchantDevice = AP_SDK_AeropayModelManager().getInstance().APSDKRegisterMerchantDevices

        AP_SDK_PrefKeeper.deviceUuid = registerMerchantDevice.uuid as String
        AP_SDK_PrefKeeper.majorId = registerMerchantDevice.majorID.toInt()
        AP_SDK_PrefKeeper.minorId = registerMerchantDevice.minorID.toInt()

        startSharedAdvertisingBeaconWithString(registerMerchantDevice.uuid as String, registerMerchantDevice.majorID.toInt() , registerMerchantDevice.minorID.toInt(), "AP Stores")
    }

    // send BLE Connection Request
    fun startSharedAdvertisingBeaconWithString(uuid: String, major: Int, minor: Int, identifier: String) {
        val manufacturer = 0x4C
        val beacon = Beacon.Builder()
            .setId1(uuid)
            .setId2(major.toString())
            .setId3(minor.toString())
            .setManufacturer(manufacturer)
            .setBluetoothName(identifier)
            .setTxPower(-59)
            .build()
        val beaconParser = BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        beaconTransmitter = BeaconTransmitter(this, beaconParser)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            beaconTransmitter!!.startAdvertising(beacon, object : AdvertiseCallback() {
                override fun onStartFailure(errorCode: Int) {
                    AP_SDK_GlobalMethods().createSnackBar(headerLayout,"BLE connection error")
                }

                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    AP_SDK_GlobalMethods().createSnackBar(headerLayout,"BLE connection successful")
                }
            })
        }
    }

    // stop BLE Radiation when activity is destroyed.
    fun stopSharedAdvertisingBeacon() {
        if(isBleSupported){
            if (this.beaconTransmitter != null) {
                try {
                    this.beaconTransmitter!!.stopAdvertising()
                    this.subscriptionWatcher.cancel()
                } catch (ex: Exception) {
                }
            }
        }
    }

    // Listen to Merchant Device Location Subscription, whether there is any change
    private fun startSubscription() {
        val subscriptionCallback = object : AppSyncSubscriptionCall.Callback<OnCreateMerchantSyncSubscription.Data> {
            override fun onResponse(response: Response<OnCreateMerchantSyncSubscription.Data>) {
                runOnUiThread {
                    isBillSend = true
                    var payload = response.data()!!.onCreateMerchantSync()!!.payload().toString()
                    objModelManager.cardPayloadData = Gson().fromJson(payload.substring(1,payload.length - 1), AP_SDK_CreateSyncPayload::class.java)

                    var apStatus = objModelManager.cardPayloadData.status
                    txnID = objModelManager.cardPayloadData.transactionId
                    if(objModelManager.cardPayloadData.expirationTime.toInt() != 0){
                        objModelManager.cardPayloadData.expirationTime = AP_SDK_GlobalMethods().EpochToDate(objModelManager.cardPayloadData.expirationTime.toLong(),"dd-MM-YYYY HH:mm:ss")
                    }

                    if (payload.contains("tipAmount")) {
                        var listSize = objModelManager.APSDKSubscriptionPayloadForList.payloadList.size

                        for (i in 0..listSize - 1) {
                            if (txnID.equals(objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].transactionId)) {
                                objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].status = apStatus
                                objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].tip = objModelManager.cardPayloadData.tip
                            }
                        }
                        setupView()
                    }
                    else
                    {
                    if (apStatus.equals("initiated")) {
                        cardViewRecycler.visibility = View.VISIBLE
                        aeropayTransparent.visibility = View.GONE

                        cardViewRecycler.layoutManager = LinearLayoutManager(this@AP_SDK_HomeActivity, LinearLayoutManager.HORIZONTAL, false)

                        APSDKHomeViewModel.setValues(response)
                        APSDKHomeViewModel.setPayload(response)

                        txnID = objModelManager.cardPayloadData.transactionId

                        var createSyncPayload = objModelManager.cardPayloadData

                        objModelManager.createSyncPayloadAPSDK.payloadList.add(createSyncPayload)

                        cardAdapterAPSDK.setValues(objModelManager.createSyncPayloadAPSDK.payloadList)

                        cardAdapterAPSDK.onItemClick = { pos, view -> onItemClick(pos, view) }

                        APSDKHomeViewModel.numberOfConsumers = APSDKHomeViewModel.numberOfConsumers!! + 1

                        var text = "<font color=#06dab3>" + APSDKHomeViewModel.numberOfConsumers.toString() + "</font> <font color=#232323>ready to pay : </font>" +  "<font color=#06dab3>"+ AP_SDK_PrefKeeper.deviceName
                        readyToPay.setText(Html.fromHtml(text))

                    }
                    else if (apStatus.equals("processed"))
                    {
                        var listSize = objModelManager.APSDKSubscriptionPayloadForList.payloadList.size

                        for (i in 0..listSize - 1) {
                            if (txnID.equals(objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].transactionId)) {
                                homeListAdapter.stopTimer()
                                objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].status = apStatus
                            }
                        }
                        setupView()
                    }
                    else if (apStatus.equals("cancelled"))
                    {
                        var listSize = objModelManager.APSDKSubscriptionPayloadForList.payloadList.size

                        for (i in 0..listSize - 1) {
                            if (txnID.equals(objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].transactionId)) {
                                isBillSend = false
                                objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].status = apStatus
                                objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].expirationTime = "0"
                                homeListAdapter.stopTimer()
                            }
                        }

                        if ((isBillSend)) {
                            var listSizeCard = objModelManager.createSyncPayloadAPSDK.payloadList.size
                            var tag = supportFragmentManager.findFragmentByTag("SheetFragment")
                            if(!(tag==null)){
                                bottomFragment.dismiss()
                            }
                            for (i in 0..listSizeCard - 1) {
                                if (txnID.equals(objModelManager.createSyncPayloadAPSDK.payloadList[i].transactionId)) {
                                    isBillSend = false

                                    objModelManager.createSyncPayloadAPSDK.payloadList[i].status = apStatus
                                    objModelManager.createSyncPayloadAPSDK.payloadList[i].expirationTime = "0"
                                    objModelManager.APSDKSubscriptionPayloadForList.payloadList.reverse()
                                    objModelManager.APSDKSubscriptionPayloadForList.payloadList.add(objModelManager.createSyncPayloadAPSDK.payloadList[i])
                                    objModelManager.APSDKSubscriptionPayloadForList.payloadList.reverse()

                                    objModelManager.createSyncPayloadAPSDK.payloadList.removeAt(i)
                                    cardAdapterAPSDK.setValues(objModelManager.createSyncPayloadAPSDK.payloadList)

                                    APSDKHomeViewModel.numberOfConsumers = APSDKHomeViewModel.numberOfConsumers!! - 1
                                    var text = "<font color=#06dab3>" + APSDKHomeViewModel.numberOfConsumers.toString() + "</font> <font color=#232323>ready to pay : </font>" +  "<font color=#06dab3>"+ AP_SDK_PrefKeeper.deviceName
                                    readyToPay.setText(Html.fromHtml(text))
                                }
                            }
                        }
                        setupView()
                    }
                }
                }
            }

            override fun onFailure(e: ApolloException) {
                Log.e(TAG, "Subscription failure", e)
            }

            override fun onCompleted() {
                Log.d(TAG, "Subscription completed")
            }

        }
        val subscription = OnCreateMerchantSyncSubscription.builder().merchant_id(AP_SDK_PrefKeeper.merchantLocationDeviceId.toString()).build()
        subscriptionWatcher = AP_SDK_ClientFactory.getInstance(this.applicationContext).subscribe(subscription)
        subscriptionWatcher.execute(subscriptionCallback)
    }

    // On Card Adapter Click Event to open Authorize payment Screen
    fun onItemClick(position : Int,view: View) {
        APSDKHomeViewModel.userEntered = ""
        bottomFragment = AuthorizeSheetDialog()
        bottomFragment.isCancelable = false
        bottomSheetPosition = position
        bottomFragment.show(supportFragmentManager, "SheetFragment")
    }

    override fun onDestroy() {
        unregisterReceiver(mReceiver)
        stopSharedAdvertisingBeacon()
        Log.d("Yeahh" ,"Success")
        super.onDestroy()
    }

    // Transaction Progress Success and Updating UI
    fun sendProcessTransaction(isSuccess : Boolean) {
        var inProgressUserDetail = objModelManager.createSyncPayloadAPSDK.payloadList[selectedPosition!!]
        var expirationTime = inProgressUserDetail.expirationTime
        objModelManager.createSyncPayloadAPSDK.payloadList[selectedPosition!!].expirationTime = "0"
        objModelManager.createSyncPayloadAPSDK.payloadList.removeAt(!!)
        cardAdapterAPSDK.setValues(objModelManager.createSyncPayloadAPSDK.payloadList)

        APSDKHomeViewModel.numberOfConsumers = APSDKHomeViewModel.numberOfConsumers!! - 1
        var text = "<font color=#06dab3>"+ APSDKHomeViewModel.numberOfConsumers.toString() +"</font> <font color=#232323>ready to pay : </font>" +  "<font color=#06dab3>"+ AP_SDK_PrefKeeper.deviceName
        readyToPay.setText(Html.fromHtml(text))

        inProgressUserDetail.expirationTime = expirationTime

        var createSyncPayload = inProgressUserDetail

        if(isSuccess){
            createSyncPayload.status = "in-progress"
            createSyncPayload.amountAdded = APSDKHomeViewModel.userEntered
        }
        else{
            createSyncPayload.status = "cancelled"
            createSyncPayload.amountAdded = ""
            createSyncPayload.expirationTime = "0"
        }

        objModelManager.APSDKSubscriptionPayloadForList.payloadList.reverse()
        objModelManager.APSDKSubscriptionPayloadForList.payloadList.add(createSyncPayload)
        objModelManager.APSDKSubscriptionPayloadForList.payloadList.reverse()
        setupView()
        var tag = supportFragmentManager.findFragmentByTag("SheetFragment")
        if(!(tag==null)){
            bottomFragment.dismiss()
        }
    }

    // Change UI when Transaction has expired.
    fun updateExpireCard(position : Int) {
        var inProgressUserDetail = objModelManager.createSyncPayloadAPSDK.payloadList[position!!]

        objModelManager.createSyncPayloadAPSDK.payloadList.removeAt(position!!)
        cardAdapterAPSDK.setValues(objModelManager.createSyncPayloadAPSDK.payloadList)

        APSDKHomeViewModel.numberOfConsumers = APSDKHomeViewModel.numberOfConsumers!! - 1
        var text = "<font color=#06dab3>"+ APSDKHomeViewModel.numberOfConsumers.toString() +"</font> <font color=#232323>ready to pay : </font>" +  "<font color=#06dab3>"+ AP_SDK_PrefKeeper.deviceName
        readyToPay.setText(Html.fromHtml(text))
        inProgressUserDetail.amountAdded = ""

        var createSyncPayload = inProgressUserDetail

        createSyncPayload.status = "cancelled"

        objModelManager.APSDKSubscriptionPayloadForList.payloadList.reverse()
        objModelManager.APSDKSubscriptionPayloadForList.payloadList.add(createSyncPayload)
        objModelManager.APSDKSubscriptionPayloadForList.payloadList.reverse()
        setupView()
        var tag = supportFragmentManager.findFragmentByTag("SheetFragment")
        if(!(tag==null)){
            bottomFragment.dismiss()
        }
    }

}
