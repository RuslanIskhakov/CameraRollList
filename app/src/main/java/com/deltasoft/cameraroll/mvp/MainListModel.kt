package com.deltasoft.cameraroll.mvp

import android.util.Log
import com.deltasoft.cameraroll.mvp.interfaces.MainListModelInterface

class MainListModel: MainListModelInterface {
    override fun logDebug(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun logError(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }
}