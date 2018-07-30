package com.deltasoft.cameraroll.mvp.interfaces

interface MainListModelInterface {
    fun logDebug(tag: String, message: String)
    fun logError(tag: String, message: String, throwable: Throwable)
}