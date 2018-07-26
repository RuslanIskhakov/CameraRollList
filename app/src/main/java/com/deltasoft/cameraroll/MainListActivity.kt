package com.deltasoft.cameraroll

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import com.deltasoft.cameraroll.videoencoding.ExtractDecodeEditEncodeMux
import com.deltasoft.cameraroll.videoencoding.VideoEncodingHelper

class MainListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            test()
        } else {
            requestWriteExternalStoragePermission()
        }

    }

    private fun requestWriteExternalStoragePermission() {
        val permissions = arrayOfNulls<String>(1)
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 101 && permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            test()
        }
    }

    private fun test() {
        val test = ExtractDecodeEditEncodeMux()
        test.test()
    }
}
