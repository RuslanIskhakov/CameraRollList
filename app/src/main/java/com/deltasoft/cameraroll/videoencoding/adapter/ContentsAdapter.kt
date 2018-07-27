package com.deltasoft.cameraroll.videoencoding.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.deltasoft.cameraroll.R

class ContentsAdapter(var contentsItems: ArrayList<ContentsItem>) : RecyclerView.Adapter<ContentsAdapter.ViewHolder>() {

    var items: List<ContentsItem> = contentsItems
        set(newValue){
            items = ArrayList<ContentsItem>(newValue)
            notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_contents_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.filePath = item.filePath
        holder.isVideo = item.isVideo
        holder.textView?.text = item.filePath
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var isVideo: Boolean = false
        var filePath: String? = null
        var textView: TextView? = null

        init {
            textView = itemView?.findViewById(R.id.item_text)
        }
    }
}