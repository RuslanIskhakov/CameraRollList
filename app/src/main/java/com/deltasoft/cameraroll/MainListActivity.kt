package com.deltasoft.cameraroll

import android.Manifest
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
import kotlinx.android.synthetic.main.activity_main_list.*

class MainListActivity : AppCompatActivity() {

    private var mAdapter: ContentsAdapter? = null
    private var mItems: ArrayList<ContentsItem> = ArrayList<ContentsItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list)

        //TODO make enum for contents type image/video
        mItems.add(ContentsItem(ContentsType.VIDEO, "/sdcard/DCIM/Camera/20180723_165258.mp4"))
        mItems.add(ContentsItem(ContentsType.IMAGE, "file:///sdcard/Download/fuu_400x400.jpg"))
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
            mAdapter = ContentsAdapter(this, mItems)
            contentsRecyclerView.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
            contentsRecyclerView.setAdapter(mAdapter)
        } else {
            mAdapter?.items = mItems
        }
    }
}
