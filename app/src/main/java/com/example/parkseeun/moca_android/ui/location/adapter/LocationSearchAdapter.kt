package com.example.parkseeun.moca_android.ui.location.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.parkseeun.moca_android.R
import com.example.parkseeun.moca_android.model.get.GetLocationListResponseData
import com.example.parkseeun.moca_android.ui.community.review_write.data.CafeListData

class LocationSearchAdapter(val context : Context, val dataList : ArrayList<GetLocationListResponseData>) : RecyclerView.Adapter<LocationSearchAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // 뷰 인플레이트
        val view : View = LayoutInflater.from(context).inflate(R.layout.rv_item_cafe_location, parent, false)

        return Holder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        // 뷰 바인딩
        holder.txt_search_location_name.text = dataList[position].place_name
        holder.txt_search_location_address.text = dataList[position].road_address_name

    }

    // View Holder
    inner class Holder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val txt_search_location_name : TextView = itemView.findViewById(R.id.txt_search_location_name) as TextView
        val txt_search_location_address : TextView = itemView.findViewById(R.id.txt_search_location_address) as TextView
    }
}