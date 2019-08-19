/*
 * Copyright (C) 2012 dvbviewer-controller Project
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
package org.dvbviewer.controller.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.FragmentManager
import org.dvbviewer.controller.R
import org.dvbviewer.controller.activitiy.base.GroupDrawerActivity
import org.dvbviewer.controller.data.entities.DVBTarget
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.data.entities.IEPG
import org.dvbviewer.controller.data.media.MediaFile
import org.dvbviewer.controller.ui.adapter.MediaAdapter
import org.dvbviewer.controller.ui.fragments.*
import org.dvbviewer.controller.ui.fragments.ChannelList.OnChannelSelectedListener
import org.dvbviewer.controller.ui.fragments.Dashboard.OnDashboardButtonClickListener
import org.dvbviewer.controller.ui.phone.*
import org.dvbviewer.controller.utils.*
import java.util.*

/**
 * The Class HomeActivity.
 *
 * @author RayBa
 */
class HomeActivity : GroupDrawerActivity(), OnClickListener, OnChannelSelectedListener, OnDashboardButtonClickListener, Remote.OnTargetsChangedListener, IEpgDetailsActivity.OnIEPGClickListener, MediaAdapter.OnMediaClickListener {
    private var multiContainer: View? = null
    private var mSpinnerAdapter: ArrayAdapter<*>? = null
    private var mClientSpinner: AppCompatSpinner? = null
    private var chans: ChannelPager? = null
    private var enableDrawer: Boolean = false

    override var selectedTarget: DVBTarget? = null

    /* (non-Javadoc)
     * @see org.dvbviewer.controller.ui.base.BaseActivity#onCreate(android.os.Bundle)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_home)
        super.onCreate(savedInstanceState)
        multiContainer = findViewById(R.id.right_content)
        if (savedInstanceState == null) {
            val dashboard = Dashboard()
            var tran = supportFragmentManager.beginTransaction()
            tran.add(R.id.left_content, dashboard)
            tran.commit()
            if (multiContainer != null) {
                enableDrawer = true
                tran = supportFragmentManager.beginTransaction()
                chans = ChannelPager()
                chans!!.setHasOptionsMenu(true)
                val bundle = Bundle()
                bundle.putInt(ChannelPager.KEY_GROUP_INDEX, groupIndex)
                chans!!.arguments = bundle
                tran.add(multiContainer!!.id, chans!!, GroupDrawerActivity.Companion.CHANNEL_PAGER_TAG)
                tran.commit()
                setTitle(R.string.channelList)
            }
            if (Config.IS_FIRST_START) {
                Config.IS_FIRST_START = false
                val builder = AlertDialog.Builder(this)
                builder.setMessage(resources.getString(R.string.firstStartMessage)).setPositiveButton(R.string.yes, this).setTitle(resources.getString(R.string.firstStartMessageTitle))
                        .setNegativeButton(R.string.no, this).show()
                prefs = DVBViewerPreferences(this)
                prefs.prefs.edit()
                        .putBoolean(DVBViewerPreferences.KEY_IS_FIRST_START, false)
                        .apply()
            }
        } else {
            val frag = supportFragmentManager.findFragmentByTag(GroupDrawerActivity.Companion.CHANNEL_PAGER_TAG)
            if (frag != null && frag is ChannelPager) {
                chans = frag
            }
            enableDrawer = savedInstanceState.getBoolean(ENABLE_DRAWER, false)
            title = savedInstanceState.getString(TITLE)!!
        }
        initRemoteSpinner()
        setDrawerEnabled(enableDrawer)
    }

    private fun initRemoteSpinner() {
        mClientSpinner = findViewById(R.id.clientSpinner)
        if (mClientSpinner != null) {
            mClientSpinner!!.visibility = View.GONE
            mClientSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val selectedClient = mSpinnerAdapter!!.getItem(position) as String?
                    prefs.prefs.edit()
                            .putString(DVBViewerPreferences.KEY_SELECTED_CLIENT, selectedClient)
                            .apply()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
        }
    }


    /* (non-Javadoc)
         * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
         */
    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val settings = Intent(this@HomeActivity, PreferencesActivity::class.java)
                startActivity(settings)
            }
            else -> finish()
        }
    }

    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.fragments.Dashboard.OnDashboardButtonClickListener#onDashboarButtonClick(android.view.View)
	 */
    override fun onDashboarButtonClick(v: View) {
        val fm = supportFragmentManager
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        when (v.id) {
            R.id.home_btn_remote -> if (multiContainer != null) {
                enableDrawer = false
                val tran = fm.beginTransaction()
                tran.replace(multiContainer!!.id, Remote())
                tran.commit()
                setTitle(R.string.remote)
            } else {
                startActivity(Intent(this, RemoteActivity::class.java))
            }
            R.id.home_btn_channels -> if (multiContainer != null) {
                enableDrawer = true
                val tran = fm.beginTransaction()
                chans = ChannelPager()
                chans!!.setHasOptionsMenu(true)
                val bundle = Bundle()
                bundle.putInt(ChannelPager.KEY_GROUP_INDEX, groupIndex)
                chans!!.arguments = bundle
                tran.replace(multiContainer!!.id, chans!!, GroupDrawerActivity.Companion.CHANNEL_PAGER_TAG)
                tran.commit()
                setTitle(R.string.channelList)
            } else {
                startActivity(Intent(this, ChannelListActivity::class.java))
            }
            R.id.home_btn_timers -> if (multiContainer != null) {
                enableDrawer = false
                val tran = fm.beginTransaction()
                tran.replace(multiContainer!!.id, TimerList())
                tran.commit()
            } else {

                startActivity(Intent(this, TimerlistActivity::class.java))
            }
            R.id.home_btn_recordings -> if (multiContainer != null) {
                enableDrawer = false
                val tran = fm.beginTransaction()
                tran.replace(multiContainer!!.id, RecordingList())
                tran.commit()
                setTitle(R.string.recordings)
            } else {
                startActivity(Intent(this, RecordinglistActivity::class.java))
            }
            R.id.home_btn_settings -> startActivity(Intent(this, PreferencesActivity::class.java))
            R.id.home_btn_tasks -> if (multiContainer != null) {
                enableDrawer = false
                val tran = fm.beginTransaction()
                tran.replace(multiContainer!!.id, TaskListFragment())
                tran.commit()
                setTitle(R.string.tasks)
            } else {
                startActivity(Intent(this, TaskActivity::class.java))
            }
            R.id.home_btn_status -> if (multiContainer != null) {
                enableDrawer = false
                val tran = fm.beginTransaction()
                tran.replace(multiContainer!!.id, StatusList())
                tran.commit()
                setTitle(R.string.status)
            } else {
                startActivity(Intent(this, StatusActivity::class.java))
            }
            R.id.home_btn_medias -> if (multiContainer != null) {
                enableDrawer = false
                val b = Bundle()
                b.putLong(MediaList.KEY_PARENT_ID, 1)
                val mediaList = MediaList()
                mediaList.arguments = b
                val tran = fm.beginTransaction()
                tran.replace(multiContainer!!.id, mediaList)
                tran.commit()
            } else {
                startActivity(Intent(this, MedialistActivity::class.java))
            }

            else -> {
            }
        }
        if (mClientSpinner != null) {
            mClientSpinner!!.visibility = View.GONE
        }
        setDrawerEnabled(enableDrawer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuAbout -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.menuWOL -> {
                val wakeOnLanRunnabel = Runnable { NetUtils.sendWakeOnLan(prefs!!, ServerConsts.REC_SERVICE_WOL_PORT) }
                val wakeOnLanThread = Thread(wakeOnLanRunnabel)
                wakeOnLanThread.start()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun channelSelected(groupId: Long, groupIndex: Int, channelIndex: Int) {
        val channelListIntent = Intent(this, ChannelListActivity::class.java)
        channelListIntent.putExtra(ChannelPager.KEY_GROUP_ID, groupId)
        channelListIntent.putExtra(ChannelPager.KEY_GROUP_INDEX, groupIndex)
        channelListIntent.putExtra(ChannelList.KEY_CHANNEL_INDEX, channelIndex)
        channelListIntent.putExtra(ChannelPager.KEY_HIDE_FAV_SWITCH, true)
        startActivity(channelListIntent)

    }

    override fun targetsChanged(title: String, spinnerData: List<DVBTarget>?) {
        setTitle(title)
        if (mClientSpinner != null) {
            val clients = LinkedList<String>()
            spinnerData?.map { it.name }?.let { clients.addAll(it) }
            mSpinnerAdapter = ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, clients.toTypedArray())
            mSpinnerAdapter!!.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            mClientSpinner!!.adapter = mSpinnerAdapter
            val activeClient = prefs!!.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT)
            val index = clients.indexOf(activeClient)
            val spinnerPosition = if (index > Spinner.INVALID_POSITION) index else Spinner.INVALID_POSITION
            mClientSpinner!!.setSelection(spinnerPosition)
            mClientSpinner!!.visibility = View.VISIBLE
        }

    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        super.onItemClick(parent, view, position, id)
        if (chans != null) {
            chans!!.setPosition(position)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ENABLE_DRAWER, enableDrawer)
        outState.putString(TITLE, title.toString())
    }

    override fun onIEPGClick(iepg: IEPG) {
        val details = EPGDetails()
        val bundle = Bundle()
        bundle.putParcelable(IEPG::class.java.simpleName, iepg)
        details.arguments = bundle
        details.show(supportFragmentManager, IEPG::class.java.name)
    }

    override fun onMediaClick(mediaFile: MediaFile) {
        if (mediaFile.dirId > 0) {

            val mediaList = MediaList()
            val b = Bundle()
            b.putLong(MediaList.KEY_PARENT_ID, mediaFile.dirId)
            mediaList.arguments = b
            val tran = supportFragmentManager.beginTransaction()
            tran.replace(multiContainer!!.id, mediaList)
            tran.addToBackStack(MediaList::class.java.name + mediaFile.dirId!!)
            tran.commit()

        } else {
            val arguments = Bundle()
            arguments.putLong(StreamConfig.EXTRA_FILE_ID, mediaFile.id!!)
            arguments.putParcelable(StreamConfig.EXTRA_FILE_TYPE, FileType.VIDEO)
            arguments.putInt(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig)
            arguments.putString(StreamConfig.EXTRA_TITLE, mediaFile.name)
            val cfg = StreamConfig.newInstance()
            cfg.arguments = arguments
            cfg.show(supportFragmentManager, StreamConfig::class.java.name)
        }

    }

    override fun onMediaStreamClick(mediaFile: MediaFile) {
        val videoIntent = StreamUtils.buildQuickUrl(this, mediaFile.id!!, mediaFile.name, FileType.VIDEO)
        startActivity(videoIntent)
        val prefs = DVBViewerPreferences(this).streamPrefs
        val direct = prefs.getBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true)
        val bundle = Bundle()
        bundle.putString(PARAM_START, START_QUICK)
        bundle.putString(PARAM_TYPE, if (direct) TYPE_DIRECT else TYPE_TRANSCODED)
        bundle.putString(PARAM_NAME, mediaFile.name)
        mFirebaseAnalytics?.logEvent(EVENT_STREAM_MEDIA, bundle)
    }

    override fun onMediaContextClick(mediaFile: MediaFile) {
        val arguments = Bundle()
        arguments.putLong(StreamConfig.EXTRA_FILE_ID, mediaFile.id!!)
        arguments.putParcelable(StreamConfig.EXTRA_FILE_TYPE, FileType.VIDEO)
        arguments.putInt(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig)
        arguments.putString(StreamConfig.EXTRA_TITLE, mediaFile.name)
        val cfg = StreamConfig.newInstance()
        cfg.arguments = arguments
        cfg.show(supportFragmentManager, StreamConfig::class.java.name)
    }

    companion object {

        const val ENABLE_DRAWER = "ENABLE_DRAWER"
        const val TITLE = "title"
    }
}
