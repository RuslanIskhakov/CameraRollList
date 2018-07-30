package com.deltasoft.cameraroll.mvp

import com.deltasoft.cameraroll.adapter.ContentsItem
import com.deltasoft.cameraroll.enums.ContentsType
import com.deltasoft.cameraroll.mvp.interfaces.MainListModelInterface
import com.deltasoft.cameraroll.mvp.interfaces.MainListViewInterface
import java.lang.ref.WeakReference

class MainListPresenter (val model: MainListModelInterface){

    private object Holder { val INSTANCE = MainListPresenter(MainListModel.getInstance()) }

    companion object {
        val instance: MainListPresenter by lazy { Holder.INSTANCE }
        private val TAG = MainListPresenter::class.java.simpleName
    }

    private var view: WeakReference<MainListViewInterface>? = null
    private var decodedVideoFile: String? = null

    fun bindView(view: MainListViewInterface): Boolean {
        synchronized(this) {
            model.logDebug(TAG, "bindView: "+view.hashCode())
            if (isBound()) {
                return false
            } else {
                this.view = WeakReference(view)
                if (model.isReadyForVideoProcessing()) {
                    view.hideProgressView()
                    if (null!=decodedVideoFile) {
                        view.addItem(ContentsItem(ContentsType.VIDEO, decodedVideoFile!!))
                        view.notifyContentsDataSetChanged()
                        decodedVideoFile = null
                    }
                } else {
                    view.showProgressView()
                }
                return true
            }

        }
    }

    fun unbindView(view: MainListViewInterface): Boolean {
        synchronized(this) {
            model.logDebug(TAG, "unbindView: "+view.hashCode())
            if (isBound() && this.view?.get()===view) {
                this.view = null
                return true
            } else {
                return false
            }
        }
    }

    private fun isBound():Boolean {
        return this.view?.get()!=null
    }

    fun onPlusButtonClick() {
        synchronized(this) {
            model?.logDebug(TAG, "onPlusButtonClick()")
            val strongView = view?.get()
            if (null!=strongView) {
                strongView.startMediaPicker()
            }
        }
    }

    fun onMediaSelectionResult(selectionResult: ArrayList<String>) {
        synchronized(this) {
            val strongView = view?.get()
            model.logDebug(TAG, "onMediaSelectionResult: "+(strongView!=null)+"|"+selectionResult.size)
            if (null != strongView) {
                if (selectionResult.size > 0) {
                    selectionResult.forEach {
                        if (it.toLowerCase().endsWith(".mp4")) {
                            decodedVideoFile = null
                            strongView.showProgressView()
                            model.submitVideoFileToProcess(it)
                        } else {
                            strongView.addItem(ContentsItem(ContentsType.IMAGE, "file://" + it))
                        }
                    }
                    strongView.notifyContentsDataSetChanged()
                }
            }
        }
    }

    fun onVideoProcessingSuccess(outputPath: String) {
        synchronized(this) {
            model?.logDebug(TAG, "onVideoProcessingSuccess: $outputPath")
            val strongView = view?.get()
            if (null!=strongView) {
                strongView.addItem(ContentsItem(ContentsType.VIDEO, outputPath))
                strongView.notifyContentsDataSetChanged()
                strongView.hideProgressView()
                decodedVideoFile = null
            } else {
                decodedVideoFile = String.format(outputPath)
            }
        }
    }

    fun onVideoProcessingFailure(errorMessage: String) {
        synchronized(this) {
            model?.logDebug(TAG, "onVideoProcessingFailure: $errorMessage")
            val strongView = view?.get()
            if (null!=strongView) {
                strongView.showErrorMessage(errorMessage)
            }
        }
    }
}