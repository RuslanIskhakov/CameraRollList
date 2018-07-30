package com.deltasoft.cameraroll.mvp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.deltasoft.cameraroll.videoencoding.ExtractDecodeEditEncodeMux
import com.deltasoft.cameraroll.adapter.ContentsAdapter
import com.deltasoft.cameraroll.adapter.ContentsItem
import com.deltasoft.cameraroll.enums.ContentsType
import com.deltasoft.cameraroll.interfaces.OnPlusButtonClickListener
import kotlinx.android.synthetic.main.activity_main_list.*
import com.erikagtierrez.multiple_media_picker.Gallery
import com.deltasoft.cameraroll.R
import com.deltasoft.cameraroll.mvp.interfaces.MainListViewInterface

/**
 * The single activity that is a View for MVP architecture pattern
 */

class MainListActivity : AppCompatActivity(), OnPlusButtonClickListener, MainListViewInterface {

    companion object {
        val OPEN_MEDIA_PICKER = 100
        private val TAG = MainListActivity::class.java.simpleName
    }

    private val mPresenter = MainListPresenter.instance
    private var mAdapter: ContentsAdapter? = null
    private var mItems: ArrayList<ContentsItem> = ArrayList<ContentsItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_list)

        Log.d(TAG, "onCreate()")
        setupContents()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()")
        mPresenter.bindView(this)
    }

    override fun onStop() {
        Log.d(TAG, "onStop()")
        mPresenter.unbindView(this)
        super.onStop()
    }

    private fun requestWriteExternalStoragePermission() {
        Log.d(TAG, "requestWriteExternalStoragePermission()")
        val permissions = arrayOfNulls<String>(1)
        permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 101 && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mPresenter.onPlusButtonClick()
        }
    }

    private fun setupContents() {
        Log.d(TAG, "setupContents()")
        if (mAdapter == null) {
            mAdapter = ContentsAdapter(this, mItems, this)
            contentsRecyclerView.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
            contentsRecyclerView.setAdapter(mAdapter)
        } else {
            mAdapter?.items = mItems
        }
    }

    override fun onPlusButtonClick() {
        Log.d(TAG, "onPlusButtonClick()")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mPresenter.onPlusButtonClick()
        } else {
            requestWriteExternalStoragePermission()
        }
    }

    override fun startMediaPicker() {
        Log.d(TAG, "startMediaPicker()")
        val intent = Intent(this, Gallery::class.java)
        intent.putExtra("title", "Select media")
        // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
        intent.putExtra("mode", 1)
        intent.putExtra("maxSelection", 1) // Optional
        startActivityForResult(intent, OPEN_MEDIA_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == OPEN_MEDIA_PICKER) {
            if (resultCode == RESULT_OK && data != null) {
                mPresenter.bindView(this) //it has been unbound
                val selectionResult = data.getStringArrayListExtra("result")
                mPresenter.onMediaSelectionResult(selectionResult)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun addItem(item: ContentsItem) {
        Log.d(TAG, "addItem()")
        runOnUiThread(Runnable {
            mItems.add(item)
        })
    }

    override fun notifyContentsDataSetChanged() {
        runOnUiThread(Runnable {
            Log.d(TAG, "notifyContentsDataSetChanged()")
            setupContents()
        })
    }

    override fun showProgressView() {
        runOnUiThread(Runnable {
            Log.d(TAG, "showProgressView()")
            contentsRecyclerView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            progressBar.bringToFront()
        })
    }

    override fun hideProgressView() {
        runOnUiThread(Runnable {
            Log.d(TAG, "hideProgressView()")
            contentsRecyclerView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        })
    }

    override fun showErrorMessage(message: String) {
        runOnUiThread(Runnable {
            Log.d(TAG, "showErrorMessage(): $message")
            AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(getString(R.string.error_text))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.close_text), null)
                    .show()
        })
    }
}
