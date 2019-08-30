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

import androidx.loader.content.CursorLoader

import org.dvbviewer.controller.ui.fragments.ChannelEpg.EpgDateInfo

/**
 * The Class EpgLoader.
 *
 * @author RayBa
 */
abstract class EpgLoader
/**
 * Instantiates a new epg loader.
 *
 * @param context the context
 * @param dateInfo the date info
 */
(context: Context, private val mDateInfo: EpgDateInfo) : CursorLoader(context) {
    private var mEpgDate: Long = 0

    /* (non-Javadoc)
	 * @see android.support.v4.content.AsyncTaskLoader#onForceLoad()
	 */
    override fun onForceLoad() {
        super.onForceLoad()
        mEpgDate = mDateInfo.epgDate
    }

    /* (non-Javadoc)
	 * @see android.support.v4.content.Loader#takeContentChanged()
	 */
    override fun takeContentChanged(): Boolean {

        return super.takeContentChanged() || mEpgDate != mDateInfo.epgDate
    }

}
