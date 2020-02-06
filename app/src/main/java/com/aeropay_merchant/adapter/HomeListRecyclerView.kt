package com.aeropay_merchant.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aeropay_merchant.Model.CreateSyncPayload
import com.aeropay_merchant.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.home_recycler_layout.view.*


class HomeListRecyclerView(val payerName: MutableList<CreateSyncPayload>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int {
        return payerName.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.home_recycler_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.payerName?.text = payerName[position].userName
        holder?.payedAmount?.text = payerName[position].expirationTime

        var transactionStatus = payerName[position].status
        var profileImageUrl = payerName[position].profileImage

        if(transactionStatus.equals("processed")){
            holder?.status?.setImageResource(R.drawable.completed)
        }
        else if(transactionStatus.equals("in-progress")){
            holder?.status?.setImageResource(R.drawable.incompleted)
        }
        else if(transactionStatus.equals("cancelled")){
            holder?.status?.setImageResource(R.drawable.bitmap)
        }
        Glide.with(context).load(profileImageUrl).into(holder?.payerImage)
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val payerName = view.payerName
    val payerImage = view.userProfileImage
    val payedAmount = view.moneyText
    val timeLeft = view.timeLeftText
    val status = view.statusImage
}