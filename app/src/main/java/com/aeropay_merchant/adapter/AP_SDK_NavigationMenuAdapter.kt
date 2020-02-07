package com.aeropay_merchant.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.aeropay_merchant.R

class AP_SDK_NavigationMenuAdapter(context: Context?, private val menuItemsNameArray: Array<String>, private val menuItemsImageArray: Array<Int>) : ArrayAdapter<String>(context!!, R.layout.ap_sdk_navigation_menu_items, menuItemsNameArray){
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return menuItemsNameArray.size
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val rowView = inflater.inflate(R.layout.ap_sdk_navigation_menu_items, null, true)

        var itemsNameText = rowView.findViewById(R.id.itemsName) as TextView
        var itemsImageView = rowView.findViewById(R.id.itemsImage) as ImageView

        itemsNameText.text = menuItemsNameArray[position]
        itemsImageView.setImageResource(menuItemsImageArray[position])

        return rowView
    }
}