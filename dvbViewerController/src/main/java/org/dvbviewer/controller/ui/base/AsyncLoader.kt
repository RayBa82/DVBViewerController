/*
 * Copyright © 2013 dvbviewer-controller Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.dvbviewer.controller.ui.base

import android.content.Context

import androidx.loader.content.AsyncTaskLoader


import java.io.Closeable

/**
 * Loader which extends AsyncTaskLoaders and handles caveats
 * as pointed out in http://code.google.com/p/android/issues/detail?id=14944.
 *
 * Based on CursorLoader.java in the Fragment compatibility package
 *
 * @param <D> mData type
 * @author RayBa
 * @date 07.04.2013
</D> */
abstract class AsyncLoader<D>
/**
 * Instantiates a new async loader.
 *
 * @param context the context
 * @author RayBa
 * @date 07.04.2013
 */
(context: Context) : AsyncTaskLoader<D>(context) {
    private var mData: D? = null


    /* (non-Javadoc)
	 * @see android.support.v4.content.Loader#deliverResult(java.lang.Object)
	 */
    override fun deliverResult(data: D?) {
        if (isReset) {
            // An async query came in while the loader is stopped
            closeData(data)
            return
        }

        if (isStarted) {
            super.deliverResult(data)
        }

        val oldData = mData
        mData = data
        if (oldData !== mData) {
            closeData(oldData)
        }
    }


    override fun onCanceled(data: D?) {
        super.onCanceled(data)
        closeData(data)
    }

    /* (non-Javadoc)
         * @see android.support.v4.content.Loader#onStartLoading()
         */
    override fun onStartLoading() {
        if (mData != null) {
            deliverResult(mData)
        }
        if (takeContentChanged() || mData == null) {
            forceLoad()
        }
    }

    /* (non-Javadoc)
	 * @see android.support.v4.content.Loader#onStopLoading()
	 */
    override fun onStopLoading() {
        cancelLoad()

    }

    /* (non-Javadoc)
	 * @see android.support.v4.content.Loader#onReset()
	 */
    override fun onReset() {
        super.onReset()
        // Ensure the loader is stopped
        onStopLoading()
        closeData(mData)
        mData = null
    }

    private fun closeData(data: D?) {
        if (data != null && data is Closeable) {
            val c = data as Closeable?
            c?.close()
        }
    }


}
