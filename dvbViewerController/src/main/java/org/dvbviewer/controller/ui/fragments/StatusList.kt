/*
 * Copyright © 2012 dvbviewer-controller Project
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

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.status.StatusViewModel
import org.dvbviewer.controller.data.status.StatusViewModelFactory
import org.dvbviewer.controller.data.version.VersionRepository
import org.dvbviewer.controller.entities.DVBViewerPreferences
import org.dvbviewer.controller.entities.Status
import org.dvbviewer.controller.entities.Status.Folder
import org.dvbviewer.controller.entities.Status.StatusItem
import org.dvbviewer.controller.io.RecordingService
import org.dvbviewer.controller.io.ServerRequest
import org.dvbviewer.controller.io.data.Status1Handler
import org.dvbviewer.controller.io.data.Status2Handler
import org.dvbviewer.controller.io.data.StatusHandler
import org.dvbviewer.controller.ui.base.BaseListFragment
import org.dvbviewer.controller.utils.*

/**
 * The Class StatusList.
 *
 * @author RayBa
 * @date 05.07.2012
 */
class StatusList : BaseListFragment() {

    private lateinit var mAdapter: CategoryAdapter
    private lateinit var mRes: Resources
    private lateinit var versionRepository: VersionRepository
    private lateinit var prefs: DVBViewerPreferences
    private lateinit var statusViewModel: StatusViewModel
    private var mStatusAdapter: StatusAdapter? = null

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
        mStatusAdapter = StatusAdapter()
        mAdapter = CategoryAdapter(context)
        mRes = resources
        prefs = DVBViewerPreferences(activity!!.applicationContext)
        versionRepository = VersionRepository(activity!!.applicationContext, getDmsInterface())
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val statusObserver = Observer<ApiResponse<Status>> { response -> onStatusLoaded(response!!) }
        val statusViewModelFactory = StatusViewModelFactory(prefs, versionRepository)
        statusViewModel = ViewModelProvider(this, statusViewModelFactory)
                .get(StatusViewModel::class.java)
        statusViewModel.getStatus().observe(this, statusObserver)
        setListShown(isResumed)
    }

    private fun onStatusLoaded(apiResponse: ApiResponse<Status>) {
        if (apiResponse.status == org.dvbviewer.controller.data.Status.SUCCESS) {
            mStatusAdapter!!.items = apiResponse.data?.items
            val folderAdapter = FolderAdapter()
            folderAdapter.items = apiResponse.data?.folders
            mAdapter.addSection(getString(R.string.status), mStatusAdapter)
            mAdapter.addSection(getString(R.string.recording_folder), folderAdapter)
            mAdapter.notifyDataSetChanged()
        } else if (apiResponse.status == org.dvbviewer.controller.data.Status.ERROR) {
            catchException(TAG, apiResponse.e)
        }
        listAdapter = mAdapter
        setListShown(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.status, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuRefresh -> {
                refresh()
                true
            }
            else -> false
        }
    }

    private class StatusHolder {
        internal var title: TextView? = null
        internal var statusText: TextView? = null
        internal var free: TextView? = null
        internal var size: TextView? = null
    }


    inner class FolderAdapter

        : ArrayListAdapter<Folder>() {

        /*
         * (non-Javadoc)
         *
         * @see android.widget.ArrayAdapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: StatusHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_status, parent, false)
                holder = StatusHolder()
                holder.title = convertView!!.findViewById(R.id.title)
                holder.statusText = convertView.findViewById(R.id.statusText)
                holder.size = convertView.findViewById(R.id.size)
                holder.free = convertView.findViewById(R.id.free)
                convertView.tag = holder
            } else {
                holder = convertView.tag as StatusHolder
            }
            holder.title!!.text = mItems[position].path
            holder.statusText!!.visibility = View.GONE
            holder.size!!.visibility = View.VISIBLE
            holder.free!!.visibility = View.VISIBLE
            holder.size!!.text = mRes.getString(R.string.status_folder_total) + mRes.getString(R.string.common_colon) + FileUtils.byteToHumanString(mItems[position].size)
            holder.free!!.text = mRes.getString(R.string.status_folder_free) + mRes.getString(R.string.common_colon) + FileUtils.byteToHumanString(mItems[position].free)
            super.getViewTypeCount()
            return convertView
        }


    }

    /**
     * The Class StatusAdapter.
     *
     * @author RayBa
     * @date 05.07.2012
     */
    inner class StatusAdapter
    /**
     * Instantiates a new status adapter.
     *
     * @param context the context
     * @author RayBa
     * @date 05.07.2012
     */
        : ArrayListAdapter<StatusItem>() {

        /*
         * (non-Javadoc)
         *
         * @see android.widget.ArrayAdapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: StatusHolder
            if (convertView == null || convertView.tag !is StatusHolder) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_status, parent, false)
                holder = StatusHolder()
                holder.title = convertView!!.findViewById(R.id.title)
                holder.statusText = convertView.findViewById(R.id.statusText)
                holder.size = convertView.findViewById(R.id.size)
                holder.free = convertView.findViewById(R.id.free)
                convertView.tag = holder
            } else {
                holder = convertView.tag as StatusHolder
            }
            holder.title!!.visibility = View.VISIBLE
            holder.title!!.text = resources.getString(mItems[position].nameRessource)
            holder.statusText!!.visibility = View.VISIBLE
            when (mItems[position].nameRessource) {
                R.string.status_epg_update_running, R.string.status_standby_blocked -> holder.statusText!!.setText(if (NumberUtils.toInt(mItems[position].value) == 0) R.string.no else R.string.yes)
                R.string.status_epg_before, R.string.status_epg_after -> holder.statusText!!.text = mItems[position].value + " " + mRes.getString(R.string.minutes)
                R.string.status_timezone -> {
                    val timezone = NumberUtils.toInt(mItems[position].value) / 60
                    holder.statusText!!.text = mRes.getString(R.string.gmt) + (if (timezone > 0) " +" else "") + timezone
                }
                R.string.status_def_after_record -> holder.statusText!!.text = mRes.getStringArray(R.array.postRecoridngActions)[NumberUtils.toInt(mItems[position].value)]
                R.string.status_last_ui_access -> holder.statusText!!.text = DateUtils.secondsToReadableFormat(NumberUtils.toLong(mItems[position].value) * -1L)
                R.string.status_next_Rec, R.string.status_next_timer -> holder.statusText!!.text = DateUtils.secondsToReadableFormat(NumberUtils.toLong(mItems[position].value))
                else -> holder.statusText!!.text = mItems[position].value
            }
            holder.size!!.visibility = View.GONE
            holder.free!!.visibility = View.GONE
            return convertView
        }


    }

    /**
     * Refresh.
     *
     * @author RayBa
     * @date 05.07.2012
     */
    private fun refresh() {
        statusViewModel.getStatus(true)
        setListShown(false)
    }

    companion object {

        @Throws(Exception::class)
        fun getStatus(prefs: DVBViewerPreferences, version: String?): Status {
            val result: Status = requestStatusFromServer(ServerConsts.URL_STATUS2, Status2Handler())
            addVersionItem(result, version)
            val prefEditor = prefs.prefs.edit()
            prefEditor.putString(DVBViewerPreferences.KEY_RS_VERSION, version)
            val jsonClients = RecordingService.getDvbViewerTargets()
            if (StringUtils.isNotBlank(jsonClients)) {
                prefEditor.putString(DVBViewerPreferences.KEY_RS_CLIENTS, jsonClients)
            }
            val oldStatus = requestStatusFromServer(ServerConsts.URL_STATUS, Status1Handler())
            result!!.items.addAll(oldStatus.items)
            prefEditor.putInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, oldStatus.epgBefore)
            prefEditor.putInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, oldStatus.epgAfter)
            prefEditor.putInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, oldStatus.defAfterRecord)
            prefEditor.apply()
            return result
        }

        @Throws(Exception::class)
        private fun requestStatusFromServer(url: String, handler: StatusHandler): Status {
            try {
                val statusXml = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + url)
                statusXml.use {
                    return handler.parse(it)
                }
            } catch (e: java.lang.Exception) {
                Log.e(javaClass.simpleName, "Error getting status", e)
            }
            return Status()
        }

        private fun addVersionItem(status: Status?, version: String?): Status {
            var status = status
            if (status == null) {
                status = Status()
            }
            val versionItem = StatusItem()
            versionItem.nameRessource = R.string.status_server_version
            versionItem.value = version
            status.items.add(0, versionItem)
            return status
        }
    }

}
