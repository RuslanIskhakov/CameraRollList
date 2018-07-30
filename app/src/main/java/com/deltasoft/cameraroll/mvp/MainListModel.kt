package com.deltasoft.cameraroll.mvp

import android.content.Context
import android.util.Log
import com.deltasoft.cameraroll.enums.MainListModelState
import com.deltasoft.cameraroll.mvp.interfaces.MainListModelInterface
import com.deltasoft.cameraroll.videoencoding.ExtractDecodeEditEncodeMux
import java.io.File
import java.util.*

class MainListModel (val context: Context) : MainListModelInterface {

    companion object {
        @Volatile private var INSTANCE: MainListModel? = null

        fun getInstance(context: Context): MainListModel =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildModel(context).also { INSTANCE = it }
                }

        private fun buildModel(context: Context) = MainListModel(context)

        fun getInstance(): MainListModel = INSTANCE!!

        private val TAG = MainListModel::class.java.simpleName
    }

    private var state: MainListModelState = MainListModelState.READY
    private val videoEncoder: ExtractDecodeEditEncodeMux by lazy {ExtractDecodeEditEncodeMux()}



    override fun logDebug(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun logError(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }

    override fun isReadyForVideoProcessing(): Boolean {
        synchronized(this) {
            return state == MainListModelState.READY
        }
    }

    override fun submitVideoFileToProcess(filePath: String) {
        synchronized(this) {
            if (isReadyForVideoProcessing()) {
                state = MainListModelState.PROCESSING_VIDEO
                val outputPath = File(context.filesDir, UUID.randomUUID().toString() + ".mp4").absolutePath
                Log.d(TAG, "submitVideoFileToProcess:"+filePath+"|"+outputPath)
                videoEncoder.start(filePath,outputPath)
            }
        }
    }

    override fun onVideoProcessingSuccess(outputPath: String) {
        synchronized(this) {
            state = MainListModelState.READY
            MainListPresenter.instance.onVideoProcessingSuccess(outputPath)
        }
    }

    override fun onVideoProcessingFailure(errorMessage: String) {
        synchronized(this) {
            state = MainListModelState.READY
            MainListPresenter.instance.onVideoProcessingFailure(errorMessage)
        }
    }
}