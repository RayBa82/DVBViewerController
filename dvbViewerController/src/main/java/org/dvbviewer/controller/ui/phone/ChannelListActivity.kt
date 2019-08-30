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
package org.dvbviewer.controller.ui.phone

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.loader.content.Loader
import org.dvbviewer.controller.R
import org.dvbviewer.controller.activitiy.base.GroupDrawerActivity
import org.dvbviewer.controller.data.ProviderConsts
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.data.entities.IEPG
import org.dvbviewer.controller.ui.fragments.ChannelList
import org.dvbviewer.controller.ui.fragments.ChannelPager
import org.dvbviewer.controller.ui.fragments.EPGDetails
import org.dvbviewer.controller.ui.fragments.EpgPager

class ChannelListActivity : GroupDrawerActivity(), ChannelList.OnChannelSelectedListener, EpgPager.OnChannelScrolledListener, ChannelPager.OnGroupTypeChangedListener, IEpgDetailsActivity.OnIEPGClickListener {

    private var mChannelPager: ChannelPager? = null
    private var container: View? = null
    private var groupTypeChanged: Boolean = false

    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_drawer)
        super.onCreate(savedInstanceState)
        container = findViewById(R.id.right_content)
        initFragments(savedInstanceState)
    }

    private fun initFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            mChannelPager = ChannelPager()
            mChannelPager!!.arguments = intentToFragmentArguments(intent)
            supportFragmentManager.beginTransaction()
                    .add(R.id.left_content, mChannelPager!!, CHANNEL_PAGER_TAG)
                    .commit()
            if (container != null) {
                mEpgPager = EpgPager()
                mEpgPager!!.arguments = intentToFragmentArguments(intent)
                supportFragmentManager.beginTransaction()
                        .add(R.id.right_content, mEpgPager!!, EPG_PAGER_TAG)
                        .commit()
            }
        } else {
            mChannelPager = supportFragmentManager.findFragmentByTag(CHANNEL_PAGER_TAG) as ChannelPager?
            if (container != null) {
                mEpgPager = supportFragmentManager.findFragmentByTag(EPG_PAGER_TAG) as EpgPager?
            }
        }
    }

    override fun channelSelected(groupId: Long, groupIndex: Int, channelIndex: Int) {
        mChannelPager!!.updateIndex(groupIndex, channelIndex)
        if (container == null) {
            val epgPagerIntent = Intent(this, EpgPagerActivity::class.java)
            epgPagerIntent.putExtra(ChannelPager.KEY_GROUP_ID, groupId)
            epgPagerIntent.putExtra(ChannelPager.KEY_GROUP_INDEX, groupIndex)
            epgPagerIntent.putExtra(ChannelList.KEY_CHANNEL_INDEX, channelIndex)
            startActivity(epgPagerIntent)
        } else {
            mEpgPager!!.setPosition(channelIndex)
        }
    }


    override fun channelChanged(groupId: Long, channelIndex: Int) {
        mChannelPager!!.setChannelSelection(groupId, channelIndex)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        super.onItemClick(parent, view, position, id)
        mChannelPager!!.setPosition(position)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var result = super.onOptionsItemSelected(item)
        if (mChannelPager != null) {
            result = mChannelPager!!.onOptionsItemSelected(item)
        }
        if (mEpgPager != null) {
            result = mEpgPager!!.onOptionsItemSelected(item)
        }
        return result
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        super.onLoadFinished(loader, data)
        if (container == null) {
            val f = supportFragmentManager.findFragmentByTag(CHANNEL_PAGER_TAG)
            if (f == null) {
                val tran = supportFragmentManager.beginTransaction()
                mChannelPager = ChannelPager()
                tran.add(R.id.content_frame, mChannelPager!!, CHANNEL_PAGER_TAG)
                tran.commitAllowingStateLoss()
            } else {
                mChannelPager = f as ChannelPager?
            }
        }
        if (container != null && groupTypeChanged) {
            data.moveToFirst()
            mEpgPager!!.refresh(data.getLong(data.getColumnIndex(ProviderConsts.GroupTbl._ID)), 0)
        }
        groupTypeChanged = false
    }

    override fun groupTypeChanged(type: Int) {
        groupTypeChanged = true
        showFavs = prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false)
        supportLoaderManager.restartLoader(0, intent.extras, this)
        groupIndex = 0
    }

    override fun onIEPGClick(iepg: IEPG) {
        val details = EPGDetails()
        val bundle = Bundle()
        bundle.putParcelable(IEPG::class.java.simpleName, iepg)
        details.arguments = bundle
        details.show(supportFragmentManager, IEPG::class.java.name)
    }
}