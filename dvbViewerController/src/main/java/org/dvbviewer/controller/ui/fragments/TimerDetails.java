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
package org.dvbviewer.controller.ui.fragments;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.squareup.okhttp.HttpUrl;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.io.HTTPUtil;
import org.dvbviewer.controller.io.ServerRequest.RecordingServiceGet;
import org.dvbviewer.controller.io.UrlBuilderException;
import org.dvbviewer.controller.ui.base.BaseActivity;
import org.dvbviewer.controller.ui.widget.DateField;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.ServerConsts;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * The Class TimerDetails.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class TimerDetails extends DialogFragment implements OnDateSetListener, OnClickListener, OnLongClickListener {

	public static final int			TIMER_RESULT		= 0;
	public static final int			RESULT_CHANGED		= 1;

	public static final String		EXTRA_ID			= "_id";
	public static final String		EXTRA_TITLE			= "_title";
	public static final String		EXTRA_CHANNEL_NAME	= "_channel_name";
	public static final String		EXTRA_CHANNEL_ID	= "_channel_id";
	public static final String		EXTRA_START			= "_start";
	public static final String		EXTRA_END			= "_end";
	public static final String		EXTRA_ACTION		= "_action";
	public static final String		EXTRA_ACTIVE		= "_active";

	Timer							timer;
	private TextView				channelField;
	private TextView				titleField;
	private CheckBox				activeBox;
	private DateField				dateField;
	private DateField				startField;
	private DateField				stopField;
	private Button					cancelButton;
	private Button					okButton;
	private OnTimeSetListener		startTimeSetListener;
	private OnTimeSetListener		stopTimeSetListener;
	private Calendar				cal;
	private Spinner					postRecordSpinner;
	private OnTimerEditedListener	mOntimeredEditedListener;
	private DVBViewerPreferences	prefs;
	

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cal = GregorianCalendar.getInstance();
		Date now = new Date();
		prefs = new DVBViewerPreferences(getActivity());
		if (timer == null && savedInstanceState == null) {
			timer = new Timer();
			timer.setId(getArguments().getLong(EXTRA_ID, -1l));
			timer.setTitle(getArguments().getString(EXTRA_TITLE));
			timer.setChannelName(getArguments().getString(EXTRA_CHANNEL_NAME));
			timer.setChannelId(getArguments().getLong(EXTRA_CHANNEL_ID, 0));
			Date start = new Date(getArguments().getLong(EXTRA_START, now.getTime()));
			Date end = new Date(getArguments().getLong(EXTRA_END, now.getTime()));
			timer.setStart(start);
			timer.setEnd(end);
			if (!getArguments().getBoolean(EXTRA_ACTIVE)) {
				timer.setFlag(Timer.FLAG_DISABLED);
			}
			if (timer.getId() <= 0l) {
				timer.setTimerAction(prefs.getInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, 0));
			}else{
				timer.setTimerAction(getArguments().getInt(EXTRA_ACTION, 0));
			}
		}else if (savedInstanceState != null) {
			timer = new Timer();
			timer.setId(savedInstanceState.getLong(EXTRA_ID, 0));
			timer.setTitle(savedInstanceState.getString(EXTRA_TITLE));
			timer.setChannelName(savedInstanceState.getString(EXTRA_CHANNEL_NAME));
			timer.setChannelId(savedInstanceState.getLong(EXTRA_CHANNEL_ID, 0));
			timer.setStart(new Date(savedInstanceState.getLong(EXTRA_START, now.getTime())));
			timer.setEnd(new Date(savedInstanceState.getLong(EXTRA_END, now.getTime())));
			timer.setTimerAction(savedInstanceState.getInt(EXTRA_ACTION, 0));
			if (!savedInstanceState.getBoolean(EXTRA_ACTIVE)) {
				timer.setFlag(Timer.FLAG_DISABLED);
			}
		}

	}
	
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockDialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnTimerEditedListener) {
			mOntimeredEditedListener = (OnTimerEditedListener) activity;
		}
	}

	/**
	 * New instance.
	 *
	 * @return the timer details©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static TimerDetails newInstance() {
		TimerDetails frag = new TimerDetails();
		return frag;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		if (activity instanceof BaseActivity){
			BaseActivity a = (BaseActivity) activity;
			a.setSubTitle(R.string.details);
		}
		if (timer != null) {
			titleField.setText(timer.getTitle());
			dateField.setDate(timer.getStart());
			activeBox.setChecked(!timer.isFlagSet(Timer.FLAG_DISABLED));
			startField.setTime(timer.getStart());
			stopField.setTime(timer.getEnd());
			postRecordSpinner.setSelection(timer.getTimerAction());
			if (!TextUtils.isEmpty(timer.getChannelName())) {
				channelField.setText(timer.getChannelName());
//				imageCahcer.getImage(channelLogo, ServerConsts.URL_CHANNEL_LOGO + URLEncoder.encode(timer.getChannelName()), null);
			}
		}
		if (getDialog() != null) {
			getDialog().setTitle(timer != null && timer.getId() <= 0 ? R.string.createTimer : R.string.editTimer);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putLong(EXTRA_ID, timer.getId());
		arg0.putString(EXTRA_TITLE, timer.getTitle());
		arg0.putString(EXTRA_CHANNEL_NAME, timer.getChannelName());
		arg0.putLong(EXTRA_CHANNEL_ID, timer.getChannelId());
		arg0.putLong(EXTRA_START, timer.getStart().getTime());
		arg0.putLong(EXTRA_END, timer.getEnd().getTime());
		arg0.putInt(EXTRA_ACTION, timer.getTimerAction());
		arg0.putBoolean(EXTRA_ACTIVE, !timer.isFlagSet(Timer.FLAG_DISABLED));
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_timer_details, container, false);
		titleField = (TextView) v.findViewById(R.id.titleField);
		dateField = (DateField) v.findViewById(R.id.dateField);
		activeBox = (CheckBox) v.findViewById(R.id.activeBox);
		startField = (DateField) v.findViewById(R.id.startField);
		postRecordSpinner = (Spinner) v.findViewById(R.id.postRecordingSpinner);

		startTimeSetListener = new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				cal.setTime(startField.getDate());
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minute);
				startField.setTime(cal.getTime());
			}
		};
		stopTimeSetListener = new OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				cal.setTime(stopField.getDate());
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minute);
				stopField.setTime(cal.getTime());
			}
		};

		stopField = (DateField) v.findViewById(R.id.stopField);
		cancelButton = (Button) v.findViewById(R.id.buttonCancel);
		okButton = (Button) v.findViewById(R.id.buttonOk);
		channelField = (TextView) v.findViewById(R.id.channelField);

		dateField.setOnClickListener(this);
		startField.setOnClickListener(this);
		stopField.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		okButton.setOnClickListener(this);

		dateField.setOnLongClickListener(this);
		startField.setOnLongClickListener(this);
		stopField.setOnLongClickListener(this);
		return v;
	}

	/**
	 * Gets the timer.
	 *
	 * @return the timer
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Timer getTimer() {
		return timer;
	}

	/**
	 * Sets the timer.
	 *
	 * @param timer the new timer
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	/* (non-Javadoc)
	 * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget.DatePicker, int, int, int)
	 */
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(dateField.getDate());
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, monthOfYear);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		dateField.setDate(cal.getTime());
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		DateDialogFragment f;
		switch (v.getId()) {
		case R.id.dateField:
			f = DateDialogFragment.newInstance(getActivity(), TimerDetails.this, dateField.getDate());
			f.show(getActivity().getSupportFragmentManager(), "datepicker");
			break;
		case R.id.startField:
			f = DateDialogFragment.newInstance(getActivity(), startTimeSetListener, startField.getDate());
			f.show(getActivity().getSupportFragmentManager(), "startTimePicker");
			break;
		case R.id.stopField:
			f = DateDialogFragment.newInstance(getActivity(), stopTimeSetListener, stopField.getDate());
			f.show(getActivity().getSupportFragmentManager(), "stopTimePicker");
			break;
		case R.id.buttonCancel:
			if (mOntimeredEditedListener != null) {
				mOntimeredEditedListener.timerEdited(false);
			}
			dismiss();
			break;
		case R.id.buttonOk:
			timer.setStart(startField.getDate());
			timer.setEnd(stopField.getDate());
			timer.setTitle(titleField.getText().toString());
			timer.setTimerAction(postRecordSpinner.getSelectedItemPosition());
			if (activeBox.isChecked()){
				timer.unsetFlag(Timer.FLAG_DISABLED);
			}else{
				timer.setFlag(Timer.FLAG_DISABLED);
			}
			String query = buildTimerUrl(timer);
			RecordingServiceGet rsGet = new RecordingServiceGet(query);
			Thread executionThread = new Thread(rsGet);
			executionThread.start();
			if (mOntimeredEditedListener != null) {
				mOntimeredEditedListener.timerEdited(true);
			}
			if (getDialog() != null && getDialog().isShowing()) {
				dismiss();
			}
			break;

		default:
			break;
		}
	}

	@Nullable
	public static String buildTimerUrl(Timer timer) {
		final HttpUrl.Builder builder;
		try {
			builder = HTTPUtil.getUrlBuilder(ServerConsts.REC_SERVICE_URL + (timer.getId() < 0l ? ServerConsts.URL_TIMER_CREATE : ServerConsts.URL_TIMER_EDIT));
			String title = timer.getTitle();
			String days = String.valueOf(DateUtils.getDaysSinceDelphiNull(timer.getStart()));
			String start = String.valueOf(DateUtils.getMinutesOfDay(timer.getStart()));
			String stop = String.valueOf(DateUtils.getMinutesOfDay(timer.getEnd()));
			String endAction = String.valueOf(timer.getTimerAction());
			builder.addQueryParameter("ch", String.valueOf(timer.getChannelId()));
			builder.addQueryParameter("dor", days);
			builder.addQueryParameter("encoding", "255");
			builder.addQueryParameter("enable", timer.isFlagSet(Timer.FLAG_DISABLED) ? "0" : "1");
			builder.addQueryParameter("start", start);
			builder.addQueryParameter("stop", stop);
			builder.addQueryParameter("title", title);
			builder.addQueryParameter("endact", endAction);
			if (timer.getId() >= 0) {
				builder.addQueryParameter("id", String.valueOf(timer.getId()));
			}
			return builder.build().toString();
		} catch (UrlBuilderException e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		return true;
	}

	/**
	 * The listener interface for receiving onTimerEdited events.
	 * The class that is interested in processing a onTimerEdited
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addOnTimerEditedListener<code> method. When
	 * the onTimerEdited event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public interface OnTimerEditedListener{
		
		/**
		 * Timer edited.
		 *
		 * @param edited the edited
		 * @author RayBa
		 * @date 07.04.2013
		 */
		void timerEdited(boolean edited);
		
	}
	
}
