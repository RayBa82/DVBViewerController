/*
 * Copyright © 2015 dvbviewer-controller Project
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

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.PagerTitleStrip
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.DbHelper
import org.dvbviewer.controller.data.channel.ChannelGroupViewModel
import org.dvbviewer.controller.data.channel.ChannelGroupViewModelFactory
import org.dvbviewer.controller.data.channel.ChannelRepository
import org.dvbviewer.controller.data.entities.ChannelGroup
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.data.version.VersionRepository
import org.dvbviewer.controller.ui.base.BaseFragment
import org.dvbviewer.controller.utils.Config
import org.dvbviewer.controller.utils.UIUtils
import java.util.*


/**
 * The Class EpgPager.
 *
 * @author RayBa82
 */
class ChannelPager : BaseFragment(), OnPageChangeListener {
    private var mGroupIndex = AdapterView.INVALID_POSITION
    @SuppressLint("UseSparseArrays")
    private var index = HashMap<Int, Int>()
    private var showFavs: Boolean = false
    private var showNowPlaying: Boolean = false
    private var showNowPlayingWifi: Boolean = false
    private var refreshGroupType: Boolean = false
    private var hideFavSwitch = false
    private var mGroupCHangedListener: OnGroupChangedListener? = null
    private var mOnGroupTypeCHangedListener: OnGroupTypeChangedListener? = null
    private var mNetworkInfo: NetworkInfo? = null
    private lateinit var mProgress: View
    private lateinit var mPager: ViewPager
    private lateinit var mAdapter: ChannelPagerAdapter
    private lateinit var mPagerIndicator: PagerTitleStrip
    private lateinit var prefs: DVBViewerPreferences
    private lateinit var versionRepository: VersionRepository
    private lateinit var channelRepository: ChannelRepository
    private lateinit var mDbHelper: DbHelper
    private lateinit var groupViewModel: ChannelGroupViewModel

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
	 */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnGroupTypeChangedListener) {
            mOnGroupTypeCHangedListener = context
        }
        if (context is OnGroupChangedListener) {
            mGroupCHangedListener = context
        }
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        prefs = DVBViewerPreferences(activity?.applicationContext)
        mDbHelper = DbHelper(activity?.applicationContext)
        versionRepository = VersionRepository(activity!!.applicationContext, getDmsInterface())
        channelRepository = ChannelRepository(getDmsInterface(), mDbHelper)
        mAdapter = ChannelPagerAdapter(childFragmentManager)
        val connManager = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        mNetworkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        showFavs = prefs.prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false)
        showNowPlaying = prefs.prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_NOW_PLAYING, true)
        showNowPlayingWifi = prefs.prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_NOW_PLAYING_WIFI_ONLY, true)
        if (savedInstanceState == null && arguments != null) {
            hideFavSwitch = arguments!!.getBoolean(KEY_HIDE_FAV_SWITCH, false)
            if (arguments!!.containsKey(KEY_GROUP_INDEX)) {
                mGroupIndex = arguments!!.getInt(KEY_GROUP_INDEX)
            }
            val initialChanIndex = arguments!!.getInt(ChannelList.KEY_CHANNEL_INDEX)
            index[mGroupIndex] = initialChanIndex
        } else if (savedInstanceState != null) {
            mGroupIndex = savedInstanceState.getInt(KEY_GROUP_INDEX)
            index = (savedInstanceState.getSerializable(KEY_INDEX) as HashMap<Int, Int>?)!!
        }
    }

    fun setPosition(position: Int) {
        mGroupIndex = position
        mPager.setCurrentItem(mGroupIndex, false)
    }

    override fun onDestroy() {
        mDbHelper.close()
        super.onDestroy()
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.setTitle(if (showFavs) R.string.favourites else R.string.channelList)
        mPager.adapter = mAdapter
        mPager.pageMargin = UIUtils.dipToPixel(context!!, 25).toInt()
        mPager.addOnPageChangeListener(this)

        if (savedInstanceState == null) {
            /**
             * Prüfung ob das EPG in der Senderliste angezeigt werden soll.
             */
            if (!Config.CHANNELS_SYNCED) {
            } else if (showNowPlaying && !showNowPlayingWifi || showNowPlaying && mNetworkInfo!!.isConnected) {
            }
        }
        mPager.currentItem = mGroupIndex
        val groupObserver = Observer<List<ChannelGroup>> { response -> onGroupChanged(response!!) }
        val channelGroupViewModelFactory = ChannelGroupViewModelFactory(prefs, channelRepository)
        groupViewModel = ViewModelProvider(this, channelGroupViewModelFactory)
                .get(ChannelGroupViewModel::class.java)
        groupViewModel.getGroupList(showFavs).observe(this, groupObserver)
        showProgress(savedInstanceState == null)
    }


    fun onGroupChanged(groupList: List<ChannelGroup>) {
        mAdapter.groups = groupList
        mAdapter.notifyDataSetChanged()
        mPager.setCurrentItem(mGroupIndex, false)
        // mPager.setPageTransformer(true, new DepthPageTransformer());
        activity?.invalidateOptionsMenu()
        showProgress(false)
        if (refreshGroupType) {
            mOnGroupTypeCHangedListener?.groupTypeChanged(if (showFavs) ChannelGroup.TYPE_FAV else ChannelGroup.TYPE_CHAN)
        }
        refreshGroupType = false
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pager, container, false)
        mProgress = view.findViewById(android.R.id.progress)
        mPager = view.findViewById(R.id.pager)
        mPagerIndicator = view.findViewById(R.id.titles)
        val c = view.findViewById<View>(R.id.bottom_container)
        if (c != null) {
            c.visibility = View.GONE
        }
        return view
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.actionbarsherlock.app.SherlockListFragment#onCreateOptionsMenu(android
	 * .view.Menu, android.view.MenuInflater)
	 */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.channel_pager, menu)
        if (!hideFavSwitch) {
            menu.findItem(R.id.menuChannelList).isVisible = showFavs
            menu.findItem(R.id.menuFavourties).isVisible = !showFavs
        } else {
            menu.findItem(R.id.menuChannelList).isVisible = false
            menu.findItem(R.id.menuFavourties).isVisible = false
        }
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.actionbarsherlock.app.SherlockListFragment#onOptionsItemSelected(
	 * android.view.MenuItem)
	 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.menuRefresh -> {
                groupViewModel.fetchEpg()
                return true
            }
            R.id.menuSyncChannels -> {
                mPager.adapter = null
                mAdapter.notifyDataSetChanged()
                mAdapter = ChannelPagerAdapter(childFragmentManager)
                mPager.adapter = mAdapter
                mAdapter.notifyDataSetChanged()
                persistChannelConfigConfig()
                mGroupIndex = 0
                groupViewModel.syncChannels(showFavs)
                return true
            }
            R.id.menuChannelList, R.id.menuFavourties -> {
                mPager.adapter = null
                mAdapter.notifyDataSetChanged()
                mAdapter = ChannelPagerAdapter(childFragmentManager)
                mPager.adapter = mAdapter
                mAdapter.notifyDataSetChanged()
                showFavs = !showFavs
                persistChannelConfigConfig()
                mGroupIndex = 0
                groupViewModel.getGroupList(showFavs, true)
                return true
            }

            else -> return false
        }
    }

    /**
     * Persist channel config config.
     */
    @SuppressLint("CommitPrefEdits")
    private fun persistChannelConfigConfig() {
        val editor = prefs.prefs.edit()
        editor.putBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, showFavs)
        editor.commit()
        super.onPause()
    }

    /**
     * The Class PagerAdapter.
     */
    internal inner class ChannelPagerAdapter
    /**
     * Instantiates a new pager adapter.
     *
     * @param fm the fm
     */
    (fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        var groups: List<ChannelGroup>? = null

        /*
		 * (non-Javadoc)
		 *
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
        override fun getItem(position: Int): Fragment {
            val groupId = groups?.get(position)?.id
            val bundle = Bundle()
            groupId?.let { bundle.putLong(KEY_GROUP_ID, it) }
            bundle.putInt(KEY_GROUP_INDEX, position)
            bundle.putInt(ChannelList.KEY_CHANNEL_INDEX, getChannelIndex(position))
            bundle.putBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, showFavs)
            val fragment = fragmentManager!!.fragmentFactory.instantiate(javaClass.classLoader!!, ChannelList::class.java.name)
            fragment.arguments = bundle
            return fragment
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        fun getGroupId(position: Int): Long {
            return groups!![position].id
        }


        /*
				 * (non-Javadoc)
				 *
				 * @see android.support.v4.view.PagerAdapter#getCount()
				 */
        override fun getCount(): Int {
            return if (CollectionUtils.isNotEmpty(groups)) {
                groups!!.size
            } else 0
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val groupName = groups!![position].name
            return if (StringUtils.isNotBlank(groupName)) groupName else StringUtils.EMPTY
        }

    }

    /**
     * Sets the position.
     *
     * @param channelIndex the new channel index
     */
    fun setChannelSelection(groupId: Long, channelIndex: Int) {
        val uri = ChannelList.BASE_CONTENT_URI.buildUpon().appendPath(groupId.toString()).appendQueryParameter("index", channelIndex.toString()).build()
        index[mPager.currentItem] = channelIndex
        context?.contentResolver?.notifyChange(uri, null)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_GROUP_INDEX, mPager.currentItem)
        outState.putSerializable(KEY_INDEX, index)
    }

    private fun showProgress(show: Boolean) {
        mProgress.visibility = if (show) View.VISIBLE else View.GONE
        mPager.visibility = if (show) View.GONE else View.VISIBLE
        mPagerIndicator.visibility = if (show) View.GONE else View.VISIBLE
    }

    public override fun getStringSafely(resId: Int): String {
        var result = ""
        if (!isDetached && isAdded && isVisible) {
            try {
                result = getString(resId)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return result
    }

    interface OnGroupTypeChangedListener {

        fun groupTypeChanged(type: Int)

    }

    interface OnGroupChangedListener {

        fun groupChanged(groupId: Long, groupIndex: Int, channelIndex: Int)

    }

    override fun onPageScrollStateChanged(arg0: Int) {

    }

    override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {

    }

    override fun onPageSelected(position: Int) {
        mGroupIndex = position
        mGroupCHangedListener?.groupChanged(mAdapter.getGroupId(mGroupIndex), mGroupIndex, getChannelIndex(mGroupIndex))
    }

    fun updateIndex(groupIndex: Int, channelIndex: Int) {
        index[groupIndex] = channelIndex
    }

    private fun getChannelIndex(groupIndex: Int): Int {
        val channelIndex = index[groupIndex]
        return channelIndex ?: 0
    }

    companion object {

        private val KEY_INDEX = ChannelPager::class.java.name + "KEY_INDEX"
        val KEY_GROUP_INDEX = ChannelPager::class.java.name + "KEY_GROUP_INDEX"
        val KEY_GROUP_ID = ChannelPager::class.java.name + "KEY_GROUP_ID"
        val KEY_HIDE_FAV_SWITCH = ChannelPager::class.java.name + "KEY_HIDE_FAV_SWITCH"
    }

}
