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
import android.database.Cursor
import android.database.MatrixCursor
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
import androidx.cursoradapter.widget.CursorAdapter
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import com.squareup.picasso.Picasso
import org.apache.commons.collections4.CollectionUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ProviderConsts.EpgTbl
import org.dvbviewer.controller.entities.DVBViewerPreferences
import org.dvbviewer.controller.entities.EpgEntry
import org.dvbviewer.controller.entities.IEPG
import org.dvbviewer.controller.entities.Timer
import org.dvbviewer.controller.io.HTTPUtil
import org.dvbviewer.controller.io.ServerRequest
import org.dvbviewer.controller.io.ServerRequest.DVBViewerCommand
import org.dvbviewer.controller.io.ServerRequest.RecordingServiceGet
import org.dvbviewer.controller.io.UrlBuilderException
import org.dvbviewer.controller.io.data.EpgEntryHandler
import org.dvbviewer.controller.ui.base.BaseListFragment
import org.dvbviewer.controller.ui.base.EpgLoader
import org.dvbviewer.controller.ui.phone.IEpgDetailsActivity
import org.dvbviewer.controller.ui.phone.TimerDetailsActivity
import org.dvbviewer.controller.utils.DateUtils
import org.dvbviewer.controller.utils.ServerConsts
import org.dvbviewer.controller.utils.UIUtils
import java.text.MessageFormat
import java.util.*

/**
 * The Class ChannelEpg.
 *
 * @author RayBa
 */
class ChannelEpg : BaseListFragment(), LoaderCallbacks<Cursor>, OnItemClickListener, OnClickListener, PopupMenu.OnMenuItemClickListener {
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

    override val layoutRessource: Int
        get() = R.layout.fragment_channel_epg

    /* (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EpgDateInfo) {
            mDateInfo = context
        }
        if (context is IEpgDetailsActivity.OnIEPGClickListener) {
            clickListener = context
        }
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fillFromBundle(arguments!!)
        mAdapter = ChannelEPGAdapter(context)
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
        loaderManager.initLoader(0, savedInstanceState, this)
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
     * @see android.support.v4.app.Fragment#setUserVisibleHint(boolean)
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && isVisible) {
            refreshDate()
        }
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
     */
    override fun onCreateLoader(arg0: Int, arg1: Bundle?): Loader<Cursor> {

        return object : EpgLoader(context!!, mDateInfo!!) {

            override fun loadInBackground(): Cursor {
                val columnNames = arrayOf(EpgTbl._ID, EpgTbl.EPG_ID, EpgTbl.TITLE, EpgTbl.SUBTITLE, EpgTbl.DESC, EpgTbl.START, EpgTbl.END, EpgTbl.PDC, EpgTbl.EVENT_ID)
                try {
                    val now = Date(mDateInfo!!.epgDate)
                    val nowFloat = DateUtils.getFloatDate(now)
                    val tommorrow = DateUtils.addDay(now)
                    val tommorrowFloat = DateUtils.getFloatDate(tommorrow)
                    val builder = buildBaseEpgUrl()
                            .addQueryParameter("channel", epgId.toString())
                            .addQueryParameter("start", nowFloat.toString())
                            .addQueryParameter("end", tommorrowFloat.toString())
                    val handler = EpgEntryHandler()
                    val stream = ServerRequest.getInputStream(builder.build().toString())
                    stream.use {
                        val result = handler.parse(it)
                        if (CollectionUtils.isNotEmpty(result)) {
                            val cursor = MatrixCursor(columnNames)
                            for (entry in result) {
                                cursor.addRow(arrayOf<Any>(entry.id, entry.epgID, entry.title, entry.subTitle, entry.description, entry.start.time, entry.end.time, entry.pdc, entry.eventId))
                            }
                            return cursor
                        }
                    }
                } catch (e: Exception) {
                    catchException(javaClass.simpleName, e)
                }
                return MatrixCursor(columnNames)
            }
        }
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
     */
    override fun onLoadFinished(arg0: Loader<Cursor>, cursor: Cursor) {
        mAdapter!!.changeCursor(cursor)
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
        val c = mAdapter!!.cursor
        c.moveToPosition(position)
        val entry = cursorToEpgEntry(c)
        if (clickListener != null) {
            clickListener!!.onIEPGClick(entry)
            return
        }
        val i = Intent(context, IEpgDetailsActivity::class.java)
        i.putExtra(IEPG::class.java.simpleName, entry)
        startActivity(i)
    }

    /**
     * Reads the current cursorposition to an EpgEntry.
     *
     * @param c the c
     * @return the iEPG©
     */
    private fun cursorToEpgEntry(c: Cursor): IEPG {
        val entry = EpgEntry()
        entry.channel = channel
        entry.description = c.getString(c.getColumnIndex(EpgTbl.DESC))
        entry.end = Date(c.getLong(c.getColumnIndex(EpgTbl.END)))
        entry.epgID = epgId
        entry.start = Date(c.getLong(c.getColumnIndex(EpgTbl.START)))
        entry.subTitle = c.getString(c.getColumnIndex(EpgTbl.SUBTITLE))
        entry.title = c.getString(c.getColumnIndex(EpgTbl.TITLE))
        return entry
    }

    /**
     * The Class ChannelEPGAdapter.
     *
     * @author RayBa
     */
    inner class ChannelEPGAdapter
    /**
     * Instantiates a new channel epg adapter.
     *
     * @param context the context
     */
    (context: Context?) : CursorAdapter(context, null, FLAG_REGISTER_CONTENT_OBSERVER) {

        /* (non-Javadoc)
         * @see android.support.v4.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
         */
        override fun bindView(view: View, context: Context, c: Cursor) {
            val holder = view.tag as ViewHolder
            holder.contextMenu!!.tag = c.position
            val millis = c.getLong(c.getColumnIndex(EpgTbl.START))
            val flags = DateUtils.FORMAT_SHOW_TIME
            val date = DateUtils.formatDateTime(context, millis, flags)
            holder.startTime!!.text = date
            holder.title!!.text = c.getString(c.getColumnIndex(EpgTbl.TITLE))
            val subTitle = c.getString(c.getColumnIndex(EpgTbl.SUBTITLE))
            val desc = c.getString(c.getColumnIndex(EpgTbl.DESC))
            holder.description!!.text = if (TextUtils.isEmpty(subTitle)) desc else subTitle
            holder.description!!.visibility = if (TextUtils.isEmpty(holder.description!!.text)) View.GONE else View.VISIBLE
        }

        /* (non-Javadoc)
         * @see android.support.v4.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
         */
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            val view = LayoutInflater.from(context).inflate(R.layout.list_row_epg, parent, false)
            val holder = ViewHolder()
            holder.startTime = view.findViewById(R.id.startTime)
            holder.title = view.findViewById(R.id.title)
            holder.description = view.findViewById(R.id.description)
            holder.contextMenu = view.findViewById(R.id.contextMenu)
            holder.contextMenu!!.setOnClickListener(this@ChannelEpg)
            view.tag = holder
            return view
        }

    }

    /**
     * Refreshs the data
     *
     */
    fun refresh() {
        setListShown(false)
        loaderManager.restartLoader(0, arguments, this).forceLoad()
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
        val c = mAdapter!!.cursor
        c.moveToPosition(selectedPosition)
        val pos = selectedPosition
        val timer: Timer
        when (item.itemId) {
            R.id.menuRecord -> {
                timer = cursorToTimer(c)
                val url = TimerDetails.buildTimerUrl(timer)
                val rsGet = RecordingServiceGet(url)
                val executionThread = Thread(rsGet)
                executionThread.start()
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
                c.moveToPosition(pos)
                val entry = cursorToEpgEntry(c)
                details.putExtra(IEPG::class.java.simpleName, entry)
                startActivity(details)
                return true
            }
            R.id.menuSwitch -> {
                val prefs = DVBViewerPreferences(context!!)
                val cid = ":$channelId"
                val switchRequest = MessageFormat.format(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_SWITCH_COMMAND, prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT), cid)
                val command = DVBViewerCommand(context, switchRequest)
                val exexuterTHread = Thread(command)
                exexuterTHread.start()
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


    /* (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
     */
    override fun onLoaderReset(arg0: Loader<Cursor>) {}

    /**
     * Cursor to timer.
     *
     * @param c the c
     * @return the timer©
     */
    private fun cursorToTimer(c: Cursor): Timer {
        val epgTitle = if (!c.isNull(c.getColumnIndex(EpgTbl.TITLE))) c.getString(c.getColumnIndex(EpgTbl.TITLE)) else channel
        val epgStart = c.getLong(c.getColumnIndex(EpgTbl.START))
        val epgEnd = c.getLong(c.getColumnIndex(EpgTbl.END))
        val prefs = DVBViewerPreferences(context!!)
        val epgBefore = prefs.prefs.getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, DVBViewerPreferences.DEFAULT_TIMER_TIME_BEFORE)
        val epgAfter = prefs.prefs.getInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, DVBViewerPreferences.DEFAULT_TIMER_TIME_AFTER)
        val start = if (epgStart > 0) Date(epgStart) else Date()
        val end = if (epgEnd > 0) Date(epgEnd) else Date()
        val eventId = c.getString(c.getColumnIndex(EpgTbl.EVENT_ID))
        val pdc = c.getString(c.getColumnIndex(EpgTbl.PDC))
        val timer = Timer()
        timer.title = epgTitle
        timer.channelId = channelId
        timer.channelName = channel
        timer.start = start
        timer.end = end
        timer.pre = epgBefore
        timer.post = epgAfter
        timer.eventId = eventId
        timer.pdc = pdc
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

        @Throws(UrlBuilderException::class)
        fun buildBaseEpgUrl(): HTTPUtil.UrlBuilder {
            return HTTPUtil.getUrlBuilder(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_EPG)
                    .addQueryParameter("utf8", "1")
                    .addQueryParameter("lvl", "2")
        }
    }

}
