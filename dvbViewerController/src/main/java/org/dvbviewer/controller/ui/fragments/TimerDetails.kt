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

import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.SwitchCompat
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.entities.Timer
import org.dvbviewer.controller.ui.base.BaseDialogFragment
import org.dvbviewer.controller.ui.widget.DateField
import org.dvbviewer.controller.utils.DateUtils
import java.util.*


/**
 * The Class TimerDetails.
 *
 * @author RayBa
 */
class TimerDetails : BaseDialogFragment(), OnDateSetListener, OnClickListener, OnLongClickListener {

    private var timer: Timer? = null
    private var channelField: TextView? = null
    private var titleField: TextView? = null
    private var activeBox: SwitchCompat? = null
    private var dateField: DateField? = null
    private var startField: DateField? = null
    private var stopField: DateField? = null
    private var startTimeSetListener: OnTimeSetListener? = null
    private var stopTimeSetListener: OnTimeSetListener? = null
    private var cal: Calendar? = null
    private var postRecordSpinner: Spinner? = null
    private var mOntimeredEditedListener: OnTimerEditedListener? = null
    private var monitoringSpinner: Spinner? = null
    private var monitoringLabel: TextView? = null
    private var preField: AppCompatEditText? = null
    private var postField: AppCompatEditText? = null


    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cal = GregorianCalendar.getInstance()
        if (savedInstanceState == null) {
            bundleToTimer(arguments!!)
        } else {
            bundleToTimer(savedInstanceState)
        }

    }

    /* (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockDialogFragment#onAttach(android.app.Activity)
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTimerEditedListener) {
            mOntimeredEditedListener = context
        }
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (timer != null) {
            val create = timer!!.id < 0L
            titleField!!.text = timer!!.title
            dateField!!.date = timer!!.start
            val start = if (create) timer!!.start else DateUtils.addMinutes(timer!!.start, timer!!.pre)
            val stop = if (create) timer!!.end else DateUtils.addMinutes(timer!!.end, timer!!.post * -1)
            activeBox!!.isChecked = !timer!!.isFlagSet(Timer.FLAG_DISABLED)
            startField!!.setTime(start)
            stopField!!.setTime(stop)
            preField!!.setText(timer!!.pre.toString())
            postField!!.setText(timer!!.post.toString())
            val invalidindex = timer!!.timerAction >= postRecordSpinner!!.count
            postRecordSpinner!!.setSelection(if (invalidindex) 0 else timer!!.timerAction)
            if (!TextUtils.isEmpty(timer!!.channelName)) {
                channelField!!.text = timer!!.channelName
            }
            if (StringUtils.isNotBlank(timer!!.pdc)) {
                monitoringSpinner!!.setSelection(timer!!.monitorPDC)
            } else {
                monitoringLabel!!.visibility = View.GONE
                monitoringSpinner!!.visibility = View.GONE
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(if (timer != null && timer!!.id <= 0) R.string.createTimer else R.string.editTimer)
        return dialog
    }

    /* (non-Javadoc)
         * @see android.support.v4.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
         */
    override fun onSaveInstanceState(arg0: Bundle) {
        super.onSaveInstanceState(arg0)
        timerToBundle(timer!!, arg0)
    }


    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = activity!!.layoutInflater.inflate(R.layout.fragment_timer_details, container, false)
        titleField = v.findViewById(R.id.titleField)
        dateField = v.findViewById(R.id.dateField)
        activeBox = v.findViewById(R.id.activeBox)
        startField = v.findViewById(R.id.startField)
        preField = v.findViewById(R.id.pre)
        postField = v.findViewById(R.id.post)
        postRecordSpinner = v.findViewById(R.id.postRecordingSpinner)
        monitoringLabel = v.findViewById(R.id.monitoringCaption)
        monitoringSpinner = v.findViewById(R.id.monitoringgSpinner)

        startTimeSetListener = OnTimeSetListener { view, hourOfDay, minute ->
            cal!!.time = startField!!.date
            cal!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal!!.set(Calendar.MINUTE, minute)
            startField!!.setTime(cal!!.time)
        }
        stopTimeSetListener = OnTimeSetListener { view, hourOfDay, minute ->
            cal!!.time = stopField!!.date
            cal!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal!!.set(Calendar.MINUTE, minute)
            stopField!!.setTime(cal!!.time)
        }

        stopField = v.findViewById(R.id.stopField)
        val cancelButton = v.findViewById<Button>(R.id.buttonCancel)
        val okButton = v.findViewById<Button>(R.id.buttonOk)
        channelField = v.findViewById(R.id.channelField)

        dateField!!.setOnClickListener(this)
        startField!!.setOnClickListener(this)
        stopField!!.setOnClickListener(this)
        cancelButton.setOnClickListener(this)
        okButton.setOnClickListener(this)

        dateField!!.setOnLongClickListener(this)
        startField!!.setOnLongClickListener(this)
        stopField!!.setOnLongClickListener(this)
        return v
    }

    fun setOnTimerEditedListener(onTimerEditedListener: OnTimerEditedListener) {
        this.mOntimeredEditedListener = onTimerEditedListener
    }

    /* (non-Javadoc)
	 * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget.DatePicker, int, int, int)
	 */
    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val cal = GregorianCalendar.getInstance()
        cal.time = dateField!!.date
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthOfYear)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        dateField!!.date = cal.time
    }

    /* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
    override fun onClick(v: View) {
        val f: DateDialogFragment
        when (v.id) {
            R.id.dateField -> {
                f = DateDialogFragment.newInstance(context, this@TimerDetails, dateField!!.date)
                f.show(activity!!.supportFragmentManager, "datepicker")
            }
            R.id.startField -> {
                f = DateDialogFragment.newInstance(context, startTimeSetListener, startField!!.date)
                f.show(activity!!.supportFragmentManager, "startTimePicker")
            }
            R.id.stopField -> {
                f = DateDialogFragment.newInstance(context, stopTimeSetListener, stopField!!.date)
                f.show(activity!!.supportFragmentManager, "stopTimePicker")
            }
            R.id.buttonCancel -> {
                if (mOntimeredEditedListener != null) {
                    mOntimeredEditedListener!!.timerEdited(null)
                }
                dismiss()
            }
            R.id.buttonOk -> {
                calcStartEnd()
                timer!!.title = titleField!!.text.toString()
                timer!!.timerAction = postRecordSpinner!!.selectedItemPosition
                timer!!.monitorPDC = monitoringSpinner!!.selectedItemPosition
                timer!!.pre = NumberUtils.toInt(preField!!.text!!.toString())
                timer!!.post = NumberUtils.toInt(postField!!.text!!.toString())
                if (activeBox!!.isChecked) {
                    timer!!.unsetFlag(Timer.FLAG_DISABLED)
                } else {
                    timer!!.setFlag(Timer.FLAG_DISABLED)
                }
                mOntimeredEditedListener?.timerEdited(timer)
                if (dialog != null && dialog!!.isShowing) {
                    dismiss()
                }
            }

            else -> {
            }
        }
    }

    private fun calcStartEnd() {
        val day = GregorianCalendar.getInstance()
        val start = GregorianCalendar.getInstance()
        val end = GregorianCalendar.getInstance()
        day.time = dateField!!.date
        start.time = startField!!.date
        start.set(Calendar.DAY_OF_YEAR, day.get(Calendar.DAY_OF_YEAR))
        end.time = stopField!!.date
        end.set(Calendar.DAY_OF_YEAR, day.get(Calendar.DAY_OF_YEAR))
        if (end.before(start)) {
            end.add(Calendar.DAY_OF_YEAR, 1)
        }
        start.set(Calendar.DAY_OF_YEAR, day.get(Calendar.DAY_OF_YEAR))
        timer!!.start = start.time
        timer!!.end = end.time
    }

    private fun bundleToTimer(bundle: Bundle) {
        timer = Timer()
        timer!!.id = bundle.getLong(EXTRA_ID, 0)
        timer!!.title = bundle.getString(EXTRA_TITLE)
        timer!!.channelName = bundle.getString(EXTRA_CHANNEL_NAME)
        timer!!.channelId = bundle.getLong(EXTRA_CHANNEL_ID, 0)
        timer!!.start = Date(bundle.getLong(EXTRA_START, System.currentTimeMillis()))
        timer!!.end = Date(bundle.getLong(EXTRA_END, System.currentTimeMillis()))
        timer!!.timerAction = bundle.getInt(EXTRA_ACTION, 0)
        timer!!.pre = bundle.getInt(EXTRA_PRE, 5)
        timer!!.post = bundle.getInt(EXTRA_POST, 5)
        timer!!.eventId = bundle.getString(EXTRA_EVENT_ID)
        timer!!.pdc = bundle.getString(EXTRA_PDC)
        timer!!.adjustPAT = bundle.getInt(EXTRA_ADJUST_PAT, -1)
        timer!!.allAudio = bundle.getInt(EXTRA_ALL_AUDIO, -1)
        timer!!.dvbSubs = bundle.getInt(EXTRA_DVB_SUBS, -1)
        timer!!.teletext = bundle.getInt(EXTRA_TELETEXT, -1)
        timer!!.eitEPG = bundle.getInt(EXTRA_EIT_EPG, -1)
        timer!!.monitorPDC = bundle.getInt(EXTRA_MONITOR_PDC, -1)
        timer!!.runningStatusSplit = bundle.getInt(EXTRA_STATUS_SPLIT, -1)
        if (!bundle.getBoolean(EXTRA_ACTIVE)) {
            timer!!.setFlag(Timer.FLAG_DISABLED)
        }
    }

    /* (non-Javadoc)
     * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
     */
    override fun onLongClick(v: View): Boolean {
        return true
    }

    /**
     * The listener interface for receiving onTimerEdited events.
     * The class that is interested in processing a onTimerEdited
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's `addOnTimerEditedListener` method. When
     * the onTimerEdited event occurs, that object's appropriate
     * method is invoked.
     *
     * @author RayBa
    `` */
    interface OnTimerEditedListener {

        /**
         * Timer edited.
         *
         * @param timer the Timer which has been edited
         */
        fun timerEdited(timer: Timer?)

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val parentFragment = targetFragment
        if (parentFragment is DialogInterface.OnDismissListener) {
            (parentFragment as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
    }

    companion object {

        val TIMER_RESULT = 0
        val RESULT_CHANGED = 1

        val EXTRA_ID = "_id"
        val EXTRA_TITLE = "_title"
        val EXTRA_CHANNEL_NAME = "_channel_name"
        val EXTRA_CHANNEL_ID = "_channel_id"
        val EXTRA_START = "_start"
        val EXTRA_END = "_end"
        val EXTRA_ACTION = "_action"
        val EXTRA_ACTIVE = "_active"
        val EXTRA_PRE = "_pre"
        val EXTRA_POST = "_post"
        val EXTRA_EVENT_ID = "_event_id"
        val EXTRA_PDC = "_pdc"
        val EXTRA_ADJUST_PAT = "AdjustPAT"
        val EXTRA_ALL_AUDIO = "AllAudio"
        val EXTRA_DVB_SUBS = "DVBSubs"
        val EXTRA_TELETEXT = "Teletext"
        val EXTRA_EIT_EPG = "EITEPG"
        val EXTRA_MONITOR_PDC = "MonitorPDC"
        val EXTRA_STATUS_SPLIT = "RunningStatusSplit"

        /**
         * New instance.
         *
         * @return the timer details©
         */
        fun newInstance(): TimerDetails {
            return TimerDetails()
        }

        fun buildBundle(timer: Timer): Bundle {
            val bundle = Bundle()
            timerToBundle(timer, bundle)
            return bundle
        }

        private fun timerToBundle(timer: Timer, bundle: Bundle) {
            bundle.putLong(EXTRA_ID, timer.id)
            bundle.putString(EXTRA_TITLE, timer.title)
            bundle.putString(EXTRA_CHANNEL_NAME, timer.channelName)
            bundle.putLong(EXTRA_CHANNEL_ID, timer.channelId)
            bundle.putLong(EXTRA_START, timer.start!!.time)
            bundle.putLong(EXTRA_END, timer.end!!.time)
            bundle.putInt(EXTRA_ACTION, timer.timerAction)
            bundle.putInt(EXTRA_PRE, timer.pre)
            bundle.putInt(EXTRA_POST, timer.post)
            bundle.putString(EXTRA_EVENT_ID, timer.eventId)
            bundle.putString(EXTRA_PDC, timer.pdc)
            bundle.putString(EXTRA_PDC, timer.pdc)
            bundle.putInt(EXTRA_ADJUST_PAT, timer.adjustPAT)
            bundle.putInt(EXTRA_ALL_AUDIO, timer.allAudio)
            bundle.putInt(EXTRA_DVB_SUBS, timer.dvbSubs)
            bundle.putInt(EXTRA_TELETEXT, timer.teletext)
            bundle.putInt(EXTRA_EIT_EPG, timer.eitEPG)
            bundle.putInt(EXTRA_MONITOR_PDC, timer.monitorPDC)
            bundle.putInt(EXTRA_STATUS_SPLIT, timer.runningStatusSplit)
            bundle.putBoolean(EXTRA_ACTIVE, !timer.isFlagSet(Timer.FLAG_DISABLED))
        }
    }

}
