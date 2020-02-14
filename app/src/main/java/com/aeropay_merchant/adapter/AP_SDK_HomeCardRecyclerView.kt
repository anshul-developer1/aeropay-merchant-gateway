package com.aeropay_merchant.adapter

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aeropay_merchant.Model.AP_SDK_AeropayModelManager
import com.aeropay_merchant.Model.AP_SDK_CreateSyncPayload
import com.aeropay_merchant.R
import com.aeropay_merchant.activity.AP_SDK_HomeActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.ap_sdk_recycler_card_layout.view.*
import java.text.FieldPosition
import java.util.concurrent.TimeUnit

class AP_SDK_HomeCardRecyclerView(var payerName: MutableList<AP_SDK_CreateSyncPayload>, val context: Context) : RecyclerView.Adapter<AP_SDK_HomeCardRecyclerView.CardViewHolder>() {

    var onItemClick: ((pos: Int, view: View) -> Unit)? = null
    var countDownTimer: CountDownTimer? = null

    override fun getItemCount(): Int {
        return payerName.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(
            LayoutInflater.from(context).inflate(R.layout.ap_sdk_recycler_card_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder?.payerName?.text = payerName[position].userName.toString()
        Glide.with(context).load(payerName[position].profileImage.toString()).apply(RequestOptions.circleCropTransform()).into(holder?.userImage)
        countDownTimer =  object : CountDownTimer(payerName[position].expirationTime.toLong(),1000){
            override fun onFinish() {
                payerName[position].expirationTime = "0"
                (context as AP_SDK_HomeActivity).updateExpireCard(position)
            }

            override fun onTick(p0: Long) {
                holder?.countDownTimer?.text = ""+String.format("0"+"%d:%d s", TimeUnit.MILLISECONDS.toMinutes( p0), TimeUnit.MILLISECONDS.toSeconds(p0) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(p0)))
                payerName[position].expirationTime = p0.toString()
            }

        }.start()
    }

    fun setValues(payerDetails: MutableList<AP_SDK_CreateSyncPayload>){
        this.payerName = payerDetails
        notifyDataSetChanged()
        if(countDownTimer != null){
            countDownTimer!!.cancel()
        }
    }

    inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) , View.OnClickListener {
        override fun onClick(v: View?) {
            if (v != null) {
                onItemClick?.invoke(adapterPosition, v)
            }
        }

        init {
            view.setOnClickListener(this)
        }
        val payerName = view.userName
        val userImage = view.userImage
        val countDownTimer = view.time
    }
}