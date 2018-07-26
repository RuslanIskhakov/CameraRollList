package com.deltasoft.cameraroll.videoencoding

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.experimental.async

import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicReference

class ExtractDecodeEditEncodeMux {

    /** Width of the output frames.  */
    private val mWidth = 480
    /** Height of the output frames.  */
    private val mHeight = 320
    /** The destination file for the encoded output.  */
    private val mOutputFile = "/sdcard/Download/test_result_480x320.MP4"
    /** The input MP4 video file to be resized  */
    private val mInputFile = "/sdcard/DCIM/Camera/20180723_165258.mp4"

    fun test() {
        async {
            try {
                extractDecodeEditEncodeMux()
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ", e)
            }
        }
    }

    @Throws(Exception::class)
    private fun extractDecodeEditEncodeMux() {
        // Exception that may be thrown during release.
        var exception: Exception? = null

        var videoExtractor: MediaExtractor? = null
        var outputSurface: OutputSurface? = null
        var videoDecoder: MediaCodec? = null
        var videoEncoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputSurface: InputSurface? = null
        try {
            videoExtractor = createExtractor()
            val videoInputTrack = getAndSelectVideoTrackIndex(videoExtractor)
            val inputFormat = videoExtractor.getTrackFormat(videoInputTrack)
            // We avoid the device-specific limitations on width and height by using values
            // that are multiples of 16, which all tested devices seem to be able to handle.
            val outputVideoFormat = MediaFormat.createVideoFormat(OUTPUT_VIDEO_MIME_TYPE, mWidth, mHeight)
            // Set some properties. Failing to specify some of these can cause the MediaCodec
            // configure() call to throw an unhelpful exception.
            outputVideoFormat.setInteger(
                    MediaFormat.KEY_COLOR_FORMAT, OUTPUT_VIDEO_COLOR_FORMAT)
            outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_VIDEO_BIT_RATE)
            outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,
                    if (inputFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) inputFormat.getInteger(MediaFormat.KEY_FRAME_RATE) else 30)
            outputVideoFormat.setInteger(
                    MediaFormat.KEY_I_FRAME_INTERVAL, OUTPUT_VIDEO_IFRAME_INTERVAL)
            if (VERBOSE) Log.d(TAG, "video format: $outputVideoFormat")
            // Create a MediaCodec for the desired codec, then configure it as an encoder with
            // our desired properties. Request a Surface to use for input.
            val inputSurfaceReference = AtomicReference<Surface>()
            videoEncoder = createVideoEncoder(outputVideoFormat, inputSurfaceReference)
            inputSurface = InputSurface(inputSurfaceReference.get())
            inputSurface.makeCurrent()
            // Create a MediaCodec for the decoder, based on the extractor's format.
            outputSurface = OutputSurface()
            videoDecoder = createVideoDecoder(inputFormat, outputSurface.surface)
            // Creates a muxer but do not start or add tracks just yet.
            muxer = createMuxer()
            doExtractDecodeEditEncodeMux(
                    videoExtractor,
                    videoDecoder,
                    videoEncoder,
                    muxer,
                    inputSurface,
                    outputSurface)
        } finally {
            if (VERBOSE) Log.d(TAG, "releasing extractor, decoder, encoder, and muxer")
            // Try to release everything we acquired, even if one of the releases fails, in which
            // case we save the first exception we got and re-throw at the end (unless something
            // other exception has already been thrown). This guarantees the first exception thrown
            // is reported as the cause of the error, everything is (attempted) to be released, and
            // all other exceptions appear in the logs.
            try {
                videoExtractor?.release()
            } catch (e: Exception) {
                Log.e(TAG, "error while releasing videoExtractor", e)
                if (exception == null) {
                    exception = e
                }
            }

            try {
                if (videoDecoder != null) {
                    videoDecoder.stop()
                    videoDecoder.release()
                }
            } catch (e: Exception) {
                Log.e(TAG, "error while releasing videoDecoder", e)
                if (exception == null) {
                    exception = e
                }
            }

            try {
                outputSurface?.release()
            } catch (e: Exception) {
                Log.e(TAG, "error while releasing outputSurface", e)
                if (exception == null) {
                    exception = e
                }
            }

            try {
                if (videoEncoder != null) {
                    videoEncoder.stop()
                    videoEncoder.release()
                }
            } catch (e: Exception) {
                Log.e(TAG, "error while releasing videoEncoder", e)
                if (exception == null) {
                    exception = e
                }
            }

            try {
                if (muxer != null) {
                    muxer.stop()
                    muxer.release()
                }
            } catch (e: Exception) {
                Log.e(TAG, "error while releasing muxer", e)
                if (exception == null) {
                    exception = e
                }
            }

            try {
                inputSurface?.release()
            } catch (e: Exception) {
                Log.e(TAG, "error while releasing inputSurface", e)
                if (exception == null) {
                    exception = e
                }
            }

        }
        if (exception != null) {
            throw exception
        }
    }

    @Throws(IOException::class)
    private fun createExtractor(): MediaExtractor {
        val extractor: MediaExtractor
        extractor = MediaExtractor()
        val inputFile = File(mInputFile)
        extractor.setDataSource(inputFile.absolutePath)
        return extractor
    }

    /**
     * Creates a decoder for the given format, which outputs to the given surface.
     *
     * @param inputFormat the format of the stream to decode
     * @param surface into which to decode the frames
     */
    private fun createVideoDecoder(inputFormat: MediaFormat, surface: Surface?): MediaCodec {
        var decoder: MediaCodec? = null
        try {
            decoder = MediaCodec.createDecoderByType("video/avc")
        } catch (e: IOException) {
            Log.e(TAG, "Exception: ", e)
        }

        decoder!!.configure(inputFormat, surface, null, 0)
        decoder.start()
        return decoder
    }

    /**
     * Creates an encoder for the given format using the specified codec, taking input from a
     * surface.
     *
     *
     * The surface to use as input is stored in the given reference.
     *
     * @param format of the stream to be produced
     * @param surfaceReference to store the surface to use as input
     */
    private fun createVideoEncoder(
            format: MediaFormat?,
            surfaceReference: AtomicReference<Surface>): MediaCodec {
        var encoder: MediaCodec? = null
        try {
            encoder = MediaCodec.createEncoderByType("video/avc")
        } catch (e: IOException) {
            Log.e(TAG, "Exception (1): ", e)
        }

        Log.d(TAG, "createVideoEncoder: " + (null != format))
        encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        // Must be called before start() is.
        surfaceReference.set(encoder.createInputSurface())
        encoder.start()
        return encoder
    }

    /**
     * Creates a muxer to write the encoded frames.
     *
     *
     * The muxer is not started as it needs to be started only after all streams have been added.
     */
    @Throws(IOException::class)
    private fun createMuxer(): MediaMuxer {
        val outputFile = File(mOutputFile)
        if (outputFile.exists()) outputFile.delete()
        return MediaMuxer(mOutputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    private fun getAndSelectVideoTrackIndex(extractor: MediaExtractor): Int {
        for (index in 0 until extractor.trackCount) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is "
                        + getMimeTypeFor(extractor.getTrackFormat(index)))
            }
            if (isVideoFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index)
                return index
            }
        }
        return -1
    }

    /**
     * Does the actual work for extracting, decoding, encoding and muxing.
     */
    private fun doExtractDecodeEditEncodeMux(
            videoExtractor: MediaExtractor,
            videoDecoder: MediaCodec?,
            videoEncoder: MediaCodec?,
            muxer: MediaMuxer,
            inputSurface: InputSurface,
            outputSurface: OutputSurface) {

        var videoDecoderOutputBufferInfo: MediaCodec.BufferInfo? = null
        var videoEncoderOutputBufferInfo: MediaCodec.BufferInfo? = null
        videoDecoderOutputBufferInfo = MediaCodec.BufferInfo()
        videoEncoderOutputBufferInfo = MediaCodec.BufferInfo()
        // We will get these from the decoders when notified of a format change.
        var decoderOutputVideoFormat: MediaFormat? = null
        // We will get these from the encoders when notified of a format change.
        var encoderOutputVideoFormat: MediaFormat? = null
        // We will determine these once we have the output format.
        var outputVideoTrack = -1
        // Whether things are done on the video side.
        var videoExtractorDone = false
        var videoDecoderDone = false
        var videoEncoderDone = false
        var muxing = false
        var videoExtractedFrameCount = 0
        var videoDecodedFrameCount = 0
        var videoEncodedFrameCount = 0
        while (!videoEncoderDone) {
            if (VERBOSE) {
                Log.d(TAG, String.format(
                        "loop: "
                                + "{"
                                + "extracted:%d(done:%b) "
                                + "decoded:%d(done:%b) "
                                + "encoded:%d(done:%b)} "
                                + "muxing:%b(V:%d)",
                        videoExtractedFrameCount, videoExtractorDone,
                        videoDecodedFrameCount, videoDecoderDone,
                        videoEncodedFrameCount, videoEncoderDone,
                        muxing, outputVideoTrack))
            }
            // Extract video from file and feed to decoder.
            // Do not extract video if we have determined the output format but we are not yet
            // ready to mux the frames.
            while (!videoExtractorDone && (encoderOutputVideoFormat == null || muxing)) {
                val decoderInputBufferIndex = videoDecoder!!.dequeueInputBuffer(TIMEOUT_USEC.toLong())
                if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no video decoder input buffer")
                    break
                }
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned input buffer: $decoderInputBufferIndex")
                }
                val decoderInputBuffer = videoDecoder.getInputBuffer(decoderInputBufferIndex)
                val size = videoExtractor.readSampleData(decoderInputBuffer!!, 0)
                val presentationTime = videoExtractor.sampleTime
                if (VERBOSE) {
                    Log.d(TAG, "video extractor: returned buffer of size $size")
                    Log.d(TAG, "video extractor: returned buffer for time $presentationTime")
                }
                if (size >= 0) {
                    videoDecoder.queueInputBuffer(
                            decoderInputBufferIndex,
                            0,
                            size,
                            presentationTime,
                            videoExtractor.sampleFlags)
                }
                videoExtractorDone = !videoExtractor.advance()
                if (videoExtractorDone) {
                    if (VERBOSE) Log.d(TAG, "video extractor: EOS")
                    videoDecoder.queueInputBuffer(
                            decoderInputBufferIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                }
                videoExtractedFrameCount++
                // We extracted a frame, let's try something else next.
                break
            }

            // Poll output frames from the video decoder and feed the encoder.
            while (!videoDecoderDone && (encoderOutputVideoFormat == null || muxing)) {
                val decoderOutputBufferIndex = videoDecoder!!.dequeueOutputBuffer(
                        videoDecoderOutputBufferInfo, TIMEOUT_USEC.toLong())
                if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no video decoder output buffer")
                    break
                }
                if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    if (VERBOSE) Log.d(TAG, "video decoder: output buffers changed")
                    break
                }
                if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    decoderOutputVideoFormat = videoDecoder.outputFormat
                    if (VERBOSE) {
                        Log.d(TAG, "video decoder: output format changed: " + decoderOutputVideoFormat!!)
                    }
                    break
                }
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned output buffer: $decoderOutputBufferIndex")
                    Log.d(TAG, "video decoder: returned buffer of size " + videoDecoderOutputBufferInfo.size)
                }
                val decoderOutputBuffer = videoDecoder.getOutputBuffer(decoderOutputBufferIndex)
                if (videoDecoderOutputBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    if (VERBOSE) Log.d(TAG, "video decoder: codec config buffer")
                    videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false)
                    break
                }
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned buffer for time " + videoDecoderOutputBufferInfo.presentationTimeUs)
                }
                val render = videoDecoderOutputBufferInfo.size != 0
                videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, render)
                if (render) {
                    if (VERBOSE) Log.d(TAG, "output surface: await new image")
                    outputSurface.awaitNewImage()
                    // Edit the frame and send it to the encoder.
                    if (VERBOSE) Log.d(TAG, "output surface: draw image")
                    outputSurface.drawImage()
                    inputSurface.setPresentationTime(
                            videoDecoderOutputBufferInfo.presentationTimeUs * 1000)
                    if (VERBOSE) Log.d(TAG, "input surface: swap buffers")
                    inputSurface.swapBuffers()
                    if (VERBOSE) Log.d(TAG, "video encoder: notified of new frame")
                }
                if (videoDecoderOutputBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    if (VERBOSE) Log.d(TAG, "video decoder: EOS")
                    videoDecoderDone = true
                    videoEncoder!!.signalEndOfInputStream()
                }
                videoDecodedFrameCount++
                // We extracted a pending frame, let's try something else next.
                break
            }

            // Poll frames from the video encoder and send them to the muxer.
            while (!videoEncoderDone && (encoderOutputVideoFormat == null || muxing)) {
                val encoderOutputBufferIndex = videoEncoder!!.dequeueOutputBuffer(
                        videoEncoderOutputBufferInfo, TIMEOUT_USEC.toLong())
                if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (VERBOSE) Log.d(TAG, "no video encoder output buffer")
                    break
                }
                if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    if (VERBOSE) Log.d(TAG, "video encoder: output buffers changed")
                    break
                }
                if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (VERBOSE) Log.d(TAG, "video encoder: output format changed")
                    if (outputVideoTrack >= 0) {
                        //TODO fail("video encoder changed its output format again?");
                    }
                    encoderOutputVideoFormat = videoEncoder.outputFormat
                    break
                }
                if (VERBOSE) {
                    Log.d(TAG, "video encoder: returned output buffer: $encoderOutputBufferIndex")
                    Log.d(TAG, "video encoder: returned buffer of size " + videoEncoderOutputBufferInfo.size)
                }
                val encoderOutputBuffer = videoEncoder.getOutputBuffer(encoderOutputBufferIndex)
                if (videoEncoderOutputBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    if (VERBOSE) Log.d(TAG, "video encoder: codec config buffer")
                    // Simply ignore codec config buffers.
                    videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false)
                    break
                }
                if (VERBOSE) {
                    Log.d(TAG, "video encoder: returned buffer for time " + videoEncoderOutputBufferInfo.presentationTimeUs)
                }
                if (videoEncoderOutputBufferInfo.size != 0) {
                    muxer.writeSampleData(
                            outputVideoTrack, encoderOutputBuffer!!, videoEncoderOutputBufferInfo)
                }
                if (videoEncoderOutputBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    if (VERBOSE) Log.d(TAG, "video encoder: EOS")
                    videoEncoderDone = true
                }
                videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false)
                videoEncodedFrameCount++
                // We enqueued an encoded frame, let's try something else next.
                break
            }

            if (!muxing && encoderOutputVideoFormat != null) {
                Log.d(TAG, "muxer: adding video track.")
                outputVideoTrack = muxer.addTrack(encoderOutputVideoFormat)
                Log.d(TAG, "muxer: starting")
                muxer.start()
                muxing = true
            }
        }
        // TODO: Check the generated output file.
    }

    companion object {
        private val TAG = ExtractDecodeEditEncodeMux::class.java.simpleName
        private val VERBOSE = false // lots of logging
        /** How long to wait for the next buffer to become available.  */
        private val TIMEOUT_USEC = 10000
        /** Where to output the test files.  */
        private val OUTPUT_FILENAME_DIR = Environment.getExternalStorageDirectory()
        // parameters for the video encoder
        private val OUTPUT_VIDEO_MIME_TYPE = "video/avc" // H.264 Advanced Video Coding
        private val OUTPUT_VIDEO_BIT_RATE = 2000000 // 2Mbps
        private val OUTPUT_VIDEO_IFRAME_INTERVAL = 10 // 10 seconds between I-frames
        private val OUTPUT_VIDEO_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        private fun isVideoFormat(format: MediaFormat): Boolean {
            return getMimeTypeFor(format).startsWith("video/")
        }

        private fun getMimeTypeFor(format: MediaFormat): String {
            return format.getString(MediaFormat.KEY_MIME)
        }
    }
}
