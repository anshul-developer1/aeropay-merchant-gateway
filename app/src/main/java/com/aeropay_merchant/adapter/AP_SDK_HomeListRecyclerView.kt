package com.aeropay_merchant.adapter

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aeropay_merchant.Model.AP_SDK_CreateSyncPayload
import com.aeropay_merchant.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.synthetic.main.ap_sdk_home_recycler_layout.view.*
import java.util.concurrent.TimeUnit


class HomeListRecyclerView(val payerName: MutableList<AP_SDK_CreateSyncPayload>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    lateinit var countDownTimer: CountDownTimer

    override fun getItemCount(): Int {
        return payerName.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.ap_sdk_home_recycler_layout, parent, false))
    }

    fun stopTimer(){
        countDownTimer.cancel()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.payerName?.text = payerName[position].userName
        holder?.payedAmount?.text = payerName[position].amountAdded

        var transactionStatus = payerName[position].status
        var profileImageUrl = payerName[position].profileImage

        if(transactionStatus.equals("processed")){
            holder?.status?.setImageResource(R.drawable.completed)
            payerName[position].expirationTime = "0"
            holder?.timeLeft?.text = "00:00"
        }
        else if(transactionStatus.equals("in-progress")){
            holder?.status?.setImageResource(R.drawable.incompleted)
            countDownTimer =  object : CountDownTimer(payerName[position].expirationTime.toLong(),1000){
                override fun onFinish() {
                    holder?.timeLeft?.text = "00:00"
                    holder?.status?.setImageResource(R.drawable.bitmap)
                    payerName[position].expirationTime = "0"
                }

                override fun onTick(p0: Long) {
                    holder?.timeLeft?.text = ""+String.format("0"+"%d:%d s", TimeUnit.MILLISECONDS.toMinutes( p0), TimeUnit.MILLISECONDS.toSeconds(p0) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(p0)))
                    payerName[position].expirationTime = p0.toString()
                }

            }.start()
        }
        else if(transactionStatus.equals("cancelled")){
            holder?.status?.setImageResource(R.drawable.bitmap)
            payerName[position].expirationTime = "0"
            holder.timeLeft.text = "00:00"
        }
        Glide.with(context).load(profileImageUrl).apply(RequestOptions.circleCropTransform()).into(holder?.payerImage)

        if(!(payerName[position].tip == null)){
            holder?.tipAmount.visibility = View.VISIBLE
            holder?.tipText.visibility = View.VISIBLE

            var responseList = payerName[position].tip as LinkedTreeMap<String, String>
            holder?.tipAmount.text = "$" + responseList["tipAmount"]
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val payerName = view.payerName
    val payerImage = view.userProfileImage
    val payedAmount = view.moneyText
    val timeLeft = view.timeLeftText
    val status = view.statusImage
    val tipAmount = view.tipAmount
    val tipText = view.tipText
}