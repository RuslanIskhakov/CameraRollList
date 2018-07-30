package com.deltasoft.cameraroll

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import com.deltasoft.cameraroll.videoencoding.ExtractDecodeEditEncodeMux
import com.deltasoft.cameraroll.adapter.ContentsAdapter
import com.deltasoft.cameraroll.adapter.ContentsItem
import com.deltasoft.cameraroll.enums.ContentsType
import com.deltasoft.cameraroll.interfaces.OnPlusButtonClickListener
import kotlinx.android.synthetic.main.activity_main_list.*
import android.R.attr.data
import android.content.pm.ActivityInfo
import com.erikagtierrez.multiple_media_picker.Gallery
import android.R.attr.data
import android.util.Log


class MainListActivity : AppCompatActivity(), OnPlusButtonClickListener {

    companion object {
        public val OPEN_MEDIA_PICKER = 100
    }

    private var mAdapter: ContentsAdapter? = null
    private var mItems: ArrayList<ContentsItem> = ArrayList<ContentsItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list)

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //TODO make enum for contents type image/video
//        mItems.add(ContentsItem(ContentsType.VIDEO, "/sdcard/DCIM/Camera/20180723_165258.mp4"))
//        mItems.add(ContentsItem(ContentsType.IMAGE, "file:///sdcard/Download/fuu_400x400.jpg"))
//        mItems.add(ContentsItem(ContentsType.VIDEO, "/sdcard/DCIM/Camera/20180723_165258.mp4"))
//        mItems.add(ContentsItem(ContentsType.VIDEO, "/sdcard/DCIM/Camera/20180723_165258.mp4"))
//        mItems.add(ContentsItem(ContentsType.IMAGE, "file:///sdcard/Download/fuu_400x400.jpg"))
//        mItems.add(ContentsItem(ContentsType.VIDEO, "/sdcard/DCIM/Camera/20180723_165258.mp4"))
//        mItems.add(ContentsItem(ContentsType.VIDEO, "/sdcard/DCIM/Camera/20180723_165258.mp4"))
//        mItems.add(ContentsItem(ContentsType.VIDEO, "/sdcard/DCIM/Camera/20180723_165258.mp4"))

        setupContents()


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //test()
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
            //test()
        }
    }

    private fun test() {
        val test = ExtractDecodeEditEncodeMux()
        test.test()
    }

    private fun setupContents() {
        if (mAdapter == null) {
            mAdapter = ContentsAdapter(this, mItems, this)
            contentsRecyclerView.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
            contentsRecyclerView.setAdapter(mAdapter)
        } else {
            mAdapter?.items = mItems
        }
    }

    override fun onPlusButtonClick() {
        val intent = Intent(this, Gallery::class.java)
        intent.putExtra("title", "Select media")
        // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
        intent.putExtra("mode", 1)
        intent.putExtra("maxSelection", 10) // Optional
        startActivityForResult(intent, OPEN_MEDIA_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode === OPEN_MEDIA_PICKER) {
            if (resultCode === RESULT_OK && data != null) {
                val selectionResult = data.getStringArrayListExtra("result")
                if (selectionResult.size>0) {
                    selectionResult.forEach {
                        if (it.toLowerCase().endsWith(".mp4")) {
                            mItems.add(ContentsItem(ContentsType.VIDEO, it))
                        } else {
                            mItems.add(ContentsItem(ContentsType.IMAGE, "file://"+it))
                        }
                    }
                    setupContents()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
