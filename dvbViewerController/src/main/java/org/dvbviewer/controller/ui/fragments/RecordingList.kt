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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.PopupMenu
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.data.entities.IEPG
import org.dvbviewer.controller.data.entities.Recording
import org.dvbviewer.controller.data.recording.RecordingRepository
import org.dvbviewer.controller.ui.base.AsyncLoader
import org.dvbviewer.controller.ui.base.BaseListFragment
import org.dvbviewer.controller.ui.phone.IEpgDetailsActivity
import org.dvbviewer.controller.ui.phone.StreamConfigActivity
import org.dvbviewer.controller.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * The Class RecordingList.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class RecordingList : BaseListFragment(), LoaderCallbacks<List<Recording>>, OnClickListener, ActionMode.Callback, OnCheckedChangeListener, View.OnClickListener, AdapterView.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {

    private var mAdapter: RecordingAdapter? = null
    private var mode: ActionMode? = null
    private var selectedPosition: Int = 0
    private var actionMode: Boolean = false
    private var clickListener: IEpgDetailsActivity.OnIEPGClickListener? = null
    private var recordings: MutableList<Recording>? = null
    private lateinit var recordingRepo: RecordingRepository

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = RecordingAdapter(context)
        setHasOptionsMenu(true)
        retainInstance = true
        recordingRepo = RecordingRepository(getDmsInterface())
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listAdapter = mAdapter
        listView!!.itemsCanFocus = false
        val loader = loaderManager.initLoader(0, savedInstanceState, this)
        setListShown(!(!isResumed || loader.isStarted))
        setEmptyText(resources.getString(R.string.no_recordings))
        listView!!.onItemLongClickListener = this
        if (savedInstanceState != null && savedInstanceState.getBoolean(ACTION_MODE, false)) {
            val activity = activity as AppCompatActivity?
            mode = activity!!.startSupportActionMode(this)
            updateActionModeTitle(savedInstanceState.getInt(CHECKED_ITEM_COUNT))
        }
        activity!!.setTitle(R.string.recordings)
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
	 */
    override fun onCreateLoader(arg0: Int, arg1: Bundle?): Loader<List<Recording>> {
        return object : AsyncLoader<List<Recording>>(context!!) {

            override fun loadInBackground(): List<Recording>? {
                try {
                    return recordingRepo.getRecordingList()
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
    override fun onLoadFinished(arg0: Loader<List<Recording>>, arg1: List<Recording>) {
        mAdapter!!.items = arg1
        setListShown(true)
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
	 */
    override fun onLoaderReset(arg0: Loader<List<Recording>>) {
        if (isVisible) {
            setListShown(true)
        }
    }


    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        listView!!.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView!!.setItemChecked(position, true)
        val count = checkedItemCount
        if (actionMode == false) {
            actionMode = true
            val activty = activity as AppCompatActivity?
            mode = activty!!.startSupportActionMode(this@RecordingList)
        }
        updateActionModeTitle(count)
        return true
    }

    private fun updateActionModeTitle(count: Int) {
        mode!!.title = count.toString() + " " + resources.getString(R.string.selected)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuStreamDirect -> {
                streamDirect(mAdapter!!.getItem(selectedPosition))
                return true
            }
            R.id.menuStreamTranscoded -> {
                streamTranscoded(mAdapter!!.getItem(selectedPosition))
                return true
            }
            R.id.menuStreamConfig -> {
                if (UIUtils.isTablet(context!!)) {
                    val arguments = getIntentExtras(mAdapter!!.getItem(selectedPosition))
                    val cfg = StreamConfig.newInstance()
                    cfg.arguments = arguments
                    cfg.show(activity!!.supportFragmentManager, StreamConfig::class.java.name)
                } else {
                    val arguments = getIntentExtras(mAdapter!!.getItem(selectedPosition))
                    val streamConfig = Intent(context, StreamConfigActivity::class.java)
                    streamConfig.putExtras(arguments)
                    startActivity(streamConfig)
                }
                return true
            }
            R.id.menuDelete -> {
                recordings = mutableListOf(mAdapter!!.getItem(selectedPosition))
                val builder = AlertDialog.Builder(context!!)
                builder.setMessage(resources.getString(R.string.confirmDelete)).setPositiveButton(resources.getString(R.string.yes), this).setNegativeButton(resources.getString(R.string.no), this).show()
                return true
            }

            else -> {
            }
        }
        return false
    }

    private fun streamDirect(recording: IEPG) {
        try {
            val videoIntent = StreamUtils.getDirectUrl(recording.id, recording.title, FileType.RECORDING)
            activity!!.startActivity(videoIntent)
            val bundle = Bundle()
            bundle.putString(PARAM_START, START_QUICK)
            bundle.putString(PARAM_TYPE, TYPE_DIRECT)
            bundle.putString(PARAM_NAME, recording.title)
            logEvent(EVENT_STREAM_RECORDING, bundle)
        } catch (e: ActivityNotFoundException) {
            val builder = AlertDialog.Builder(context!!)
            builder.setMessage(resources.getString(R.string.noFlashPlayerFound)).setPositiveButton(resources.getString(R.string.yes), null).setNegativeButton(resources.getString(R.string.no), null).show()
            e.printStackTrace()
        }

    }

    private fun streamTranscoded(recording: IEPG) {
        try {
            val videoIntent = StreamUtils.getTranscodedUrl(context, recording.id, recording.title, FileType.RECORDING)
            activity!!.startActivity(videoIntent)
            val bundle = Bundle()
            bundle.putString(PARAM_START, START_QUICK)
            bundle.putString(PARAM_TYPE, TYPE_TRANSCODED)
            bundle.putString(PARAM_NAME, recording.title)
            logEvent(EVENT_STREAM_RECORDING, bundle)
        } catch (e: ActivityNotFoundException) {
            val builder = AlertDialog.Builder(context!!)
            builder.setMessage(resources.getString(R.string.noFlashPlayerFound)).setPositiveButton(resources.getString(R.string.yes), null).setNegativeButton(resources.getString(R.string.no), null).show()
            e.printStackTrace()
        }

    }

    private fun getIntentExtras(recording: IEPG): Bundle {
        val arguments = Bundle()
        arguments.putLong(StreamConfig.EXTRA_FILE_ID, recording.id)
        arguments.putParcelable(StreamConfig.EXTRA_FILE_TYPE, FileType.RECORDING)
        arguments.putInt(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig)
        arguments.putString(StreamConfig.EXTRA_TITLE, recording.title)
        return arguments
    }

    /**
     * The Class ViewHolder.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private class ViewHolder {
        internal var thumbNail: ImageView? = null
        internal var thumbNailContainer: View? = null
        internal var title: TextView? = null
        internal var subTitle: TextView? = null
        internal var channelName: TextView? = null
        internal var date: TextView? = null
        internal var contextMenu: ImageView? = null
    }

    /**
     * The Class RecordingAdapter.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    inner class RecordingAdapter
    /**
     * The Constructor.
     *
     * @param context the context
     * @author RayBa
     * @date 04.06.2010
     * @description Instantiates a new recording adapter.
     */
    (context: Context?) : ArrayListAdapter<Recording>() {

        internal val placeHolder: Drawable?

        init {
            placeHolder = context?.let { AppCompatResources.getDrawable(it, R.drawable.ic_play_white_40dp) }
        }

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
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_recording, parent, false)
                holder = ViewHolder()
                holder.thumbNail = convertView!!.findViewById(R.id.thumbNail)
                holder.title = convertView.findViewById(R.id.title)
                holder.subTitle = convertView.findViewById(R.id.subTitle)
                holder.channelName = convertView.findViewById(R.id.channelName)
                holder.date = convertView.findViewById(R.id.date)
                holder.contextMenu = convertView.findViewById(R.id.contextMenu)
                holder.contextMenu!!.setOnClickListener(this@RecordingList)
                holder.thumbNailContainer = convertView.findViewById(R.id.thumbNailContainer)
                holder.thumbNailContainer!!.setOnClickListener(this@RecordingList)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            val o = getItem(position)
            if (o != null) {
                //				holder.layout.setChecked(getListView().isItemChecked(position));
                holder.title!!.text = o.title
                if (TextUtils.isEmpty(o.subTitle)) {
                    holder.subTitle!!.visibility = View.GONE
                } else {
                    holder.subTitle!!.visibility = View.VISIBLE
                    holder.subTitle!!.text = o.subTitle
                }
                holder.thumbNail!!.setImageDrawable(null)
                if (TextUtils.isEmpty(o.thumbNail)) {
                    holder.thumbNailContainer!!.visibility = View.GONE
                } else {
                    holder.thumbNailContainer!!.visibility = View.VISIBLE
                    holder.thumbNail!!.setImageDrawable(null)
                    Picasso.get()
                            .load(ServerConsts.REC_SERVICE_URL + ServerConsts.THUMBNAILS_VIDEO_URL + o.thumbNail)
                            .placeholder(placeHolder!!)
                            .fit()
                            .centerInside()
                            .into(holder.thumbNail)
                }
                holder.thumbNailContainer!!.tag = position
                holder.date!!.text = DateUtils.formatDateTime(context, o.start.time, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH)
                holder.channelName!!.text = o.channel
                holder.contextMenu!!.tag = position
            }
            return convertView
        }
    }


    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        Log.i("onListItemClick", "onListItemClick ")
        if (actionMode) {
            v.isSelected = !v.isSelected
            val count = checkedItemCount
            updateActionModeTitle(count)
            if (checkedItemCount == 0) {
                mode!!.finish()
            }
        } else {
            val entry = mAdapter!!.getItem(position)
            if (clickListener != null) {
                clickListener!!.onIEPGClick(entry)
                return
            }
            val details = Intent(context, IEpgDetailsActivity::class.java)
            details.putExtra(IEPG::class.java.simpleName, entry)
            startActivity(details)
            clearSelection()
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
                val checkedPositions = listView!!.checkedItemPositions
                if (checkedPositions != null && checkedPositions.size() > 0) {

                    val size = checkedPositions.size()
                    recordings = ArrayList()
                    for (i in 0 until size) {
                        if (checkedPositions.valueAt(i)) {
                            recordings!!.add(mAdapter!!.getItem(checkedPositions.keyAt(i)))
                        }
                    }
                    /**
                     * Alertdialog to confirm the delete of Recordings
                     */
                    val builder = AlertDialog.Builder(context!!)
                    builder.setMessage(resources.getString(R.string.confirmDelete)).setPositiveButton(resources.getString(R.string.yes), this).setNegativeButton(resources.getString(R.string.no), this).show()
                }
            }

            else -> {
            }
        }
        return true
    }

    /* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onDestroyActionMode(com.actionbarsherlock.view.ActionMode)
	 */
    override fun onDestroyActionMode(mode: ActionMode) {
        clearSelection()
        listView!!.choiceMode = ListView.CHOICE_MODE_SINGLE
        actionMode = false
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
        mAdapter!!.notifyDataSetChanged()
    }


    /* (non-Javadoc)
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {}

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity!!.menuInflater.inflate(R.menu.context_menu_recordinglist, menu)
    }

    /* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
	 */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.recording_list, menu)
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
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val call = recordingRepo.deleteRecording(recordings!!)
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        sendMessage(getString(R.string.recording_deleted))
                        refresh()
                        logEvent(EVENT_RECORDING_DELETED)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        sendMessage(R.string.error_common)
                    }
                })
                if (mode != null) {
                    mode!!.finish()
                }
            }

            DialogInterface.BUTTON_NEGATIVE -> {
            }
        }// No button clicked
    }

    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseListFragment#onDestroyView()
	 */
    override fun onDestroyView() {
        if (mode != null) {
            mode!!.finish()
        }
        super.onDestroyView()
    }

    /* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
    override fun onClick(v: View) {
        when (v.id) {
            R.id.contextMenu -> {
                selectedPosition = v.tag as Int
                val popup = PopupMenu(context!!, v)
                popup.inflate(R.menu.context_menu_stream)
                popup.inflate(R.menu.context_menu_recordinglist)
                popup.setOnMenuItemClickListener(this)
                popup.show()
            }
            R.id.thumbNailContainer -> try {
                selectedPosition = v.tag as Int
                val recording = mAdapter!!.getItem(selectedPosition)
                val videoIntent = StreamUtils.buildQuickUrl(context, recording.id, recording.title, FileType.RECORDING)
                activity!!.startActivity(videoIntent)
                val prefs = DVBViewerPreferences(context).streamPrefs
                val direct = prefs.getBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true)
                val bundle = Bundle()
                bundle.putString(PARAM_START, START_QUICK)
                bundle.putString(PARAM_TYPE, if (direct) TYPE_DIRECT else TYPE_TRANSCODED)
                bundle.putString(PARAM_NAME, recording.title)
                logEvent(EVENT_STREAM_RECORDING, bundle)
            } catch (e: ActivityNotFoundException) {
                val builder = AlertDialog.Builder(context!!)
                builder.setMessage(resources.getString(R.string.noFlashPlayerFound)).setPositiveButton(resources.getString(R.string.yes), null).setNegativeButton(resources.getString(R.string.no), null).show()
                e.printStackTrace()
            }

            else -> {
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IEpgDetailsActivity.OnIEPGClickListener) {
            clickListener = context
        }
    }

    companion object {

        val ACTION_MODE = "action_mode"
        val CHECKED_ITEM_COUNT = "checked_item_count"
    }

}
