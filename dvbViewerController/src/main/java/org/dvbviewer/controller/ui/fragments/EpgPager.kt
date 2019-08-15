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
import android.database.Cursor
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ProviderConsts.ChannelTbl
import org.dvbviewer.controller.entities.Channel
import org.dvbviewer.controller.ui.base.CursorPagerAdapter
import org.dvbviewer.controller.ui.fragments.ChannelEpg.EpgDateInfo
import org.dvbviewer.controller.ui.widget.ActionToolbar
import org.dvbviewer.controller.utils.DateUtils
import org.dvbviewer.controller.utils.UIUtils
import java.util.*

/**
 * The Class EpgPager.
 */
class EpgPager : Fragment(), LoaderCallbacks<Cursor>, Toolbar.OnMenuItemClickListener, OnPageChangeListener {
    private var mGroupId = AdapterView.INVALID_POSITION.toLong()
    private var chanIndex = AdapterView.INVALID_POSITION
    private var mGroupIndex = AdapterView.INVALID_POSITION
    private var mPager: ViewPager? = null
    private var mAdapter: PagerAdapter? = null
    private var mOnCHannelChanedListener: OnChannelScrolledListener? = null
    private var dateInfo: EpgDateInfo? = null


    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
	 */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChannelScrolledListener) {
            mOnCHannelChanedListener = context
        }
        if (context is EpgDateInfo) {
            dateInfo = context
        }
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = PagerAdapter(childFragmentManager)
        var showOptionsMenu = true
        if (arguments != null) {
            showOptionsMenu = !arguments!!.getBoolean(KEY_HIDE_OPTIONSMENU, false)
        }
        setHasOptionsMenu(showOptionsMenu)
        if (savedInstanceState == null) {
            if (arguments != null) {
                mGroupId = if (arguments!!.containsKey(ChannelPager.KEY_GROUP_ID)) arguments!!.getLong(ChannelPager.KEY_GROUP_ID, mGroupId) else mGroupId
                mGroupIndex = if (arguments!!.containsKey(ChannelPager.KEY_GROUP_INDEX)) arguments!!.getInt(ChannelPager.KEY_GROUP_INDEX, mGroupIndex) else mGroupIndex
                chanIndex = if (arguments!!.containsKey(ChannelList.KEY_CHANNEL_INDEX)) arguments!!.getInt(ChannelList.KEY_CHANNEL_INDEX, chanIndex) else chanIndex
            }
        } else {
            mGroupId = if (savedInstanceState.containsKey(ChannelPager.KEY_GROUP_ID)) savedInstanceState.getLong(ChannelPager.KEY_GROUP_ID, mGroupId) else mGroupId
            mGroupIndex = if (savedInstanceState.containsKey(ChannelPager.KEY_GROUP_INDEX)) savedInstanceState.getInt(ChannelPager.KEY_GROUP_INDEX, mGroupIndex) else mGroupIndex
            chanIndex = if (savedInstanceState.containsKey(ChannelList.KEY_CHANNEL_INDEX)) savedInstanceState.getInt(ChannelList.KEY_CHANNEL_INDEX, chanIndex) else chanIndex
        }
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPager!!.adapter = mAdapter
        mPager!!.pageMargin = UIUtils.dipToPixel(context!!, 25).toInt()
        mPager!!.currentItem = chanIndex
        mPager!!.addOnPageChangeListener(this)
        loaderManager.initLoader(0, null, this)
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPager = view.findViewById<View>(R.id.pager) as ViewPager
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.pager, container, false)
        val bootomBar = v.findViewById<ActionToolbar>(R.id.toolbar)
        if (bootomBar != null) {
            bootomBar.inflateMenu(R.menu.channel_epg_bottom_bar)
            bootomBar.setOnMenuItemClickListener(this)
        }
        return v
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(ChannelPager.KEY_GROUP_ID, mGroupId)
        outState.putInt(ChannelList.KEY_CHANNEL_INDEX, mPager!!.currentItem)
    }

    /*
		 * (non-Javadoc)
		 */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.channel_epg, menu)
    }

    /*
	 * (non-Javadoc)
	 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        val itemId = item.itemId
        when (itemId) {
            R.id.menuRefresh -> {
                dateInfo!!.epgDate = System.currentTimeMillis()
                refresh()
            }
            R.id.menuPrev -> {
                dateInfo!!.epgDate = DateUtils.substractDay(Date(dateInfo!!.epgDate)).time
                refreshOptionsMenu()
            }
            R.id.menuNext -> {
                dateInfo!!.epgDate = DateUtils.addDay(Date(dateInfo!!.epgDate)).time
                refreshOptionsMenu()
            }
            R.id.menuToday -> {
                dateInfo!!.epgDate = Date().time
                refreshOptionsMenu()
            }
            R.id.menuNow -> {
                dateInfo!!.epgDate = DateUtils.setCurrentTime(Date()).time
                refreshOptionsMenu()
            }
            R.id.menuEvening -> {
                dateInfo!!.epgDate = DateUtils.setEveningTime(Date(dateInfo!!.epgDate)).time
                refreshOptionsMenu()
            }
            else -> return false
        }

        return true
    }

    /*
	 * (non-Javadoc)
	 */
    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        val info = activity as EpgDateInfo?
        val itemId = menuItem.itemId
        when (itemId) {
            R.id.menuPrev -> info!!.epgDate = DateUtils.substractDay(Date(info.epgDate)).time
            R.id.menuNext -> info!!.epgDate = DateUtils.addDay(Date(info.epgDate)).time
            R.id.menuToday -> info!!.epgDate = Date().time
            R.id.menuNow -> info!!.epgDate = DateUtils.setCurrentTime(Date()).time
            R.id.menuEvening -> info!!.epgDate = DateUtils.setEveningTime(Date(info.epgDate)).time
            else -> return false
        }
        refreshOptionsMenu()
        return true
    }

    private fun refreshOptionsMenu() {
        refresh()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        chanIndex = position
        if (mOnCHannelChanedListener != null) {
            mOnCHannelChanedListener!!.channelChanged(mGroupId, position)
        }

    }

    override fun onPageScrollStateChanged(state: Int) {

    }


    /**
     * The Class PagerAdapter.
     */
    internal inner class PagerAdapter
    /**
     * Instantiates a new pager adapter.
     *
     * @param fm the fm
     */
    (fm: FragmentManager) : CursorPagerAdapter(fm) {

        /*
		 * (non-Javadoc)
		 *
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
        override fun getItem(position: Int): Fragment {
            cursor?.moveToPosition(position)
            val chan = cursor?.let { ChannelList.cursorToChannel(it) }
            val bundle = Bundle()
            bundle.putString(ChannelEpg.KEY_CHANNEL_NAME, chan?.name)
            chan?.epgID?.let { bundle.putLong(ChannelEpg.KEY_EPG_ID, it) }
            chan?.channelID?.let { bundle.putLong(ChannelEpg.KEY_CHANNEL_ID, it) }
            bundle.putString(ChannelEpg.KEY_CHANNEL_LOGO, chan?.logoUrl)
            bundle.putInt(ChannelEpg.KEY_CHANNEL_POS, chan?.position!!)
            bundle.putInt(ChannelEpg.KEY_FAV_POS, chan.favPosition!!)
            val fragment = fragmentManager!!.fragmentFactory.instantiate(javaClass.classLoader!!, ChannelEpg::class.java.name)
            fragment.arguments = bundle
            return fragment
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun getCount(): Int {
            return if (cursor == null || cursor!!.isClosed) {
                0
            } else cursor!!.count
        }
    }

    /**
     * Sets the position.
     *
     * @param position the new position
     */
    fun setPosition(position: Int) {
        chanIndex = position
        if (mPager != null) {
            mPager!!.setCurrentItem(chanIndex, false)
        }
    }

    override fun onCreateLoader(arg0: Int, arg1: Bundle?): Loader<Cursor> {
        val selection = StringBuilder(ChannelTbl.FLAGS + " & " + Channel.FLAG_ADDITIONAL_AUDIO + "== 0")
        if (mGroupId > 0) {
            selection.append(" and ")
            selection.append(ChannelTbl.GROUP_ID).append(" = ").append(mGroupId)
        }
        return CursorLoader(context!!, ChannelTbl.CONTENT_URI, null, selection.toString(), null, ChannelTbl.POSITION)
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
	 * .support.v4.content.Loader, java.lang.Object)
	 */
    override fun onLoadFinished(arg0: Loader<Cursor>, cursor: Cursor) {
        mAdapter!!.changeCursor(cursor)
        mAdapter!!.notifyDataSetChanged()
        mPager!!.setCurrentItem(chanIndex, false)
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
	 * .support.v4.content.Loader)
	 */
    override fun onLoaderReset(arg0: Loader<Cursor>) {
        arg0.reset()
    }

    fun refresh(groupId: Long, selectedPosition: Int) {
        if (mGroupId != groupId) {
            mGroupId = groupId
            chanIndex = selectedPosition
            refresh()
        }

    }

    private fun refresh() {
        resetLoader()
    }

    private fun resetLoader() {
        mAdapter = PagerAdapter(childFragmentManager)
        mPager?.adapter = mAdapter
        mAdapter?.notifyDataSetChanged()
        loaderManager.destroyLoader(0)
        loaderManager.restartLoader(0, arguments, this)
    }


    interface OnChannelScrolledListener {

        fun channelChanged(groupId: Long, channelIndex: Int)

    }

    companion object {

        private val KEY_HIDE_OPTIONSMENU = EpgPager::class.java.name + "KEY_HIDE_OPTIONSMENU"
    }

}
