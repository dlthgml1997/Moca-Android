package com.example.parkseeun.moca_android.ui.plus

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.parkseeun.moca_android.R
import de.hdodenhof.circleimageview.CircleImageView

class PlusRecyclerViewAdapter(val context : Context, val dataList : ArrayList<PlusData>) : RecyclerView.Adapter<PlusRecyclerViewAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // 뷰 인플레이트
        val view : View = LayoutInflater.from(context).inflate(R.layout.rv_item_plus, parent, false)

        return Holder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        // 뷰 바인딩
        Glide.with(context).load(dataList[position].contentsImageUrl).into(holder.iv_plus_contentsImage)
        Glide.with(context).load(dataList[position].profileImageUrl).into(holder.civ_plus_profile)
        holder.tv_plus_title.text = dataList[position].title
        holder.tv_plus_editorName.text = dataList[position].editorName
    }

    // View Holder
    inner class Holder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val iv_plus_contentsImage : ImageView = itemView.findViewById(R.id.iv_plus_contentsImage) as ImageView
        val civ_plus_profile : CircleImageView = itemView.findViewById(R.id.civ_plus_profile) as CircleImageView
        val tv_plus_title : TextView = itemView.findViewById(R.id.tv_plus_title) as TextView
        val tv_plus_editorName : TextView = itemView.findViewById(R.id.tv_plus_editorName) as TextView
    }
}