package com.deltasoft.cameraroll.mvp.interfaces

/**
 * Interface for MainListModel
 */

interface MainListModelInterface {
    fun logDebug(tag: String, message: String)
    fun logError(tag: String, message: String, throwable: Throwable)
    fun isReadyForVideoProcessing(): Boolean
    fun submitVideoFileToProcess(filePath: String)
    fun onVideoProcessingSuccess(outputPath: String)
    fun onVideoProcessingFailure(errorMessage: String)
}