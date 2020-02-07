package com.aeropay_merchant.Utilities

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object AP_SDK_PrefKeeper {

    private var prefs: SharedPreferences? = null

    var logInCount: Int
        get() = prefs!!.getInt(AP_SDK_ConstantsStrings().loginCount, 0)
        set(loginCount) = prefs!!.edit().putInt(AP_SDK_ConstantsStrings().loginCount, loginCount).apply()

    var minorId: Int
        get() = prefs!!.getInt(AP_SDK_ConstantsStrings().minorId, -1)
        set(minorId) = prefs!!.edit().putInt(AP_SDK_ConstantsStrings().minorId, minorId).apply()

    var majorId: Int
        get() = prefs!!.getInt(AP_SDK_ConstantsStrings().majorId, -1)
        set(majorId) = prefs!!.edit().putInt(AP_SDK_ConstantsStrings().majorId, majorId).apply()

    var deviceUuid: String?
        get() = prefs!!.getString(AP_SDK_ConstantsStrings().deviceUUID, AP_SDK_ConstantsStrings().noValue)
        set(deviceUuid) = prefs!!.edit().putString(AP_SDK_ConstantsStrings().deviceUUID, deviceUuid).apply()

    var merchantId: Int
        get() = prefs!!.getInt(AP_SDK_ConstantsStrings().merchantId, 0)
        set(merchantId) = prefs!!.edit().putInt(AP_SDK_ConstantsStrings().merchantId, merchantId).apply()

    var pinValue: String?
        get() = prefs!!.getString(AP_SDK_ConstantsStrings().pinValue,AP_SDK_ConstantsStrings().noValue)
        set(pinValue) = prefs!!.edit().putString(AP_SDK_ConstantsStrings().pinValue, pinValue).apply()

    var deviceToken: String?
        get() = prefs!!.getString(AP_SDK_ConstantsStrings().deviceToken,AP_SDK_ConstantsStrings().noValue)
        set(deviceToken) = prefs!!.edit().putString(AP_SDK_ConstantsStrings().deviceToken, deviceToken).apply()

    var isPinEnabled: Boolean
        get() = prefs!!.getBoolean(AP_SDK_ConstantsStrings().pinEnabled, false)
        set(pinEnabled) = prefs!!.edit().putBoolean(AP_SDK_ConstantsStrings().pinEnabled, pinEnabled).apply()

    var isLoggedIn: Boolean
        get() = prefs!!.getBoolean(AP_SDK_ConstantsStrings().isLoggedin, false)
        set(isLogin) = prefs!!.edit().putBoolean(AP_SDK_ConstantsStrings().isLoggedin, isLogin).apply()

    var storeName: String?
        get() = prefs?.getString(AP_SDK_ConstantsStrings().storeName, AP_SDK_ConstantsStrings().noValue)
        set(storeName) = prefs!!.edit().putString(AP_SDK_ConstantsStrings().storeName, storeName).apply()

    var deviceName: String?
        get() = prefs?.getString(AP_SDK_ConstantsStrings().deviceName, AP_SDK_ConstantsStrings().noValue)
        set(deviceName) = prefs!!.edit().putString(AP_SDK_ConstantsStrings().deviceName, deviceName).apply()

    var merchantDeviceId : Int?
        get() = prefs?.getInt(AP_SDK_ConstantsStrings().merchantDeviceId, 0)
        set(devicePosition) = prefs!!.edit().putInt(AP_SDK_ConstantsStrings().merchantDeviceId, devicePosition!!).apply()

    var merchantLocationId : Int?
        get() = prefs?.getInt(AP_SDK_ConstantsStrings().merchantLocationId, 0)
        set(devicePosition) = prefs!!.edit().putInt(AP_SDK_ConstantsStrings().merchantLocationId, devicePosition!!).apply()

    var merchantLocationDeviceId : Int?
        get() = prefs?.getInt(AP_SDK_ConstantsStrings().merchantLocationDeviceId, 0)
        set(devicePosition) = prefs!!.edit().putInt(AP_SDK_ConstantsStrings().merchantLocationDeviceId, devicePosition!!).apply()

    var username: String?
        get() = prefs?.getString(AP_SDK_ConstantsStrings().username, AP_SDK_ConstantsStrings().noValue)
        set(username) = prefs!!.edit().putString(AP_SDK_ConstantsStrings().username, username).apply()

    var password: String?
        get() = prefs?.getString(AP_SDK_ConstantsStrings().password, null)
        set(token) = prefs!!.edit().putString(AP_SDK_ConstantsStrings().password, token).apply()

    var usernameIV: String?
        get() = prefs?.getString(AP_SDK_ConstantsStrings().usernameIv, null)
        set(usernameIv) = prefs!!.edit().putString(AP_SDK_ConstantsStrings().usernameIv, usernameIv).apply()

    var passwordIV: String?
        get() = prefs?.getString(AP_SDK_ConstantsStrings().passwordIv, null)
        set(passwordIv) = prefs!!.edit().putString(AP_SDK_ConstantsStrings().passwordIv, passwordIv).apply()

    fun init(context: Context) {
        if (prefs == null)
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun clear() = prefs?.edit()?.clear()?.apply()

}