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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import okhttp3.ResponseBody
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.api.ApiResponse
import org.dvbviewer.controller.data.api.ApiStatus
import org.dvbviewer.controller.data.entities.Timer
import org.dvbviewer.controller.data.timer.TimerRepository
import org.dvbviewer.controller.data.timer.TimerViewModel
import org.dvbviewer.controller.data.timer.TimerViewModelFactory
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
class TimerList : BaseListFragment(), Callback, OnClickListener, TimerDetails.OnTimerEditedListener, AdapterView.OnItemLongClickListener {

    private lateinit var mAdapter: TimerAdapter
    private var mode: ActionMode? = null
    private var actionMode: Boolean = false
    private lateinit var repository: TimerRepository
    private lateinit var viewModel: TimerViewModel

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = TimerAdapter()
        setHasOptionsMenu(true)
        repository = TimerRepository(getDmsInterface())
        val vFac = TimerViewModelFactory(repository)
        viewModel = ViewModelProvider(this, vFac)
                .get(TimerViewModel::class.java)

    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listAdapter = mAdapter
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
        val timerObserver = Observer<ApiResponse<List<Timer>>> { response -> onTimerLoaded(response!!) }
        viewModel.getTimerList().observe(this, timerObserver)
        setListShown(false)
    }

    private fun onTimerLoaded(response: ApiResponse<List<Timer>>?) {
        if(response?.status == ApiStatus.SUCCESS) {
            mAdapter.items = response.data
        } else if(response?.status == ApiStatus.ERROR) {
            catchException(TAG, response.e)
        }
        setListShown(true)
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
     * @author RayBa
     * @date 04.06.2010
     * @description Instantiates a new recording adapter.
     */
        : ArrayListAdapter<Timer>() {

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
    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        if (actionMode) {
            v.isSelected = !v.isSelected
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
        if (!actionMode) {
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
        viewModel.getTimerList(true)
        setListShown(false)
    }

    /* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(android.view.MenuItem)
	 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuRefresh -> {
                refresh()
                true
            }

            else -> false
        }
    }

    companion object {

        const val TAG = "TimerList"
        const val ACTION_MODE = "action_mode"
        const val CHECKED_ITEM_COUNT = "checked_item_count"
    }
}
