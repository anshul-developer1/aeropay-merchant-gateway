package com.aeropay_merchant.activity


import AP.model.ProcessTransaction
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import com.aeropay_merchant.R
import com.aeropay_merchant.Utilities.AP_SDK_GlobalMethods
import com.aeropay_merchant.Utilities.AP_SDK_PrefKeeper
import com.aeropay_merchant.ViewModel.AP_SDK_HomeViewModel
import com.aeropay_merchant.communication.AP_SDK_AWSConnectionManager
import com.aeropay_merchant.communication.AP_SDK_DefineID
import com.aeropay_merchant.view.AP_SDK_CustomTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.earthling.atminput.ATMEditText
import com.earthling.atminput.Currency
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A simple [Fragment] subclass.
 */
open class AuthorizeSheetDialog : BottomSheetDialogFragment() {

lateinit var mContext: AP_SDK_HomeActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            // Inflate the layout for this fragment
            val v = inflater.inflate(R.layout.ap_sdk_authorize_payment, container, false)
            mContext = (context as AP_SDK_HomeActivity)
            setValuesInDialog(v,mContext.bottomSheetPosition!!)
            return v
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialogBox = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialogBox.setOnShowListener { dialog ->
            setupFullHeight(dialogBox)
        }
        return dialogBox
    }

    fun setupFullHeight(bottomSheetDialog : BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        var layoutParams = bottomSheet.getLayoutParams()

        var sheetHeight = bottomSheet.height
        if (layoutParams != null) {
            layoutParams.height = sheetHeight
        }
        bottomSheet.setLayoutParams(layoutParams)
        bottomSheetBehavior.setSkipCollapsed(false)
        bottomSheetBehavior.setHideable(false)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
            }

        })
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
    }

    // Set Values in Dialog and Initialize UI of Dialog
    private fun setValuesInDialog(view: View, position: Int) {
        var etInput = view.findViewById(com.aeropay_merchant.R.id.amountEdit) as ATMEditText
        var userImage = view.findViewById(com.aeropay_merchant.R.id.userImage) as ImageView
        var userName = view.findViewById(com.aeropay_merchant.R.id.userName) as AP_SDK_CustomTextView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            etInput.focusable = View.NOT_FOCUSABLE
        }
        etInput.inputType = InputType.TYPE_NULL

        userName.setText(mContext.objModelManager.createSyncPayloadAPSDK.payloadList[position].userName)
        Glide.with(this).load(mContext.objModelManager.createSyncPayloadAPSDK.payloadList[position].profileImage).apply(
            RequestOptions.circleCropTransform()).into(userImage)

        etInput.Currency   = Currency.USA
        etInput.setText("0")

        val pinButtonHandler = View.OnClickListener { v ->
            val pressedButton = v as AP_SDK_CustomTextView
            mContext. APSDKHomeViewModel.userEntered = mContext.APSDKHomeViewModel.userEntered + pressedButton.text
            etInput.setText(mContext.APSDKHomeViewModel.userEntered)
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
            mContext.bottomFragment.dismiss()
        })

        var buttonDelete = view.findViewById<View>(com.aeropay_merchant.R.id.buttonDeleteBack) as AP_SDK_CustomTextView
        buttonDelete!!.setOnClickListener(View.OnClickListener {
            var userEnteredLength = mContext.APSDKHomeViewModel.userEntered!!.length
            if(userEnteredLength == 1){
                mContext.APSDKHomeViewModel.userEntered = mContext.APSDKHomeViewModel.userEntered!!.substring(0, mContext.APSDKHomeViewModel.userEntered!!.length - 1)
                etInput.setText("0")
                etInput.setTypeface(Typeface.DEFAULT_BOLD)
                etInput.setTextColor(Color.LTGRAY)
            }
            else if (userEnteredLength > 1){
                mContext.APSDKHomeViewModel.userEntered = mContext.APSDKHomeViewModel.userEntered!!.substring(0, mContext.APSDKHomeViewModel.userEntered!!.length - 1)
                etInput.setText(mContext.APSDKHomeViewModel.userEntered)
            }
        }
        )

        var authorizeButton = view.findViewById<View>(com.aeropay_merchant.R.id.authoriseButton) as Button
        authorizeButton.setOnClickListener {

            var amount = etInput.text.toString()
            if(amount.equals("$0.00")){
                mContext.showMsgToast("Please enter an amount to proceed.")
            }
            else{
                var amountValueLength = amount.replace("$","").trim().length
                if(amountValueLength > 6){
                    (context as AP_SDK_HomeActivity).showMsgToast("Amount should be lesser than or equal to $500.00.")
                }
                else {
                    if (amount.replace("$", "").trim().toDouble() > 500.00) {
                        (context as AP_SDK_HomeActivity).showMsgToast("Amount should be lesser than or equal to $500.00.")
                    } else {
                        var expirationTime = mContext.objModelManager.createSyncPayloadAPSDK.payloadList[position].expirationTime
                        if(!(expirationTime.equals("0"))){
                            if (AP_SDK_GlobalMethods().checkConnection(context as AP_SDK_HomeActivity)) {
                                var processTransaction = ProcessTransaction()

                                processTransaction.type = "debit"
                                processTransaction.fromMerchant = "1".toBigDecimal()
                                processTransaction.merchantLocationId = AP_SDK_PrefKeeper.merchantLocationId!!.toBigDecimal()

                                processTransaction.transactionDescription = "Aeropay Transaction"
                                processTransaction.amount = amount.replace("$", "").trim().toBigDecimal()
                                processTransaction.debug = "0".toBigDecimal()
                                processTransaction.transactionId = mContext.txnID

                                mContext.APSDKHomeViewModel.userEntered = amount

                                mContext.selectedPosition = position
                                var awsConnectionManager = AP_SDK_AWSConnectionManager(context as AP_SDK_HomeActivity)
                                awsConnectionManager.hitServer(AP_SDK_DefineID().FETCH_MERCHANT_PROCESS_TRANSACTION, context as AP_SDK_HomeActivity, processTransaction)
                            } else {
                                (context as AP_SDK_HomeActivity).showMsgToast("Please check your Internet Connection")
                            }
                        }
                        else{
                            mContext.showMsgToast("Transaction has expired.")
                        }
                    }
            }
            }
        }
    }

}