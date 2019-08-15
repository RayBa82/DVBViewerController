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
package org.dvbviewer.controller.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.apache.commons.lang3.BooleanUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.Status.ERROR
import org.dvbviewer.controller.data.Status.SUCCESS
import org.dvbviewer.controller.data.media.MediaFile
import org.dvbviewer.controller.data.media.MediaRepository
import org.dvbviewer.controller.data.media.MediaViewModel
import org.dvbviewer.controller.data.media.MediaViewModelFactory
import org.dvbviewer.controller.data.version.VersionRepository
import org.dvbviewer.controller.data.version.VersionViewModel
import org.dvbviewer.controller.data.version.VersionViewModelFactory
import org.dvbviewer.controller.ui.adapter.MediaAdapter
import org.dvbviewer.controller.ui.base.RecyclerViewFragment
import java.text.MessageFormat

/**
 * Fragment for EPG details or Timer details.
 */
class MediaList : RecyclerViewFragment() {

    private var mAdapter: MediaAdapter? = null
    private var parentId = 0L
    private var mediaClickListener: MediaAdapter.OnMediaClickListener? = null

    private var versionViewModel: VersionViewModel? = null
    private var mediaViewModel: MediaViewModel? = null
    private var isFeatureSupported: Boolean = false


    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAdapter = MediaAdapter(context!!, mediaClickListener!!)
        if (arguments != null) {
            parentId = arguments!!.getLong(KEY_PARENT_ID, 1)
        } else {
            parentId = 1
        }
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity as AppCompatActivity?
        activity!!.setTitle(R.string.medias)
        recyclerView.adapter = mAdapter
        setListShown(false)
        val checkVersion = parentId == 1L
        initViewModels()
        val mediaObserver = Observer<ApiResponse<List<MediaFile>>> { response -> onMediaChanged(response!!) }
        val versionObserver = Observer<ApiResponse<Boolean>> { response -> onVersionChange(response!!, mediaObserver) }
        if (checkVersion) {
            setListShown(false)
            versionViewModel!!.isSupported(MINIMUM_VERSION).observe(this, versionObserver)
        } else {
            setListShown(false)
            mediaViewModel!!.getMedias(parentId).observe(this@MediaList, mediaObserver)
        }
    }

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        if (parentFragment != null && parentFragment is MediaAdapter.OnMediaClickListener) {
            mediaClickListener = parentFragment as MediaAdapter.OnMediaClickListener?
        } else if (activity is MediaAdapter.OnMediaClickListener) {
            mediaClickListener = activity
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.media_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setListShown(false)
        if (isFeatureSupported) {
            mediaViewModel!!.fetchMedias(parentId)
        } else {
            versionViewModel!!.fetchSupported(MINIMUM_VERSION)
        }
        return true
    }

    private fun initViewModels() {
        val repo = VersionRepository(context!!, getDmsInterface())
        val vFac = VersionViewModelFactory(activity!!.application, repo)
        versionViewModel = ViewModelProvider(this, vFac)
                .get(VersionViewModel::class.java)
        val mediaRepo = MediaRepository(getDmsInterface())
        val mediaFac = MediaViewModelFactory(activity!!.application, mediaRepo)
        mediaViewModel = ViewModelProvider(this, mediaFac)
                .get(MediaViewModel::class.java)
    }

    private fun onMediaChanged(response: ApiResponse<List<MediaFile>>) {
        if (response.status == SUCCESS) {
            mAdapter!!.setCursor(response.data!!)
            mAdapter!!.notifyDataSetChanged()
        } else if (response.status == ERROR) {
            catchException(MediaList::class.java.simpleName, response.e)
        }
        setListShown(true)
    }

    private fun onVersionChange(response: ApiResponse<Boolean>, mediaObserver: Observer<ApiResponse<List<MediaFile>>>) {
        if (response.status == SUCCESS) {
            if (BooleanUtils.isTrue(response.data)) {
                isFeatureSupported = true
                mediaViewModel!!.getMedias(parentId).observe(this@MediaList, mediaObserver)
            } else {
                val text = MessageFormat.format(getString(R.string.version_unsupported_text), MINIMUM_VERSION)
                infoText.text = text
                setListShown(true)
            }
        } else if (response.status == ERROR){
            catchException(javaClass.simpleName, response.e)
            setListShown(true)
        }
    }

    companion object {

        val KEY_PARENT_ID = MediaList::class.java.simpleName + "KEY_PARENT_ID"
        private const val MINIMUM_VERSION = "2.1.0.0"
    }

}
