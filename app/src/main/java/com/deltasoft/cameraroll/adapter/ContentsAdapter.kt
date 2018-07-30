package com.deltasoft.cameraroll.adapter

import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.deltasoft.cameraroll.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.async

class ContentsAdapter(val activity: AppCompatActivity, contentsItems: ArrayList<ContentsItem>) : RecyclerView.Adapter<ContentsAdapter.ViewHolder>() {

    //TODO store video playback position and resume from the same time position on video gets visible once again
    //TODO handle start/stop events of MainActivity in order to pause/resume video playback
    //TODO RecyclerView scrolling is still not perfectly smooth. Should be investigated more, may be the MediaPlayer must be replaced with custom player

    var items: List<ContentsItem> = contentsItems
        set(newValue){
            items = ArrayList<ContentsItem>(newValue)
            notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_contents_item_layout, parent, false)
        return ViewHolder(activity, view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.setDataSource(item)
    }

    class ViewHolder(val activity: AppCompatActivity, itemView: View?) : RecyclerView.ViewHolder(itemView), SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

        var isVideo: Boolean = false
            set(value) {
                field = value
                if (value) {
                    surfaceView?.visibility = View.VISIBLE
                    textView?.visibility = View.VISIBLE
                    imageView?.visibility = View.GONE
                } else {
                    surfaceView?.visibility = View.GONE
                    textView?.visibility = View.GONE
                    imageView?.visibility = View.VISIBLE
                }
            }
        var filePath: String? = null
            set(value) {
                if (null==value) {
                    field = ""
                } else {
                    field = String.format(value)
                }
                if (isVideo) {
                    if (surfaceCreated) {
                        releaseMediaPlayer()
                        if (filePath != field) createMediaPlayer(filePath!!, 0)
                    }
                } else {
                    Picasso.get().load(filePath).resizeDimen(R.dimen.video_frame_width, R.dimen.video_frame_height).error(R.mipmap.ic_launcher_round).into(imageView);
                }


            }
        var textView: TextView? = null
        var surfaceView: SurfaceView? = null
        var surfaceHolder: SurfaceHolder? = null
        var imageView: ImageView? = null
        var mediaPlayer: MediaPlayer? = null
        var surfaceCreated = false


        init {
            textView = itemView?.findViewById(R.id.item_text)
            surfaceView = itemView?.findViewById(R.id.item_surfaceview)
            surfaceHolder = surfaceView?.holder
            surfaceHolder?.addCallback(this)
            imageView = itemView?.findViewById(R.id.item_image)
        }

        override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(p0: SurfaceHolder?) {
            surfaceCreated = false
            textView?.visibility = View.VISIBLE
            textView?.bringToFront()
        }

        override fun surfaceCreated(p0: SurfaceHolder?) {
            Log.d("dstest", "surfaceCreated (1)")
            if (isVideo) {
                surfaceCreated = true
                if (null != filePath) {
                    async {
                        releaseMediaPlayer()
                        createMediaPlayer(filePath!!, 1)
                        activity.runOnUiThread(Runnable {
                            Log.d("dstest", "surfaceCreated (2)")
                            textView?.visibility = View.GONE
                        })
                    }
                }
            }
        }

        override fun onPrepared(p0: MediaPlayer?) {
            mediaPlayer?.start()
        }

        private fun createMediaPlayer(path: String, src: Int) {
            Log.d("dstest", "Create mediaplayer: $src")
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
            this.isVideo = item.isVideo
            this.filePath = item.filePath
        }
    }
}