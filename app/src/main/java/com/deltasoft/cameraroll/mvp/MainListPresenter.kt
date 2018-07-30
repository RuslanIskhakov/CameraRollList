package com.deltasoft.cameraroll.mvp

import com.deltasoft.cameraroll.adapter.ContentsItem
import com.deltasoft.cameraroll.enums.ContentsType
import com.deltasoft.cameraroll.mvp.interfaces.MainListModelInterface
import com.deltasoft.cameraroll.mvp.interfaces.MainListViewInterface
import java.lang.ref.WeakReference

class MainListPresenter (val model: MainListModelInterface){

    private object Holder { val INSTANCE = MainListPresenter(MainListModel()) }

    companion object {
        val instance: MainListPresenter by lazy { Holder.INSTANCE }
        private val TAG = MainListPresenter::class.java.simpleName
    }

    private var view: WeakReference<MainListViewInterface>? = null

    fun bindView(view: MainListViewInterface): Boolean {
        synchronized(this) {
            model.logDebug(TAG, "bindView: "+view.hashCode())
            if (isBound()) {
                return false
            } else {
                this.view = WeakReference(view)
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
                            strongView.addItem(ContentsItem(ContentsType.VIDEO, it))
                        } else {
                            strongView.addItem(ContentsItem(ContentsType.IMAGE, "file://" + it))
                        }
                    }
                    strongView.notifyContentsDataSetChanged()
                }
            }
        }
    }
}