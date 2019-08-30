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
package org.dvbviewer.controller.ui.phone

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.data.media.MediaFile
import org.dvbviewer.controller.ui.adapter.MediaAdapter
import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity
import org.dvbviewer.controller.ui.fragments.MediaList
import org.dvbviewer.controller.ui.fragments.StreamConfig
import org.dvbviewer.controller.utils.*

/**
 * The Class TimerlistActivity.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class MedialistActivity : BaseSinglePaneActivity(), MediaAdapter.OnMediaClickListener {

    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDisplayHomeAsUpEnabled(true)
    }

    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> false
        }
    }

    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreatePane()
	 */
    override fun onCreatePane(): Fragment {
        val mediaList = MediaList()
        mediaList.arguments = intentToFragmentArguments(intent)
        return mediaList
    }

    override fun onMediaClick(mediaFile: MediaFile) {
        if (mediaFile.dirId > 0) {
            val b = Bundle()
            b.putLong(MediaList.KEY_PARENT_ID, mediaFile.dirId)
            val mediaList = MediaList()
            mediaList.arguments = b
            changeFragment(R.id.root_container, mediaList)
        } else {
            val arguments = Bundle()
            arguments.putLong(StreamConfig.EXTRA_FILE_ID, mediaFile.id!!)
            arguments.putParcelable(StreamConfig.EXTRA_FILE_TYPE, FileType.VIDEO)
            arguments.putInt(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig)
            arguments.putString(StreamConfig.EXTRA_TITLE, mediaFile.name)
            val streamConfig = Intent(this, StreamConfigActivity::class.java)
            streamConfig.putExtras(arguments)
            startActivity(streamConfig)
        }
    }

    override fun onMediaStreamClick(mediaFile: MediaFile) {
        val videoIntent = StreamUtils.buildQuickUrl(this, mediaFile.id!!, mediaFile.name, FileType.VIDEO)
        startActivity(videoIntent)
        val prefs = DVBViewerPreferences(this).streamPrefs
        val direct = prefs.getBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true)
        val bundle = Bundle()
        bundle.putString(PARAM_START, START_QUICK)
        bundle.putString(PARAM_TYPE, if (direct) TYPE_DIRECT else TYPE_TRANSCODED)
        bundle.putString(PARAM_NAME, mediaFile.name)
        mFirebaseAnalytics?.logEvent(EVENT_STREAM_MEDIA, bundle)
    }

    override fun onMediaContextClick(mediaFile: MediaFile) {

    }

}