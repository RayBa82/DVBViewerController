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

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.api.APIClient
import org.dvbviewer.controller.data.api.ApiResponse
import org.dvbviewer.controller.data.api.ApiStatus
import org.dvbviewer.controller.data.api.DMSInterface
import org.dvbviewer.controller.data.entities.IEPG
import org.dvbviewer.controller.data.entities.Recording
import org.dvbviewer.controller.data.recording.RecordingRepository
import org.dvbviewer.controller.data.recording.RecordingViewModel
import org.dvbviewer.controller.data.recording.RecordingViewModelFactory
import org.dvbviewer.controller.ui.base.BaseDialogFragment
import org.dvbviewer.controller.utils.DateUtils

/**
 * Fragment for EPG details or Timer details.
 */
class EPGDetails : BaseDialogFragment() {


    /**
     * Gets the epg.
     *
     * @return the epg
     */
    var epg: IEPG? = null
        internal set
    private var channel: TextView? = null
    private var date: TextView? = null
    private var title: TextView? = null
    private var subTitle: TextView? = null
    private var desc: TextView? = null

    private lateinit var recordingRepo: RecordingRepository
    private lateinit var viewModel: RecordingViewModel


    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        epg = arguments!!.getParcelable(IEPG::class.java.simpleName)
        recordingRepo = RecordingRepository(APIClient.client.create(DMSInterface::class.java))
        val vFac = RecordingViewModelFactory(recordingRepo)
        viewModel = ViewModelProvider(this, vFac)
                .get(RecordingViewModel::class.java)
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        epg?.let {
            var dateString = DateUtils.getDateInLocalFormat(it.start)
            if (DateUtils.isToday(it.start.time)) {
                dateString = resources.getString(R.string.today)
            } else if (DateUtils.isTomorrow(it.start.time)) {
                dateString = resources.getString(R.string.tomorrow)
            }
            val start = DateUtils.getTimeInLocalFormat(context, it.start)
            val end = DateUtils.getTimeInLocalFormat(context, it.end)
            date?.text = "$dateString  $start - $end"
            channel?.text = it.channel
            title?.text = it.title
            if (TextUtils.isEmpty(it.subTitle)) {
                subTitle?.visibility = View.GONE
            } else {
                subTitle?.text = it.subTitle
            }
            desc?.text = it.description
            if(it is Recording) {
                val recordingListObserver = Observer<ApiResponse<Recording>> { response -> onDetailsLoaded(response) }
                viewModel.getRecordingDetail(it.id).observe(viewLifecycleOwner, recordingListObserver)
            }
        }
    }

    private fun onDetailsLoaded(observable: ApiResponse<Recording>?) {
        if(observable?.status == ApiStatus.SUCCESS) {
            desc?.text = observable.data?.description
        } else if(observable?.status == ApiStatus.ERROR) {
            observable.e?.let {
                catchException(it)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(R.string.details)
        return dialog
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = activity!!.layoutInflater.inflate(R.layout.fragment_epg_details, container, false)
        channel = v.findViewById<View>(R.id.channel) as TextView
        date = v.findViewById<View>(R.id.date) as TextView
        title = v.findViewById<View>(R.id.title) as TextView
        subTitle = v.findViewById<View>(R.id.subTitle) as TextView
        desc = v.findViewById<View>(R.id.desc) as TextView
        return v
    }


}
