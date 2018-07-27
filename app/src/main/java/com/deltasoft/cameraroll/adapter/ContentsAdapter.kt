package com.deltasoft.cameraroll.adapter

import android.media.MediaPlayer
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
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

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView), SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

        var isVideo: Boolean = false
        var filePath: String? = null
            set(value) {
                if (null==value) {
                    field = ""
                } else {
                    field = String.format(value)
                }
                if (surfaceCreated) {
                    releaseMediaPlayer()
                    if (null != field) createMediaPlayer(field!!)
                }

            }
        var textView: TextView? = null
        var surfaceView: SurfaceView? = null
        var surfaceHolder: SurfaceHolder? = null
        var mediaPlayer: MediaPlayer? = null
        var surfaceCreated = false


        init {
            textView = itemView?.findViewById(R.id.item_text)
            surfaceView = itemView?.findViewById(R.id.item_surfaceview)
            surfaceHolder = surfaceView?.holder
            surfaceHolder?.addCallback(this)
        }

        override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder?) {
            surfaceCreated = false
        }

        override fun surfaceCreated(p0: SurfaceHolder?) {
            Log.d("dstest", "surfaceCreated")
            surfaceCreated = true
            if (null!=filePath) {
                releaseMediaPlayer()
                createMediaPlayer(filePath!!)
            }
        }

        override fun onPrepared(p0: MediaPlayer?) {
            mediaPlayer?.start()
        }

        private fun createMediaPlayer(path: String) {
            Log.d("dstest", "Create mediaplayer")
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDisplay(surfaceHolder)
            mediaPlayer?.setOnPreparedListener(this)
            mediaPlayer?.setDataSource(path)
            mediaPlayer?.prepare()
        }

        private fun releaseMediaPlayer(){
            try {
                mediaPlayer?.stop()
            } catch (e: Exception) {
            }
            try {
                mediaPlayer?.release()
            } catch (e: Exception) {
            }
            try {
                mediaPlayer = null
            } catch (e: Exception) {
            }
        }
    }
}