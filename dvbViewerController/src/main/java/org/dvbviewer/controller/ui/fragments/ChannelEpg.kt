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
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.data.entities.EpgEntry
import org.dvbviewer.controller.data.entities.IEPG
import org.dvbviewer.controller.data.entities.Timer
import org.dvbviewer.controller.data.epg.ChannelEpgViewModel
import org.dvbviewer.controller.data.epg.EPGRepository
import org.dvbviewer.controller.data.epg.EpgViewModelFactory
import org.dvbviewer.controller.data.remote.RemoteRepository
import org.dvbviewer.controller.data.timer.TimerRepository
import org.dvbviewer.controller.ui.base.BaseListFragment
import org.dvbviewer.controller.ui.phone.IEpgDetailsActivity
import org.dvbviewer.controller.ui.phone.TimerDetailsActivity
import org.dvbviewer.controller.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * The Class ChannelEpg.
 *
 * @author RayBa
 */
class ChannelEpg : BaseListFragment(), OnItemClickListener, OnClickListener, PopupMenu.OnMenuItemClickListener {
    private var mAdapter: ChannelEPGAdapter? = null
    private var clickListener: IEpgDetailsActivity.OnIEPGClickListener? = null
    private var channel: String? = null
    private var channelId: Long = 0
    private var epgId: Long = 0
    private var logoUrl: String? = null
    private var channelPos: Int = 0
    private var favPos: Int = 0
    private var selectedPosition: Int = 0
    private var channelLogo: ImageView? = null
    private var channelName: TextView? = null
    private var dayIndicator: TextView? = null
    private var mDateInfo: EpgDateInfo? = null
    private var lastRefresh: Date? = null
    private var header: View? = null
    private lateinit var timerRepository: TimerRepository
    private lateinit var remoteRepository: RemoteRepository
    private lateinit var epgRepository: EPGRepository
    private lateinit var epgViewModel: ChannelEpgViewModel

    override val layoutRessource: Int
        get() = R.layout.fragment_channel_epg

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EpgDateInfo) {
            mDateInfo = context
        }
        if (context is IEpgDetailsActivity.OnIEPGClickListener) {
            clickListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timerRepository = TimerRepository(getDmsInterface())
        remoteRepository = RemoteRepository(getDmsInterface())
        epgRepository = EPGRepository(getDmsInterface())
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fillFromBundle(arguments!!)
        mAdapter = ChannelEPGAdapter()
        listAdapter = mAdapter
        setListShown(false)
        listView!!.onItemClickListener = this
        if (header != null && channel != null) {
            channelLogo!!.setImageBitmap(null)
            val url = ServerConsts.REC_SERVICE_URL + "/" + logoUrl
            Picasso.get()
                    .load(url)
                    .into(channelLogo)
            channelName!!.text = channel
            if (DateUtils.isToday(mDateInfo!!.epgDate)) {
                dayIndicator!!.setText(R.string.today)
            } else if (DateUtils.isTomorrow(mDateInfo!!.epgDate)) {
                dayIndicator!!.setText(R.string.tomorrow)
            } else {
                dayIndicator!!.text = DateUtils.formatDateTime(activity, mDateInfo!!.epgDate, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY)
            }
        }

        setEmptyText(resources.getString(R.string.no_epg))
        val epgObserver = Observer<List<EpgEntry>> { response -> onEpgChanged(response!!) }
        val epgViewModelFactory = EpgViewModelFactory(epgRepository)
        epgViewModel = ViewModelProvider(this, epgViewModelFactory)
                .get(ChannelEpgViewModel::class.java)
        val now = Date(mDateInfo!!.epgDate)
        val tommorrow = DateUtils.addDay(now)
        epgViewModel.getChannelEPG(epgId, now, tommorrow).observe(this, epgObserver)
    }

    private fun onEpgChanged(response: List<EpgEntry>) {
        mAdapter = ChannelEPGAdapter()
        listView?.adapter = mAdapter
        mAdapter!!.items = response
        mAdapter!!.notifyDataSetChanged()
        setSelection(0)
        val dateText: String
        if (DateUtils.isToday(mDateInfo!!.epgDate)) {
            dateText = getString(R.string.today)
        } else if (DateUtils.isTomorrow(mDateInfo!!.epgDate)) {
            dateText = getString(R.string.tomorrow)
        } else {
            dateText = DateUtils.formatDateTime(context, mDateInfo!!.epgDate, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY)
        }
        if (header != null) {
            if (DateUtils.isToday(mDateInfo!!.epgDate)) {
                dayIndicator!!.setText(R.string.today)
            } else if (DateUtils.isTomorrow(mDateInfo!!.epgDate)) {
                dayIndicator!!.setText(R.string.tomorrow)
            } else {
                dayIndicator!!.text = DateUtils.formatDateTime(activity, mDateInfo!!.epgDate, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY)
            }
            dayIndicator!!.text = dateText
        } else {
            val activity = activity as AppCompatActivity?
            activity!!.supportActionBar!!.subtitle = dateText
        }
        lastRefresh = Date(mDateInfo!!.epgDate)
        setListShown(true)
    }

    private fun fillFromBundle(savedInstanceState: Bundle) {
        channel = savedInstanceState.getString(KEY_CHANNEL_NAME)
        channelId = savedInstanceState.getLong(KEY_CHANNEL_ID)
        epgId = savedInstanceState.getLong(KEY_EPG_ID)
        logoUrl = savedInstanceState.getString(KEY_CHANNEL_LOGO)
        channelPos = savedInstanceState.getInt(KEY_CHANNEL_POS)
        favPos = savedInstanceState.getInt(KEY_FAV_POS)
    }

    /* (non-Javadoc)
     * @see org.dvbviewer.controller.ui.base.BaseListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        if (v != null) {
            header = v.findViewById(R.id.epg_header)
            channelLogo = v.findViewById(R.id.icon)
            channelName = v.findViewById(R.id.title)
            dayIndicator = v.findViewById(R.id.dayIndicator)
        }
        return v
    }

    /**
     * The Class ViewHolder.
     *
     * @author RayBa
     */
    private class ViewHolder {
        internal var startTime: TextView? = null
        internal var title: TextView? = null
        internal var description: TextView? = null
        internal var contextMenu: ImageView? = null
    }

    /* (non-Javadoc)
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val entry = mAdapter!!.getItem(position)
        if (clickListener != null) {
            clickListener!!.onIEPGClick(entry)
            return
        }
        val i = Intent(context, IEpgDetailsActivity::class.java)
        i.putExtra(IEPG::class.java.simpleName, entry)
        startActivity(i)
    }

    /**
     * The Class ChannelEPGAdapter.
     *
     * @author RayBa
     */
    inner class ChannelEPGAdapter
    /**
     * Instantiates a new channel epg adapter.
     */
        : ArrayListAdapter<EpgEntry>() {


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            val holder: ViewHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_row_epg, parent, false)
                holder = ViewHolder()
                holder.startTime = convertView.findViewById(R.id.startTime)
                holder.title = convertView.findViewById(R.id.title)
                holder.description = convertView.findViewById(R.id.description)
                holder.contextMenu = convertView.findViewById(R.id.contextMenu)
                holder.contextMenu!!.setOnClickListener(this@ChannelEpg)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            val epgEntry = getItem(position)
            holder.contextMenu!!.tag = position
            val flags = DateUtils.FORMAT_SHOW_TIME
            val date = DateUtils.formatDateTime(context, epgEntry.start.time, flags)
            holder.startTime!!.text = date
            holder.title!!.text = epgEntry.title
            val subTitle = epgEntry.subTitle
            val desc = epgEntry.description
            holder.description!!.text = if (TextUtils.isEmpty(subTitle)) desc else subTitle
            holder.description!!.visibility = if (TextUtils.isEmpty(holder.description!!.text)) View.GONE else View.VISIBLE
            return convertView!!
        }

    }

    /**
     * Refreshs the data
     *
     */
    fun refresh() {
        setListShown(false)
        val start = Date(mDateInfo!!.epgDate)
        val end = DateUtils.addDay(start)
        epgViewModel.getChannelEPG(epgId, start, end, true)
    }

    /**
     * Refresh date.
     *
     */
    private fun refreshDate() {
        if (lastRefresh != null && lastRefresh!!.time != mDateInfo!!.epgDate) {
            refresh()
        }
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CHANNEL_NAME, channel)
        outState.putLong(KEY_CHANNEL_ID, channelId)
        outState.putLong(KEY_EPG_ID, epgId)
        outState.putString(KEY_CHANNEL_LOGO, logoUrl)
        outState.putInt(KEY_CHANNEL_POS, channelPos)
        outState.putInt(KEY_FAV_POS, favPos)
        outState.putLong(KEY_EPG_DAY, mDateInfo!!.epgDate)
    }

    /* (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.channel_epg, menu)
        menu.findItem(R.id.menuPrev).isEnabled = !DateUtils.isToday(mDateInfo!!.epgDate)
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    override fun onClick(v: View) {
        when (v.id) {
            R.id.contextMenu -> {
                selectedPosition = v.tag as Int
                val popup = PopupMenu(context!!, v)
                popup.menuInflater.inflate(R.menu.context_menu_epg, popup.menu)
                popup.setOnMenuItemClickListener(this)
                popup.show()
            }
            else -> {
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val c = mAdapter!!.getItem(selectedPosition)
        val timer: Timer
        when (item.itemId) {
            R.id.menuRecord -> {
                timer = cursorToTimer(c)
                val call = timerRepository.saveTimer(timer)
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        sendMessage(R.string.timer_saved)
                        logEvent(EVENT_TIMER_CREATED)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        sendMessage(R.string.error_common)
                    }
                })
                return true
            }
            R.id.menuTimer -> {
                timer = cursorToTimer(c)
                if (UIUtils.isTablet(context!!)) {
                    val timerdetails = TimerDetails.newInstance()
                    val args = TimerDetails.buildBundle(timer)
                    timerdetails.arguments = args
                    timerdetails.show(activity!!.supportFragmentManager, TimerDetails::class.java.name)
                } else {
                    val timerIntent = Intent(context, TimerDetailsActivity::class.java)
                    val extras = TimerDetails.buildBundle(timer)
                    timerIntent.putExtras(extras)
                    startActivity(timerIntent)
                }
                return true
            }
            R.id.menuDetails -> {
                val details = Intent(context, IEpgDetailsActivity::class.java)
                details.putExtra(IEPG::class.java.simpleName, c)
                startActivity(details)
                return true
            }
            R.id.menuSwitch -> {
                val prefs = DVBViewerPreferences(context!!)
                val target = prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT)
                remoteRepository.switchChannel(target, channelId.toString()).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        sendMessage(R.string.channel_switched)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        sendMessage(R.string.error_common)
                    }
                })
                return true
            }
            else -> {
            }
        }
        return false
    }


    /**
     * The Interface EpgDateInfo.
     *
     * @author RayBa
     */
    interface EpgDateInfo {

        var epgDate: Long

    }


    /**
     * Cursor to timer.
     *
     * @param c the c
     * @return the timer©
     */
    private fun cursorToTimer(c: EpgEntry): Timer {
        val epgTitle = if (StringUtils.isNotBlank(c.title)) c.title else channel
        val prefs = DVBViewerPreferences(context!!)
        val epgBefore = prefs.prefs.getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, DVBViewerPreferences.DEFAULT_TIMER_TIME_BEFORE)
        val epgAfter = prefs.prefs.getInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, DVBViewerPreferences.DEFAULT_TIMER_TIME_AFTER)
        val timer = Timer()
        timer.title = epgTitle
        timer.channelId = channelId
        timer.channelName = channel
        timer.start = c.start
        timer.end = c.end
        timer.pre = epgBefore
        timer.post = epgAfter
        timer.eventId = c.eventId
        timer.pdc = c.pdc
        timer.timerAction = prefs.prefs.getInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, 0)
        return timer
    }

    companion object {

        val KEY_CHANNEL_NAME = ChannelEpg::class.java.name + "KEY_CHANNEL_NAME"
        val KEY_CHANNEL_ID = ChannelEpg::class.java.name + "KEY_CHANNEL_ID"
        val KEY_CHANNEL_LOGO = ChannelEpg::class.java.name + "KEY_CHANNEL_LOGO"
        val KEY_CHANNEL_POS = ChannelEpg::class.java.name + "KEY_CHANNEL_POS"
        val KEY_FAV_POS = ChannelEpg::class.java.name + "KEY_FAV_POS"
        val KEY_EPG_ID = ChannelEpg::class.java.name + "KEY_EPG_ID"
        val KEY_EPG_DAY = ChannelEpg::class.java.name + "EPG_DAY"

    }

}
