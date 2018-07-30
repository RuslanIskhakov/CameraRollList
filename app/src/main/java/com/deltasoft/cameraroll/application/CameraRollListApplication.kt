package com.deltasoft.cameraroll.application

import android.app.Application
import android.util.Log
import com.deltasoft.cameraroll.mvp.MainListModel
import com.deltasoft.cameraroll.mvp.MainListPresenter

class CameraRollListApplication: Application() {

    companion object {
        private val TAG = CameraRollListApplication::class.java.simpleName
    }

    private val mainListModel = MainListModel.getInstance(this)

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")
    }
}