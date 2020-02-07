package com.aeropay_merchant.activity


import AP.model.ProcessTransaction
import AP.model.RegisterMerchantDevice
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
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.aeropay_merchant.Model.AP_SDK_CreateSyncPayload
import com.aeropay_merchant.Utilities.*
import com.aeropay_merchant.ViewModel.AP_SDK_HomeViewModel
import com.aeropay_merchant.adapter.AP_SDK_HomeCardRecyclerView
import com.aeropay_merchant.view.AP_SDK_CustomTextView
import com.amazonaws.amplify.generated.graphql.OnCreateMerchantSyncSubscription
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.bumptech.glide.Glide
import com.earthling.atminput.ATMEditText
import com.earthling.atminput.Currency
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson


class AP_SDK_HomeActivity : BaseActivity(){

    lateinit var menuButton : ImageView
    lateinit var listViewRecycler : RecyclerView
    lateinit var cardViewRecycler : RecyclerView
    lateinit var readyToPay : TextView
    lateinit var aeropayTransparent : ImageView
    lateinit var headerLayout : RelativeLayout
    lateinit var beaconTransmitter: BeaconTransmitter
    lateinit var cardAdapterAPSDK: AP_SDK_HomeCardRecyclerView
    lateinit var bottomFragment: BottomSheetDialog
    lateinit var mReceiver: BroadcastReceiver
    lateinit var subscriptionWatcher: AppSyncSubscriptionCall<OnCreateMerchantSyncSubscription.Data>
    lateinit var APSDKHomeViewModel : AP_SDK_HomeViewModel
    lateinit var txnID : String
    var bleAdapter: BluetoothAdapter? = null
    var isBleSupported = false
    var isBillSend = true
    var selectedPosition : Int? = -1
    val TAG = AP_SDK_SignInScreenActivity::class.java!!.getSimpleName()
    var objModelManager = AP_SDK_AeropayModelManager().getInstance()
   /* var count : Int? = 0
    var listCount : Int? = 0*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        APSDKHomeViewModel = AP_SDK_HomeViewModel()
        setContentView(com.aeropay_merchant.R.layout.ap_sdk_activity_home)
        initialiseControls()

        objModelManager.APSDKSubscriptionPayloadForList.payloadList = mutableListOf()
        objModelManager.createSyncPayloadAPSDK.payloadList = mutableListOf()
        saveMerchantId()
        AP_SDK_GlobalMethods().getDeviceToken(applicationContext)
        setListeners()
        maintainUserLoginCount()

        var loginCount = AP_SDK_PrefKeeper.logInCount
        if(loginCount< 4){
            var isPin = AP_SDK_PrefKeeper.isPinEnabled
            var isLogin = AP_SDK_PrefKeeper.isLoggedIn
            if(!isPin && !isLogin)
                AP_SDK_GlobalMethods().showDialog(this)
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
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                var action = p1!!.action
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(p1.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF){
                        setUIWithBT()
                    }
                    else if(p1.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
                        setUIWithBT()
                    }
                }
            }
        }
        registerReceiver(mReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        setUIWithBT()
    }

    fun setUIWithBT(){
        bleAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bleAdapter == null){
            AP_SDK_GlobalMethods().createSnackBar(headerLayout,"AP_SDK_Device not supported")
        }
        else{
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
                    cardViewRecycler.visibility = View.GONE
                    aeropayTransparent.visibility = View.VISIBLE
                    AP_SDK_GlobalMethods().createSnackBar(headerLayout,"Bluetooth off. Cannot detect Consumers. Please enable your Bluetooth.")
                }
            }
            else if(result == 5){
                cardViewRecycler.visibility = View.GONE
                aeropayTransparent.visibility = View.VISIBLE
                AP_SDK_GlobalMethods().createSnackBar(headerLayout,"Bluetooth off. Cannot detect Consumers. Please enable your Bluetooth.")
            }
            else{
                AP_SDK_GlobalMethods().createSnackBar(headerLayout,"BLE is not supported in your device.")
            }
        }
    }

    fun createHitForUUID(){
        var registerMerchant = RegisterMerchantDevice()
        var deviceIntValue = AP_SDK_PrefKeeper.merchantDeviceId
        var deviceIdValue = deviceIntValue!!.toBigDecimal()

        registerMerchant.deviceId =  deviceIdValue
        registerMerchant.token = AP_SDK_PrefKeeper.deviceToken

        var awsConnectionManager = AP_SDK_AWSConnectionManager(this)
        awsConnectionManager.hitServer(AP_SDK_DefineID().REGISTER_MERCHANT_LOCATION_DEVICE,this,registerMerchant)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        cardAdapterAPSDK = AP_SDK_HomeCardRecyclerView(objModelManager.createSyncPayloadAPSDK.payloadList,this@AP_SDK_HomeActivity)
        cardViewRecycler.adapter = cardAdapterAPSDK
        listViewRecycler.adapter = HomeListRecyclerView(objModelManager.APSDKSubscriptionPayloadForList.payloadList,this)
    }

    //setting up hardcoded Recycler Adapter
    private fun setupView() {
        listViewRecycler.adapter = HomeListRecyclerView(objModelManager.APSDKSubscriptionPayloadForList.payloadList,this)
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



        var text = "<font color=#06dab3>"+ APSDKHomeViewModel.numberOfConsumers.toString() +"</font> <font color=#232323>ready to pay</font>"
        readyToPay.setText(Html.fromHtml(text))

        cardViewRecycler.visibility = View.GONE
    }

    // to check the login count of this user on this device
    private fun maintainUserLoginCount() {
        var initialLoginCount = AP_SDK_PrefKeeper.logInCount
        var finalCount = initialLoginCount + 1
        AP_SDK_PrefKeeper.logInCount = finalCount
    }

    fun creatBeaconTransmission(){
        var registerMerchantDevice = AP_SDK_AeropayModelManager().getInstance().APSDKRegisterMerchantDevices

        AP_SDK_PrefKeeper.deviceUuid = registerMerchantDevice.uuid as String
        AP_SDK_PrefKeeper.majorId = registerMerchantDevice.majorID.toInt()
        AP_SDK_PrefKeeper.minorId = registerMerchantDevice.minorID.toInt()

        startSharedAdvertisingBeaconWithString(registerMerchantDevice.uuid as String, registerMerchantDevice.majorID.toInt() , registerMerchantDevice.minorID.toInt(), "AP Stores")
    }

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
        startSubscription()
    }


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

    private fun startSubscription() {
        val subscriptionCallback = object : AppSyncSubscriptionCall.Callback<OnCreateMerchantSyncSubscription.Data> {
            override fun onResponse(response: Response<OnCreateMerchantSyncSubscription.Data>) {
                runOnUiThread {
                    isBillSend = true
                    var payload = response.data()!!.onCreateMerchantSync()!!.payload().toString()
                    var stringOutput = Gson().toJson(payload)

                    var apStatusIndex = stringOutput.indexOf("APStatus")
                    var transactionIdIndex = stringOutput.indexOf("transactionId")

                    var apStatus = stringOutput.substring(apStatusIndex+22,transactionIdIndex-14)

                    if(apStatus.equals("initiated")){
                        cardViewRecycler.visibility = View.VISIBLE
                        aeropayTransparent.visibility = View.GONE

                        cardViewRecycler.layoutManager = LinearLayoutManager(this@AP_SDK_HomeActivity,LinearLayoutManager.HORIZONTAL, false)

                        APSDKHomeViewModel.setValues(response)
                        APSDKHomeViewModel.setPayload(response)

                        var transactionIdIndex =stringOutput.indexOf("transactionId")
                        var profileImageIndex =stringOutput.indexOf("profileImage")

                        txnID = stringOutput.substring(transactionIdIndex+27,profileImageIndex-14)

                        var userNameIndex =stringOutput.indexOf("userName")
                        var expirationTimeIndex =stringOutput.indexOf("expirationTime")
                        var apStatusIndex = stringOutput.indexOf("APStatus")

                        var userName = stringOutput.substring(userNameIndex+23,apStatusIndex-14)
                        var profileImageUrl = stringOutput.substring(profileImageIndex+27,expirationTimeIndex-14)

                        var createSyncPayload =
                            AP_SDK_CreateSyncPayload()

                        createSyncPayload.userName = userName
                        createSyncPayload.status = apStatus
                        createSyncPayload.profileImage = profileImageUrl
                        createSyncPayload.transactionId = txnID
                        createSyncPayload.expirationTime = ""

                        objModelManager.createSyncPayloadAPSDK.payloadList.add(createSyncPayload)

                        cardAdapterAPSDK = AP_SDK_HomeCardRecyclerView(objModelManager.createSyncPayloadAPSDK.payloadList,this@AP_SDK_HomeActivity)
                        cardViewRecycler.adapter = cardAdapterAPSDK

                        cardAdapterAPSDK.onItemClick = { pos, view ->
                            onItemClick(pos,view)
                        }

                        APSDKHomeViewModel.numberOfConsumers = APSDKHomeViewModel.numberOfConsumers!! + 1

                        var text = "<font color=#06dab3>"+ APSDKHomeViewModel.numberOfConsumers.toString() +"</font> <font color=#232323>ready to pay</font>"
                        readyToPay.setText(Html.fromHtml(text))
                    }
                    else if(apStatus.equals("processed")){
                        var listSize = objModelManager.APSDKSubscriptionPayloadForList.payloadList.size
                        var profileImageIndex =stringOutput.indexOf("profileImage")
                        txnID = stringOutput.substring(transactionIdIndex+27,profileImageIndex-14)
                        for(i in 0..listSize - 1){
                            if(txnID.equals(objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].transactionId)){
                                objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].status = apStatus
                            }
                        }
                        setupView()
                    }
                    else if(apStatus.equals("cancelled")){

                        var listSize = objModelManager.APSDKSubscriptionPayloadForList.payloadList.size
                        var profileImageIndex =stringOutput.indexOf("profileImage")
                        txnID = stringOutput.substring(transactionIdIndex+27,profileImageIndex-14)

                        for(i in 0..listSize - 1){
                            if(txnID.equals(objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].transactionId)){
                                isBillSend = false
                                objModelManager.APSDKSubscriptionPayloadForList.payloadList[i].status = apStatus
                            }
                        }

                        if((isBillSend)){
                            var listSizeCard = objModelManager.createSyncPayloadAPSDK.payloadList.size

                            for(i in 0..listSizeCard - 1){
                                if(txnID.equals(objModelManager.createSyncPayloadAPSDK.payloadList[i].transactionId)){
                                    isBillSend = false
                                    objModelManager.createSyncPayloadAPSDK.payloadList[i].status = apStatus
                                    objModelManager.APSDKSubscriptionPayloadForList.payloadList.add(objModelManager.createSyncPayloadAPSDK.payloadList[i])

                                    objModelManager.createSyncPayloadAPSDK.payloadList.removeAt(i)
                                    cardAdapterAPSDK.setValues(objModelManager.createSyncPayloadAPSDK.payloadList)

                                    APSDKHomeViewModel.numberOfConsumers = APSDKHomeViewModel.numberOfConsumers!! - 1
                                    var text = "<font color=#06dab3>"+ APSDKHomeViewModel.numberOfConsumers.toString() +"</font> <font color=#232323>ready to pay</font>"
                                    readyToPay.setText(Html.fromHtml(text))
                                }
                            }
                        }
                        setupView()
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

     fun onItemClick(position : Int,view: View) {
         APSDKHomeViewModel.userEntered = ""
         var view = (this as FragmentActivity).layoutInflater.inflate(com.aeropay_merchant.R.layout.ap_sdk_authorize_payment, null)
         bottomFragment = BottomSheetDialog(this)
         setValuesInDialog(view,position)
         bottomFragment.setContentView(view)
         bottomFragment.show()
    }

    private fun setValuesInDialog(view: View, position: Int) {
        var etInput = view.findViewById(com.aeropay_merchant.R.id.amountEdit) as ATMEditText
        var userImage = view.findViewById(com.aeropay_merchant.R.id.userImage) as ImageView
        var userName = view.findViewById(com.aeropay_merchant.R.id.userName) as AP_SDK_CustomTextView

        userName.setText(objModelManager.createSyncPayloadAPSDK.payloadList[position].userName)
        Glide.with(this).load(objModelManager.createSyncPayloadAPSDK.payloadList[position].profileImage).into(userImage)

        etInput.Currency   = Currency.USA
        etInput.setText("0")

        val pinButtonHandler = View.OnClickListener { v ->
            val pressedButton = v as AP_SDK_CustomTextView
            APSDKHomeViewModel.userEntered = APSDKHomeViewModel.userEntered + pressedButton.text
            etInput.setText(APSDKHomeViewModel.userEntered)
            etInput.setTextColor(Color.BLACK)
            etInput.setTypeface(Typeface.DEFAULT_BOLD)
        }

        var button0 = view.findViewById<View>(com.aeropay_merchant.R.id.button0) as AP_SDK_CustomTextView
        button0!!.setOnClickListener(pinButtonHandler)

        var button1 = view.findViewById<View>(com.aeropay_merchant.R.id.button1) as AP_SDK_CustomTextView
        button1!!.setOnClickListener(pinButtonHandler)

        var button2 = view.findViewById<View>(com.aeropay_merchant.R.id.button2) as AP_SDK_CustomTextView
        button2!!.setOnClickListener(pinButtonHandler)

        var button3 = view.findViewById<View>(com.aeropay_merchant.R.id.button3) as AP_SDK_CustomTextView
        button3!!.setOnClickListener(pinButtonHandler)

        var button4 = view.findViewById<View>(com.aeropay_merchant.R.id.button4) as AP_SDK_CustomTextView
        button4!!.setOnClickListener(pinButtonHandler)

        var button5 = view.findViewById<View>(com.aeropay_merchant.R.id.button5) as AP_SDK_CustomTextView
        button5!!.setOnClickListener(pinButtonHandler)

        var button6 = view.findViewById<View>(com.aeropay_merchant.R.id.button6) as AP_SDK_CustomTextView
        button6!!.setOnClickListener(pinButtonHandler)

        var button7 = view.findViewById<View>(com.aeropay_merchant.R.id.button7) as AP_SDK_CustomTextView
        button7!!.setOnClickListener(pinButtonHandler)

        var button8 = view.findViewById<View>(com.aeropay_merchant.R.id.button8) as AP_SDK_CustomTextView
        button8!!.setOnClickListener(pinButtonHandler)

        var button9 = view.findViewById<View>(com.aeropay_merchant.R.id.button9) as AP_SDK_CustomTextView
        button9!!.setOnClickListener(pinButtonHandler)

        var dropArrow = view.findViewById<View>(com.aeropay_merchant.R.id.downArrow) as ImageView
        dropArrow!!.setOnClickListener({
            bottomFragment.cancel()
        })

        var buttonDelete = view.findViewById<View>(com.aeropay_merchant.R.id.buttonDeleteBack) as AP_SDK_CustomTextView
        buttonDelete!!.setOnClickListener(View.OnClickListener {
            var userEnteredLength = APSDKHomeViewModel.userEntered!!.length
            if(userEnteredLength == 1){
                APSDKHomeViewModel.userEntered = APSDKHomeViewModel.userEntered!!.substring(0, APSDKHomeViewModel.userEntered!!.length - 1)
                etInput.setText("0")
                etInput.setTypeface(Typeface.DEFAULT_BOLD)
                etInput.setTextColor(Color.LTGRAY)
            }
            else if (userEnteredLength > 1){
                APSDKHomeViewModel.userEntered = APSDKHomeViewModel.userEntered!!.substring(0, APSDKHomeViewModel.userEntered!!.length - 1)
                etInput.setText(APSDKHomeViewModel.userEntered)
            }
        }
        )

        var authorizeButton = view.findViewById<View>(com.aeropay_merchant.R.id.authoriseButton) as Button
        authorizeButton.setOnClickListener {
            var processTransaction = ProcessTransaction()

            processTransaction.type = "debit"
            processTransaction.fromMerchant = "1".toBigDecimal()
            processTransaction.merchantLocationId = AP_SDK_PrefKeeper.merchantLocationId!!.toBigDecimal()

            var amount = etInput.text.toString()
            processTransaction.transactionDescription = "Aeropay Transaction"
            processTransaction.amount = amount.replace("$","").toBigDecimal()
            processTransaction.debug = "0".toBigDecimal()
            processTransaction.transactionId = txnID

            APSDKHomeViewModel.userEntered = amount

            selectedPosition = position

            var awsConnectionManager = AP_SDK_AWSConnectionManager(this)
            awsConnectionManager.hitServer(AP_SDK_DefineID().FETCH_MERCHANT_PROCESS_TRANSACTION,this,processTransaction)
        }
    }

    override fun onStop() {
        unregisterReceiver(mReceiver)
        stopSharedAdvertisingBeacon()
        super.onStop()
    }

    fun sendProcessTransaction() {
        var inProgressUserDetail = objModelManager.createSyncPayloadAPSDK.payloadList[selectedPosition!!]

        objModelManager.createSyncPayloadAPSDK.payloadList.removeAt(selectedPosition!!)
        cardAdapterAPSDK.setValues(objModelManager.createSyncPayloadAPSDK.payloadList)

        APSDKHomeViewModel.numberOfConsumers = APSDKHomeViewModel.numberOfConsumers!! - 1
        var text = "<font color=#06dab3>"+ APSDKHomeViewModel.numberOfConsumers.toString() +"</font> <font color=#232323>ready to pay</font>"
        readyToPay.setText(Html.fromHtml(text))

        var createSyncPayload = AP_SDK_CreateSyncPayload()

        createSyncPayload.userName = inProgressUserDetail.userName
        createSyncPayload.status = "in-progress"
        createSyncPayload.profileImage = inProgressUserDetail.profileImage
        createSyncPayload.transactionId = inProgressUserDetail.transactionId
        createSyncPayload.expirationTime = APSDKHomeViewModel.userEntered

        objModelManager.APSDKSubscriptionPayloadForList.payloadList.add(createSyncPayload)
        setupView()
        bottomFragment.cancel()
    }

}
