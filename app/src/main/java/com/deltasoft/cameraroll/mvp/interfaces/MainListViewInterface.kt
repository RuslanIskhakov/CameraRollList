package com.deltasoft.cameraroll.mvp.interfaces

import com.deltasoft.cameraroll.adapter.ContentsItem

interface MainListViewInterface {
    fun startMediaPicker()
    fun addItem(item: ContentsItem)
    fun notifyContentsDataSetChanged()
    fun showProgressView()
    fun hideProgressView()
    fun showErrorMessage(message: String)
}