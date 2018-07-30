package com.deltasoft.cameraroll.adapter

import android.media.MediaPlayer
import android.support.v7.widget.RecyclerView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.deltasoft.cameraroll.R
import com.deltasoft.cameraroll.enums.ContentsType
import com.deltasoft.cameraroll.interfaces.OnPlusButtonClickListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.async

/**
 *  Adapter for media items to be displayed inside RecyclerView
 */
class ContentsAdapter(val activity: AppCompatActivity, contentsItems: ArrayList<ContentsItem>, val listener: OnPlusButtonClickListener) : RecyclerView.Adapter<ContentsAdapter.ViewHolder>() {

    //TODO store video playback position and resume from the same time position on video gets visible once again
    //TODO handle start/stop events of MainActivity in order to pause/resume video playback
    //TODO RecyclerView scrolling is still not perfectly smooth. Should be investigated more, may be the MediaPlayer must be replaced with custom player

    var items: List<ContentsItem> = contentsItems
        set(newValue){
            field = ArrayList<ContentsItem>(newValue)
            notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_contents_item_layout, parent, false)
        return ViewHolder(activity, view, listener)
    }

    override fun getItemCount(): Int {
        return items.size + 1// +1 item for New Item button
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < getItemCount()-1) {
            holder.setDataSource(items.get(position))
        } else {
            holder.setDataSource(ContentsItem.getPlusItem())
        }
    }

    class ViewHolder(val activity: AppCompatActivity, itemView: View?, val listener: OnPlusButtonClickListener) : RecyclerView.ViewHolder(itemView), SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, View.OnClickListener {

        var type: ContentsType = ContentsType.PLUS_ITEM
            set(value) {
                field = value
                when (value) {
                        ContentsType.IMAGE -> {
                            surfaceView?.visibility = View.GONE
                            textView?.visibility = View.GONE
                            imageView?.visibility = View.VISIBLE
                            imageButton?.visibility = View.GONE
                        }
                        ContentsType.VIDEO -> {
                            surfaceView?.visibility = View.VISIBLE
                            textView?.visibility = View.VISIBLE
                            imageView?.visibility = View.GONE
                            imageButton?.visibility = View.GONE
                        }
                        else -> {
                            surfaceView?.visibility = View.GONE
                            textView?.visibility = View.GONE
                            imageView?.visibility = View.GONE
                            imageButton?.visibility = View.VISIBLE
                        }
                }
            }
        var filePath: String? = null
            set(value) {
                if (null==value) {
                    field = ""
                } else {
                    field = String.format(value)
                }

                when (type) {
                    ContentsType.IMAGE -> {
                        Picasso.get().load(filePath).resizeDimen(R.dimen.video_frame_width, R.dimen.video_frame_height).error(R.mipmap.ic_launcher_round).into(imageView);
                    }
                    ContentsType.VIDEO -> {
                        if (surfaceCreated) {
                            releaseMediaPlayer()
                            if (filePath != field) createMediaPlayer(filePath!!)
                        }
                    }
                    else -> {

                    }
                }
            }
        var textView: TextView? = null
        var surfaceView: SurfaceView? = null
        var surfaceHolder: SurfaceHolder? = null
        var imageView: ImageView? = null
        var imageButton: ImageButton? = null
        var mediaPlayer: MediaPlayer? = null
        var surfaceCreated = false


        init {
            textView = itemView?.findViewById(R.id.item_text)
            surfaceView = itemView?.findViewById(R.id.item_surfaceview)
            surfaceHolder = surfaceView?.holder
            surfaceHolder?.addCallback(this)
            imageView = itemView?.findViewById(R.id.item_image)
            imageButton = itemView?.findViewById(R.id.item_plus_btn)
            imageButton?.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onPlusButtonClick()
        }

        override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder?) {
            surfaceCreated = false
            textView?.visibility = View.VISIBLE
            textView?.bringToFront()
        }

        override fun surfaceCreated(p0: SurfaceHolder?) {
            if (type == ContentsType.VIDEO) {
                surfaceCreated = true
                if (null != filePath) {
                    async {
                        releaseMediaPlayer()
                        createMediaPlayer(filePath!!)
                        activity.runOnUiThread(Runnable {
                            textView?.visibility = View.GONE
                        })
                    }
                }
            }
        }

        override fun onPrepared(p0: MediaPlayer?) {
            mediaPlayer?.start()
        }

        private fun createMediaPlayer(path: String) {
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

        public fun setDataSource(item: ContentsItem) {
            //the order of parameters value updates matters
            this.type = item.type
            this.filePath = item.filePath
        }
    }
}