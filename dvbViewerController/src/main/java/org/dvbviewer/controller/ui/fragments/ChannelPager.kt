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
import android.database.Cursor
import android.database.MatrixCursor
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.PagerTitleStrip
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.DbHelper
import org.dvbviewer.controller.data.ProviderConsts.GroupTbl
import org.dvbviewer.controller.entities.ChannelGroup
import org.dvbviewer.controller.entities.DVBViewerPreferences
import org.dvbviewer.controller.entities.EpgEntry
import org.dvbviewer.controller.io.RecordingService
import org.dvbviewer.controller.io.ServerRequest
import org.dvbviewer.controller.io.data.ChannelHandler
import org.dvbviewer.controller.io.data.EpgEntryHandler
import org.dvbviewer.controller.ui.base.AsyncLoader
import org.dvbviewer.controller.ui.base.BaseFragment
import org.dvbviewer.controller.ui.base.CursorPagerAdapter
import org.dvbviewer.controller.utils.*
import java.io.InputStream
import java.text.MessageFormat
import java.util.*


/**
 * The Class EpgPager.
 *
 * @author RayBa82
 */
class ChannelPager : BaseFragment(), LoaderCallbacks<Cursor>, OnPageChangeListener {
    private var mGroupIndex = AdapterView.INVALID_POSITION
    private var index = HashMap<Int, Int>()
    private var showFavs: Boolean = false
    private var showGroups: Boolean = false
    private var showExtraGroup: Boolean = false
    private var showNowPlaying: Boolean = false
    private var showNowPlayingWifi: Boolean = false
    private var refreshGroupType: Boolean = false
    private var hideFavSwitch = false
    private var mProgress: View? = null
    private var mGroupCursor: Cursor? = null
    private var mPager: ViewPager? = null
    private var mNetworkInfo: NetworkInfo? = null
    private var mAdapter: ChannelPagerAdapter? = null
    private var mPagerIndicator: PagerTitleStrip? = null
    private var prefs: DVBViewerPreferences? = null
    private var mGroupCHangedListener: OnGroupChangedListener? = null
    private var mOnGroupTypeCHangedListener: OnGroupTypeChangedListener? = null

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
        mAdapter = ChannelPagerAdapter(childFragmentManager, mGroupCursor)
        val connManager = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        mNetworkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        prefs = DVBViewerPreferences(context!!)
        showGroups = prefs!!.prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_GROUPS, true)
        showExtraGroup = prefs!!.prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_ALL_AS_GROUP, false)
        showFavs = prefs!!.prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false)
        showNowPlaying = prefs!!.prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_NOW_PLAYING, true)
        showNowPlayingWifi = prefs!!.prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_NOW_PLAYING_WIFI_ONLY, true)
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
        if (mPager != null) {
            mPager!!.setCurrentItem(mGroupIndex, false)
        }
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity!!.setTitle(if (showFavs) R.string.favourites else R.string.channelList)
        mPager!!.adapter = mAdapter
        mPager!!.pageMargin = UIUtils.dipToPixel(context!!, 25).toInt()
        mPager!!.addOnPageChangeListener(this)

        var loaderId = LOAD_CHANNELS
        if (savedInstanceState == null) {
            /**
             * Prüfung ob das EPG in der Senderliste angezeigt werden soll.
             */
            if (!Config.CHANNELS_SYNCED) {
                loaderId = SYNCHRONIZE_CHANNELS
            } else if (showNowPlaying && !showNowPlayingWifi || showNowPlaying && mNetworkInfo!!.isConnected) {
                loaderId = LOAD_CURRENT_PROGRAM
            }
        }
        mPager!!.currentItem = mGroupIndex
        loaderManager.initLoader(loaderId, savedInstanceState, this)
        showProgress(savedInstanceState == null)
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
        mPagerIndicator!!.visibility = if (showGroups) View.VISIBLE else View.GONE
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
        val itemId = item.itemId
        when (itemId) {

            R.id.menuRefresh -> {
                refresh(LOAD_CURRENT_PROGRAM)
                return true
            }
            R.id.menuSyncChannels -> {
                refreshGroupType = true
                refresh(SYNCHRONIZE_CHANNELS)
                return true
            }
            R.id.menuChannelList, R.id.menuFavourties -> {
                showFavs = !showFavs
                mGroupIndex = 0
                persistChannelConfigConfig()
                activity!!.setTitle(if (showFavs) R.string.favourites else R.string.channelList)
                refreshGroupType = true
                refresh(LOAD_CHANNELS)
                activity!!.supportInvalidateOptionsMenu()
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
        val editor = prefs!!.prefs.edit()
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
    (fm: FragmentManager, cursor: Cursor?) : CursorPagerAdapter(fm) {

        /*
		 * (non-Javadoc)
		 *
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
        override fun getItem(position: Int): Fragment {
            cursor?.moveToPosition(position)
            val groupId = cursor?.getLong(cursor!!.getColumnIndex(GroupTbl._ID))
            val args = Bundle()
            groupId?.let { args.putLong(KEY_GROUP_ID, it) }
            args.putInt(KEY_GROUP_INDEX, position)
            args.putInt(ChannelList.KEY_CHANNEL_INDEX, getChannelIndex(position))
            args.putBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, showFavs)
            return instantiate(context!!, ChannelList::class.java.name, args)
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        fun getGroupId(position: Int): Long {
            cursor!!.moveToPosition(position)
            return cursor!!.getLong(cursor!!.getColumnIndex(GroupTbl._ID))
        }


        /*
				 * (non-Javadoc)
				 *
				 * @see android.support.v4.view.PagerAdapter#getCount()
				 */
        override fun getCount(): Int {
            return if (cursor != null) {
                if (showGroups) {
                    if (showExtraGroup) {
                        cursor!!.count + 1
                    } else {
                        cursor!!.count
                    }
                } else {
                    1
                }
            } else 0
        }

        override fun getPageTitle(position: Int): CharSequence? {
            var title = getString(R.string.common_all)
            if (showExtraGroup) {
                cursor!!.moveToPosition(position - 1)
                if (position > 0) {
                    title = cursor!!.getString(cursor!!.getColumnIndex(GroupTbl.NAME))
                    return title
                }
            } else {
                cursor!!.moveToPosition(position)
                title = cursor!!.getString(cursor!!.getColumnIndex(GroupTbl.NAME))

            }
            return title
        }

    }

    /**
     * Sets the position.
     *
     * @param channelIndex the new channel index
     */
    fun setChannelSelection(groupId: Long, channelIndex: Int) {
        val uri = ChannelList.BASE_CONTENT_URI.buildUpon().appendPath(groupId.toString()).appendQueryParameter("index", channelIndex.toString()).build()
        index[mPager!!.currentItem] = channelIndex
        context!!.contentResolver.notifyChange(uri, null)
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
	 * android.os.Bundle)
	 */
    override fun onCreateLoader(id: Int, arg1: Bundle?): Loader<Cursor> {
        var loader: Loader<Cursor>
        when (id) {
            LOAD_CHANNELS -> {
                val selection = if (showFavs) GroupTbl.TYPE + " = " + ChannelGroup.TYPE_FAV else GroupTbl.TYPE + " = " + ChannelGroup.TYPE_CHAN
                val orderBy = GroupTbl._ID
                return CursorLoader(context!!, GroupTbl.CONTENT_URI, null, selection, null, orderBy)
            }
            LOAD_CURRENT_PROGRAM -> loader = object : AsyncLoader<Cursor>(context!!) {

                override fun loadInBackground(): Cursor? {
                    loadEpg()
                    return MatrixCursor(arrayOfNulls(1))
                }

            }
            else -> {
                loader = object : AsyncLoader<Cursor>(context!!) {

                    override fun loadInBackground(): Cursor? {

                        performRefresh()
                        return MatrixCursor(arrayOfNulls(1))
                    }

                }
            }
        }

        return loader
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
	 * .support.v4.content.Loader, java.lang.Object)
	 */
    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        when (loader.id) {
            LOAD_CURRENT_PROGRAM -> refresh(LOAD_CHANNELS)
            SYNCHRONIZE_CHANNELS ->
                /**
                 * Pr�fung ob das EPG in der Senderliste angezeigt werden soll.
                 */
                if (showNowPlaying && !showNowPlayingWifi || showNowPlaying && mNetworkInfo!!.isConnected) {
                    refresh(LOAD_CURRENT_PROGRAM)
                } else {
                    refresh(LOAD_CHANNELS)
                }
            LOAD_CHANNELS -> {
                mGroupCursor = cursor
                mAdapter?.changeCursor(mGroupCursor!!)
                mAdapter?.notifyDataSetChanged()
                mPager?.setCurrentItem(mGroupIndex, false)
                // mPager.setPageTransformer(true, new DepthPageTransformer());
                activity?.invalidateOptionsMenu()
                showProgress(false)
                if (refreshGroupType) {
                    mOnGroupTypeCHangedListener?.groupTypeChanged(if (showFavs) ChannelGroup.TYPE_FAV else ChannelGroup.TYPE_CHAN)
                }
                refreshGroupType = false
            }

            else -> showProgress(false)
        }
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
	 * .support.v4.content.Loader)
	 */
    override fun onLoaderReset(arg0: Loader<Cursor>) {}

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_GROUP_INDEX, mPager!!.currentItem)
        outState.putSerializable(KEY_INDEX, index)
    }

    private fun performRefresh() {
        val mDbHelper = DbHelper(context)
        var chanXml: InputStream? = null
        var favXml: InputStream? = null
        try {
            val version = RecordingService.getVersionString()
            if (!Config.isRSVersionSupported(version)) {
                showToast(context, MessageFormat.format(getStringSafely(R.string.version_unsupported_text), Config.SUPPORTED_RS_VERSION))
                return
            }
            /**
             * Request the Channels
             */
            chanXml = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_CHANNELS)
            val channelHandler = ChannelHandler()
            val chans = channelHandler.parse(chanXml, false)
            /**
             * Request the Favourites
             */
            favXml = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_FAVS)
            if (favXml != null) {
                val favs = channelHandler.parse(favXml, true)
                if (CollectionUtils.isNotEmpty(favs)) {
                    chans.addAll(favs)
                }
            }
            mDbHelper.saveChannelRoots(chans)


            /**
             * Get the Mac Address for WOL
             */
            val macAddress = NetUtils.getMacFromArpCache(ServerConsts.REC_SERVICE_HOST)
            ServerConsts.REC_SERVICE_MAC_ADDRESS = macAddress
            val prefEditor = prefs!!.prefs.edit()
            StatusList.getStatus(prefs!!, version)
            prefEditor.putBoolean(DVBViewerPreferences.KEY_CHANNELS_SYNCED, true)
            prefEditor.putString(DVBViewerPreferences.KEY_RS_VERSION, version)
            if (StringUtils.isNotBlank(macAddress)) {
                prefEditor.putString(DVBViewerPreferences.KEY_RS_MAC_ADDRESS, macAddress)
            }
            prefEditor.apply()
            Config.CHANNELS_SYNCED = true
        } catch (e: Exception) {
            catchException(javaClass.simpleName, e)
        } finally {
            mDbHelper.close()
            IOUtils.closeQuietly(chanXml)
            IOUtils.closeQuietly(favXml)
        }
    }

    private fun loadEpg() {
        val result: List<EpgEntry>
        val helper = DbHelper(context)
        var `is`: InputStream? = null
        try {
            val nowFloat = DateUtils.getFloatDate(Date())
            val builder = ChannelEpg.buildBaseEpgUrl()
                    .addQueryParameter("start", nowFloat)
                    .addQueryParameter("end", nowFloat)
            val handler = EpgEntryHandler()
            `is` = ServerRequest.getInputStream(builder.build().toString())
            result = handler.parse(`is`)
            helper.saveNowPlaying(result)
        } catch (e: Exception) {
            catchException(javaClass.simpleName, e)
        } finally {
            IOUtils.closeQuietly(`is`)
            helper.close()
        }
    }

    /**
     * Refresh.
     *
     * @param id the id
     */
    private fun refresh(id: Int) {
        mGroupCursor = null
        mPager!!.adapter = null
        mAdapter!!.notifyDataSetChanged()
        mAdapter = ChannelPagerAdapter(childFragmentManager, mGroupCursor)
        mPager!!.adapter = mAdapter
        mAdapter!!.notifyDataSetChanged()
        loaderManager.destroyLoader(id)
        loaderManager.restartLoader(id, arguments, this)
        showProgress(true)
    }

    private fun showProgress(show: Boolean) {
        mProgress!!.visibility = if (show) View.VISIBLE else View.GONE
        mPager!!.visibility = if (show) View.GONE else View.VISIBLE
        if (showGroups) {
            mPagerIndicator!!.visibility = if (show) View.GONE else View.VISIBLE
        }
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
        if (mGroupCHangedListener != null) {
            mGroupCHangedListener!!.groupChanged(mAdapter!!.getGroupId(mGroupIndex), mGroupIndex, getChannelIndex(mGroupIndex))
        }
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
        private const val SYNCHRONIZE_CHANNELS = 0
        private const val LOAD_CHANNELS = 1
        private const val LOAD_CURRENT_PROGRAM = 2
    }

}
