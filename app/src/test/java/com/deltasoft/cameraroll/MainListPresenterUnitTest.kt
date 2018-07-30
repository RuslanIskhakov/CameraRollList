package com.deltasoft.cameraroll

import android.util.Log
import com.deltasoft.cameraroll.adapter.ContentsItem
import com.deltasoft.cameraroll.mvp.MainListPresenter
import com.deltasoft.cameraroll.mvp.interfaces.MainListModelInterface
import com.deltasoft.cameraroll.mvp.interfaces.MainListViewInterface
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MainListPresenterUnitTest {
    @Test
    fun testMainListPresenter() {
        val view1 = MainListViewStub()
        val view2 = MainListViewStub()

        val presenter = MainListPresenter(MainListModelStub())

        assertNotNull(presenter)

        assertTrue(presenter.bindView(view1))
        assertFalse(presenter.bindView(view1))
        assertFalse(presenter.bindView(view2))
        assertFalse(presenter.unbindView(view2))
        assertTrue(presenter.unbindView(view1))

        assertTrue(presenter.bindView(view2))
        assertFalse(presenter.bindView(view2))
        assertFalse(presenter.bindView(view1))
        assertFalse(presenter.unbindView(view1))
        assertTrue(presenter.unbindView(view2))
    }

    class MainListViewStub: MainListViewInterface {
        override fun showProgressView() {
        }

        override fun hideProgressView() {
        }

        override fun showErrorMessage(message: String) {
        }

        override fun startMediaPicker() {
        }

        override fun addItem(item: ContentsItem) {
        }

        override fun notifyContentsDataSetChanged() {
        }

    }

    class MainListModelStub: MainListModelInterface {
        override fun isReadyForVideoProcessing(): Boolean {
            return true
        }

        override fun submitVideoFileToProcess(filePath: String) {
        }

        override fun onVideoProcessingSuccess(outputPath: String) {
        }

        override fun onVideoProcessingFailure(errorMessage: String) {
        }

        override fun logDebug(tag: String, message: String) {
            print(String.format("DEBUG: %s :: %s\n", tag, message))
        }

        override fun logError(tag: String, message: String, throwable: Throwable) {
            print(String.format("ERROR: %s :: %s %s\n", tag, message, throwable.localizedMessage))
        }
    }
}
