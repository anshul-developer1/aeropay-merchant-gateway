package com.aeropay_merchant.activity

import androidx.annotation.RequiresApi
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.os.Build
import android.os.Bundle
import android.util.Log
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconTransmitter


class AP_SDK_BeaconActivity : BaseActivity() {
    private val beaconManager: BeaconManager? = null
    private var beaconTransmitter: BeaconTransmitter? = null

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startSharedAdvertisingBeaconWithString(
            "8CAF8E6D-F16B-4382-B2DB-771AE570F405",
            582,
            29,
            "AP Stores"
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun startSharedAdvertisingBeaconWithString(
        uuid: String,
        major: Int,
        minor: Int,
        identifier: String
    ) {
        val manufacturer = 0x4C
        val beacon = Beacon.Builder()
            .setId1(uuid)
            .setId2(major.toString())
            .setId3(minor.toString())
            .setManufacturer(manufacturer)
            .setBluetoothName(identifier)
            .setTxPower(-59)
            .build()
        val beaconParser = BeaconParser()
            .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        beaconTransmitter = BeaconTransmitter(this, beaconParser)
        beaconTransmitter!!.startAdvertising(beacon, object : AdvertiseCallback() {

            override fun onStartFailure(errorCode: Int) {
                showMsgToast("onStartFailure")
                Log.d("ReactNative", "Error from start advertising $errorCode")
            }

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                showMsgToast("onStartSuccess")
                Log.d("ReactNative", "Success start advertising")
            }
        })
    }


    fun stopSharedAdvertisingBeacon() {
        if (this.beaconTransmitter != null) {
            try {
                this.beaconTransmitter!!.stopAdvertising()
            } catch (ex: Exception) {
            }

        }
    }

    /*public void checkTransmissionSupported(Callback cb) {
        int result = BeaconTransmitter.checkTransmissionSupported(context);
        cb.invoke(result);
    }*/


    override fun onDestroy() {
        super.onDestroy()
        stopSharedAdvertisingBeacon()
        // beaconManager.unbind(this);
    }

    companion object {

        val TAG = "BeaconsEverywhere"
    }
}

