package org.dvbviewer.controller.activitiy.base

import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import org.dvbviewer.controller.activitiy.DrawerActivity
import org.dvbviewer.controller.data.ProviderConsts
import org.dvbviewer.controller.data.entities.ChannelGroup
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.ui.fragments.ChannelEpg
import org.dvbviewer.controller.ui.fragments.ChannelPager
import org.dvbviewer.controller.ui.fragments.EpgPager
import java.util.*

abstract class GroupDrawerActivity : DrawerActivity(), OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, ChannelEpg.EpgDateInfo, ChannelPager.OnGroupChangedListener {

    protected lateinit var prefs: DVBViewerPreferences
    protected var mEpgPager: EpgPager? = null
    protected var groupIndex = 0
    protected var showFavs: Boolean = false
    override var epgDate: Long = 0


    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android
	 * .os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDisplayHomeAsUpEnabled(true)
        prefs = DVBViewerPreferences(this)
        showFavs = prefs.prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false)
        epgDate = if (savedInstanceState != null && savedInstanceState.containsKey(ChannelEpg.KEY_EPG_DAY)) savedInstanceState.getLong(ChannelEpg.KEY_EPG_DAY) else Date().time
        if (savedInstanceState != null) {
            groupIndex = savedInstanceState.getInt(ChannelPager.KEY_GROUP_INDEX, 0)
        } else {
            groupIndex = intent.getIntExtra(ChannelPager.KEY_GROUP_INDEX, 0)
        }
        supportLoaderManager.initLoader(0, savedInstanceState, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        showFavs = prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false)
        val selection = if (showFavs) ProviderConsts.GroupTbl.TYPE + " = " + ChannelGroup.TYPE_FAV else ProviderConsts.GroupTbl.TYPE + " = " + ChannelGroup.TYPE_CHAN
        val orderBy = ProviderConsts.GroupTbl._ID
        return CursorLoader(this, ProviderConsts.GroupTbl.CONTENT_URI, null, selection, null, orderBy)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mDrawerAdapter.changeCursor(data)
        mDrawerList.setItemChecked(groupIndex, true)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }


    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        groupIndex = position
        mDrawerLayout.closeDrawers()
    }

    /* (non-Javadoc)
 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onSaveInstanceState(android.os.Bundle)
 */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(ChannelEpg.KEY_EPG_DAY, epgDate)
        outState.putInt(ChannelPager.KEY_GROUP_INDEX, groupIndex)
        super.onSaveInstanceState(outState)
    }

    override fun groupChanged(groupId: Long, groupIndex: Int, channelIndex: Int) {
        this.groupIndex = groupIndex
        for (i in 0 until mDrawerList.adapter.count) {
            mDrawerList.setItemChecked(i, false)
        }
        mDrawerList.setItemChecked(groupIndex, true)
        if (mEpgPager != null) {
            mEpgPager!!.refresh(groupId, channelIndex)
        }

    }

    companion object {

        val CHANNEL_PAGER_TAG = ChannelPager::class.java.simpleName
        val EPG_PAGER_TAG = EpgPager::class.java.simpleName
    }

}
