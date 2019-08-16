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
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.view.ActionMode.Callback
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import okhttp3.ResponseBody
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.version.TimerRepository
import org.dvbviewer.controller.entities.Timer
import org.dvbviewer.controller.io.api.APIClient
import org.dvbviewer.controller.io.api.DMSInterface
import org.dvbviewer.controller.ui.base.AsyncLoader
import org.dvbviewer.controller.ui.base.BaseListFragment
import org.dvbviewer.controller.ui.phone.TimerDetailsActivity
import org.dvbviewer.controller.ui.widget.ClickableRelativeLayout
import org.dvbviewer.controller.utils.*
import retrofit2.Call
import retrofit2.Response
import java.util.*


/**
 * The Class TimerList.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class TimerList : BaseListFragment(), LoaderCallbacks<List<Timer>>, Callback, OnClickListener, TimerDetails.OnTimerEditedListener, AdapterView.OnItemLongClickListener {

    private lateinit var mAdapter: TimerAdapter
    private var mode: ActionMode? = null
    private var actionMode: Boolean = false
    private lateinit var repository: TimerRepository

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = TimerAdapter(context)
        setHasOptionsMenu(true)
        val dmsInterface = APIClient.client.create(DMSInterface::class.java)
        repository = TimerRepository(dmsInterface)

    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listAdapter = mAdapter
        val loader = loaderManager.initLoader(0, savedInstanceState, this)
        setListShown(!(!isResumed || loader.isStarted))
        listView!!.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView!!.onItemLongClickListener = this
        setEmptyText(resources.getString(R.string.no_timer))
        if (savedInstanceState != null && savedInstanceState.getBoolean(ACTION_MODE, false)) {
            val activity = activity as AppCompatActivity?
            mode = activity!!.startSupportActionMode(this)
            updateActionModeTitle(savedInstanceState.getInt(CHECKED_ITEM_COUNT))
        } else {
            activity!!.setTitle(R.string.timer)
        }
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
	 */
    override fun onCreateLoader(arg0: Int, arg1: Bundle?): Loader<List<Timer>> {
        return object : AsyncLoader<List<Timer>>(context!!) {

            override fun loadInBackground(): List<Timer> {
                try {
                    return repository.getTimer()!!
                } catch (e: Exception) {
                    catchException(javaClass.simpleName, e)
                }
                return Collections.emptyList()
            }
        }
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
	 */
    override fun onLoadFinished(arg0: Loader<List<Timer>>, arg1: List<Timer>) {
        mAdapter.items = arg1
        setListShown(true)
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
	 */
    override fun onLoaderReset(arg0: Loader<List<Timer>>) {
        arg0.reset()
        if (isVisible) {
            setListShown(true)
        }
    }

    override fun timerEdited(timer: Timer?) {
        timer?.let {
            setListShown(false)
            repository.saveTimer(it).enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    refresh()
                    sendMessage(R.string.timer_saved)
                    logEvent(EVENT_TIMER_EDITED)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    setListShown(true)
                    sendMessage(R.string.error_common)
                }
            })
        }
    }

    /**
     * The Class ViewHolder.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private class ViewHolder {
        internal var layout: ClickableRelativeLayout? = null
        internal var recIndicator: ImageView? = null
        internal var title: TextView? = null
        internal var channelName: TextView? = null
        internal var date: TextView? = null
    }

    /**
     * The Class TimerAdapter.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    inner class TimerAdapter
    /**
     * The Constructor.
     *
     * @param context the context
     * @author RayBa
     * @date 04.06.2010
     * @description Instantiates a new recording adapter.
     */
    (context: Context?) : ArrayListAdapter<Timer>() {

        /*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ViewHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_timer, parent, false)
                holder = ViewHolder()
                holder.layout = convertView as ClickableRelativeLayout?
                holder.recIndicator = convertView!!.findViewById(R.id.recIndicator)
                holder.title = convertView.findViewById(R.id.title)
                holder.channelName = convertView.findViewById(R.id.channelName)
                holder.date = convertView.findViewById(R.id.date)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            val o = getItem(position)
            if (o != null) {
                holder.title!!.text = o.title
                holder.channelName!!.text = o.channelName
                var date = DateUtils.getDateInLocalFormat(o.start)
                if (DateUtils.isToday(o.start!!.time)) {
                    date = resources.getString(R.string.today)
                } else if (DateUtils.isTomorrow(o.start!!.time)) {
                    date = resources.getString(R.string.tomorrow)
                }
                holder.layout!!.isError = o.isFlagSet(Timer.FLAG_EXECUTABLE)
                holder.layout!!.isDisabled = o.isFlagSet(Timer.FLAG_DISABLED)
                val start = DateUtils.getTimeInLocalFormat(context, o.start)
                val end = DateUtils.getTimeInLocalFormat(context, o.end)
                holder.date!!.text = "$date  $start - $end"
                holder.recIndicator!!.visibility = if (o.isFlagSet(Timer.FLAG_RECORDING)) View.VISIBLE else View.GONE
            }

            return convertView!!
        }
    }

    /* (non-Javadoc)
     * @see org.dvbviewer.controller.ui.base.BaseListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    override fun onListItemClick(parent: ListView, view: View, position: Int, id: Long) {
        if (actionMode) {
            view.isSelected = !view.isSelected
            val count = checkedItemCount
            updateActionModeTitle(count)
            if (checkedItemCount == 0) {
                mode!!.finish()
            }
        } else {
            if (UIUtils.isTablet(context!!)) {
                onDestroyActionMode(mode)
                val timer = mAdapter.getItem(position)
                val timerdetails = TimerDetails.newInstance()
                val args = TimerDetails.buildBundle(timer)
                timerdetails.arguments = args
                timerdetails.setTargetFragment(this, 0)
                timerdetails.setOnTimerEditedListener(this)
                timerdetails.show(activity!!.supportFragmentManager, TimerDetails::class.java.name)
            } else {
                listView!!.setItemChecked(position, !listView!!.isItemChecked(position))
                val timer = mAdapter.getItem(position)
                val timerIntent = Intent(context, TimerDetailsActivity::class.java)
                val extras = TimerDetails.buildBundle(timer)
                timerIntent.putExtras(extras)
                startActivityForResult(timerIntent, TimerDetails.TIMER_RESULT)
            }
        }

    }

    /* (non-Javadoc)
 * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
 */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ACTION_MODE, actionMode)
        outState.putInt(CHECKED_ITEM_COUNT, checkedItemCount)
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int, android.content.Intent)
	 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == TimerDetails.RESULT_CHANGED) {
            val timer: Timer? = data?.getSerializableExtra("timer") as Timer?
            timerEdited(timer)
        }
    }

    /* (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.timer_list, menu)
    }

    /* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onCreateActionMode(com.actionbarsherlock.view.ActionMode, com.actionbarsherlock.view.Menu)
	 */
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        actionMode = true
        activity!!.menuInflater.inflate(R.menu.actionmode_recording, menu)
        return true
    }

    /* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onPrepareActionMode(com.actionbarsherlock.view.ActionMode, com.actionbarsherlock.view.Menu)
	 */
    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    /* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onActionItemClicked(com.actionbarsherlock.view.ActionMode, com.actionbarsherlock.view.MenuItem)
	 */
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuDelete -> {
                /**
                 * Alertdialog to confirm the delete of Recordings
                 */
                val builder = AlertDialog.Builder(context!!)
                builder.setMessage(resources.getString(R.string.confirmDelete)).setPositiveButton(resources.getString(R.string.yes), this).setNegativeButton(resources.getString(R.string.no), this).show()
            }

            else -> {
            }
        }
        return true
    }

    /**
     * The Class TimerDeleter.
     *
     * @author RayBa
     * @date 07.04.2013
     */

    /* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onDestroyActionMode(com.actionbarsherlock.view.ActionMode)
	 */
    override fun onDestroyActionMode(mode: ActionMode?) {
        actionMode = false
        clearSelection()
    }

    /**
     * Clear selection.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private fun clearSelection() {
        for (i in 0 until listAdapter!!.count) {
            listView!!.setItemChecked(i, false)
        }
    }

    /* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val checkedPositions = listView!!.checkedItemPositions
                if (checkedPositions != null && checkedPositions.size() > 0) {
                    setListShown(false)
                    val size = checkedPositions.size()
                    val timers = ArrayList<Timer>()
                    for (i in 0 until size) {
                        if (checkedPositions.valueAt(i)) {
                            timers.add(mAdapter.getItem(checkedPositions.keyAt(i)))
                        }
                    }
                    repository.deleteTimer(timers).enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            refresh()
                            sendMessage(R.string.timer_deleted)
                            logEvent(EVENT_TIMER_DELETED)
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            setListShown(true)
                            sendMessage(R.string.error_common)
                        }
                    })
                }
                mode?.finish()
            }

            DialogInterface.BUTTON_NEGATIVE -> {
            }
        }// No button clicked
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        listView!!.setItemChecked(position, true)
        val count = checkedItemCount
        if (actionMode == false) {
            actionMode = true
            val activty = activity as AppCompatActivity?
            mode = activty?.startSupportActionMode(this@TimerList)
        }
        updateActionModeTitle(count)
        return true
    }

    private fun updateActionModeTitle(count: Int) {
        mode?.title = count.toString() + " " + resources.getString(R.string.selected)
    }

    /**
     * Refresh.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private fun refresh() {
        loaderManager.restartLoader(0, arguments, this)
        setListShown(false)
    }

    /* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(android.view.MenuItem)
	 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.menuRefresh -> {
                refresh()
                return true
            }

            else -> return false
        }
    }

    companion object {

        val ACTION_MODE = "action_mode"
        val CHECKED_ITEM_COUNT = "checked_item_count"
    }
}
