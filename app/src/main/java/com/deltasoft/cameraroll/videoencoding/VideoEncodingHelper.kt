package com.deltasoft.cameraroll.videoencoding

import android.util.Log
import kotlinx.coroutines.experimental.async
import android.R.attr.configure
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.support.v4.content.ContextCompat
import java.io.File


class VideoEncodingHelper {

    private val inputFilePath = "/sdcard/DCIM/Camera/20180723_165258.mp4"

    public fun start() {

        async{
            Log.d("dstest", "Coroutine!");
            configure()
        }

    }

    private fun configure() {
        try {
            val inputFile = File(inputFilePath)
            Log.d("dstest", "configure: "+ inputFile.exists())

            if (!inputFile.canRead()) {
                throw RuntimeException("Unable to read " + inputFile);
            }

            val extractor = MediaExtractor()
            extractor.setDataSource(inputFile.absolutePath)
            val numTracks = extractor.getTrackCount()
            Log.d("dstest", "Tracks: "+numTracks)
            for (i in 0..(numTracks-1)) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                Log.d("dstest", "Track $i: $mime")
                if (mime.startsWith("video")) {
                    val width = format.getInteger(MediaFormat.KEY_WIDTH)
                    val height = format.getInteger(MediaFormat.KEY_HEIGHT)
                    Log.d("dstest", "Frame size $width x $height")
                }
            }

            extractor.release()



        } catch (e: Exception) {
            Log.e("dstest", "Exception: ", e)
        }
    }

}