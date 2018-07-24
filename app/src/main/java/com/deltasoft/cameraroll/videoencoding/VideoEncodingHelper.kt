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
        try {
            var width: Int? = null
            var height: Int? = null
            var videoFormat: MediaFormat? = null

            val inputFile = File(inputFilePath)
            Log.d("dstest", "decodeTask: "+ inputFile.exists())

            if (!inputFile.canRead()) {
                throw RuntimeException("Unable to read " + inputFile);
            }


            //Extractor
            val extractor = MediaExtractor()
            extractor.setDataSource(inputFile.absolutePath)
            val numTracks = extractor.getTrackCount()
            Log.d("dstest", "Tracks: "+numTracks)
            for (i in 0..(numTracks-1)) {
                val format = extractor.getTrackFormat(i)
                if (null != format) {
                    val mime = format.getString(MediaFormat.KEY_MIME)
                    Log.d("dstest", "Track $i: $mime")
                    if (mime.startsWith("video")) {
                        width = format.getInteger(MediaFormat.KEY_WIDTH)
                        height = format.getInteger(MediaFormat.KEY_HEIGHT)
                        Log.d("dstest", "Frame size $width x $height")
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

                    Log.d("dstest", "Decode cycle start")
                    var bytesRead = 0
                    var totalBytesRead = 0
                    while (mRunning) {
                        if (bytesRead>=0) {
                            val index = mDecoder?.dequeueInputBuffer(10000) ?: -1
                            if (index >= 0) {
                                val buffer = mDecoder?.getInputBuffer(index)
                                bytesRead = extractor.readSampleData(buffer, 0)
                                Log.d("dstest", "bytesRead: $bytesRead")
                                if (bytesRead>0) {
                                    totalBytesRead += bytesRead
                                }
                                var flags = extractor.getSampleFlags()
                                if (bytesRead<0) flags = flags or MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                // сообщаем системе о доступности буфера данных
                                mDecoder?.queueInputBuffer(index, 0, if (bytesRead>0) bytesRead else 0, 10000L, flags)
                                extractor.advance()
                                Log.d("dstest", "queueInputBuffer passed: $bytesRead/$totalBytesRead")
                            }
                        }

                        encodeTask()
                    }
                    Log.d("dstest", "Decode cycle end")

                    extractor.release()
                    mDecoder?.stop()
                    mDecoder?.release()
                }
            }


        } catch (e: Exception) {
            Log.e("dstest", "Exception: ", e)
        }
    }

    private fun encodeTask() {
        try {

            val info = BufferInfo()

            val index = mDecoder?.dequeueOutputBuffer(info, 10000L) ?: -100
            // заканчиваем работу декодера если достигнут конец потока данных
            if (index >= 0) { // буфер с индексом index доступен
                // info.size > 0: если буфер не нулевого размера, то рендерим на Surface
                val buffer = mDecoder?.getOutputBuffer(index)
                Log.d("encodeTask", "Bytes decoded: "+info.size)
                mDecoder?.releaseOutputBuffer(index, info.size > 0)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM === MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    mRunning = false
                    Log.d("encodeTask", "End of stream")
                }
            } else {
                Log.d("encodeTask", "Index: "+index)
            }

        } catch (e: Exception) {
            Log.e("encodeTask", "Exception: ", e)
        }
    }

}