package com.deltasoft.cameraroll.videoencoding

import android.util.Log
import kotlinx.coroutines.experimental.async
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.nio.ByteBuffer
import android.R.attr.data
import kotlinx.coroutines.experimental.delay
import java.io.BufferedInputStream
import java.io.FileInputStream
import android.media.MediaCodec.BufferInfo


class VideoEncodingHelper {

    private val INPUT_BUFFER_SIZE = 16384

    private val inputFilePath = "/sdcard/DCIM/Camera/20180723_165258.mp4"

    private var mRunning = false

    var mDecoder: MediaCodec? = null

    public fun start() {

        mRunning = true

        async{
            decodeTask()
        }
//        async{
//            encodeTask()
//        }

    }

    private fun decodeTask() {
        val extractor = MediaExtractor()
        try {
            var width: Int? = null
            var height: Int? = null
            var videoFormat: MediaFormat? = null

            val inputFile = File(inputFilePath)
            Log.d("decodeTask", "decodeTask: "+ inputFile.exists())

            if (!inputFile.canRead()) {
                throw RuntimeException("Unable to read " + inputFile);
            }

            extractor.setDataSource(inputFile.absolutePath)
            val numTracks = extractor.getTrackCount()
            Log.d("decodeTask", "Tracks: "+numTracks)
            for (i in 0..(numTracks-1)) {
                val format = extractor.getTrackFormat(i)
                if (null != format) {
                    val mime = format.getString(MediaFormat.KEY_MIME)
                    Log.d("decodeTask", "Track $i: $mime")
                    if (mime.startsWith("video")) {
                        width = format.getInteger(MediaFormat.KEY_WIDTH)
                        height = format.getInteger(MediaFormat.KEY_HEIGHT)
                        Log.d("decodeTask", "Frame size $width x $height")
                        videoFormat = format
                        extractor.selectTrack(i)
                        break
                    }
                }
            }

            //Decode

            if (null!=width && null!=height && null!=videoFormat) {
//                val format = MediaFormat.createVideoFormat("video/avc", width, height)
//                // передаем наш csd-0
//                format.setByteBuffer("csd-0", ByteBuffer.allocate(1000))
                // создаем декодер
                mDecoder = MediaCodec.createDecoderByType("video/avc")
                // конфигурируем декодер

                if (null!=mDecoder) {
                    mDecoder?.configure(videoFormat, null, null, 0)
                    mDecoder?.start()

                    Log.d("decodeTask", "Decode cycle start")
                    var totalBytesRead = 0
                    var eos = false
                    while (mRunning) {
                        if (!eos) {
                            val index = mDecoder?.dequeueInputBuffer(10000) ?: -1
                            Log.d("decodeTask", "Index: $index")
                            if (index >= 0) {
                                val buffer = mDecoder?.getInputBuffer(index)
                                val bytesRead = extractor.readSampleData(buffer, 0)
                                if (bytesRead > 0) {
                                    totalBytesRead += bytesRead
                                }
                                var flags = extractor.getSampleFlags()
                                eos = (flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM)//!extractor.advance()
                                if (eos) {
                                    flags = flags or MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                }
                                mDecoder?.queueInputBuffer(index, 0, if (bytesRead > 0) bytesRead else 0, 10000L, flags)
                                extractor.advance()
                                Log.d("decodeTask", "queueInputBuffer passed: $bytesRead/$totalBytesRead")
                                if (eos) {
                                    Log.d("decodeTask", "End of stream")
                                }
                            }
                        }
                        encodeTask()
                        Thread.sleep(10)
                    }
                    Log.d("decodeTask", "Decode cycle end")
                }
            }


        } catch (e: Exception) {
            Log.e("decodeTask", "Exception: ", e)
        } finally {
            extractor.release()
            mDecoder?.stop()
            mDecoder?.release()
        }
    }

    private fun encodeTask() {
        try {
            
            val info = BufferInfo()

            val index = mDecoder?.dequeueOutputBuffer(info, 10000L) ?: -100
            if (index >= 0) {
                val buffer = mDecoder?.getOutputBuffer(index)
                Log.d("encodeTask", "Bytes decoded: " + info.size)
                mDecoder?.releaseOutputBuffer(index, info.size > 0)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.d("encodeTask", "End of stream")
                    mRunning = false
                }
            } else {
                Log.d("encodeTask", "Index: " + index)
            }

        } catch (e: Exception) {
            Log.e("encodeTask", "Exception: ", e)
        }
    }

}