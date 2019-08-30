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

import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.entities.IEPG
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


    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        epg = arguments!!.getParcelable(IEPG::class.java.simpleName)
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (epg != null) {
            var dateString = DateUtils.getDateInLocalFormat(epg!!.start)
            if (DateUtils.isToday(epg!!.start.time)) {
                dateString = resources.getString(R.string.today)
            } else if (DateUtils.isTomorrow(epg!!.start.time)) {
                dateString = resources.getString(R.string.tomorrow)
            }
            val start = DateUtils.getTimeInLocalFormat(context, epg!!.start)
            val end = DateUtils.getTimeInLocalFormat(context, epg!!.end)
            date!!.text = "$dateString  $start - $end"
            channel!!.text = epg!!.channel
            title!!.text = epg!!.title
            if (TextUtils.isEmpty(epg!!.subTitle)) {
                subTitle!!.visibility = View.GONE
            } else {
                subTitle!!.text = epg!!.subTitle
            }
            desc!!.text = epg!!.description
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
