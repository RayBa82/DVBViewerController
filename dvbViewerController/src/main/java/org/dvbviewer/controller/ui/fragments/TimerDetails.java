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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.apache.commons.lang3.StringUtils;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.io.HTTPUtil;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.UrlBuilderException;
import org.dvbviewer.controller.ui.base.BaseActivity;
import org.dvbviewer.controller.ui.base.BaseDialogFragment;
import org.dvbviewer.controller.ui.widget.DateField;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.ServerConsts;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * The Class TimerDetails.
 *
 * @author RayBa
 */
public class TimerDetails extends BaseDialogFragment implements OnDateSetListener, OnClickListener, OnLongClickListener {

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
	public static final String		EXTRA_PRE			= "_pre";
	public static final String		EXTRA_POST			= "_post";
    public static final String		EXTRA_EVENT_ID		= "_event_id";
    public static final String		EXTRA_PDC   		= "_pdc";
    public static final String		EXTRA_ADJUST_PAT	= "AdjustPAT";
    public static final String		EXTRA_ALL_AUDIO		= "AllAudio";
    public static final String		EXTRA_DVB_SUBS		= "DVBSubs";
    public static final String		EXTRA_TELETEXT		= "Teletext";
    public static final String		EXTRA_EIT_EPG		= "EITEPG";
    public static final String		EXTRA_MONITOR_PDC	= "MonitorPDC";
    public static final String		EXTRA_STATUS_SPLIT	= "RunningStatusSplit";

	private Timer					timer;
	private TextView				channelField;
	private TextView				titleField;
	private SwitchCompat            activeBox;
	private DateField				dateField;
	private DateField				startField;
	private DateField				stopField;
	private OnTimeSetListener		startTimeSetListener;
	private OnTimeSetListener		stopTimeSetListener;
	private Calendar				cal;
	private Spinner					postRecordSpinner;
	private OnTimerEditedListener	mOntimeredEditedListener;
    private Spinner monitoringSpinner;
    private TextView monitoringLabel;


    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cal = GregorianCalendar.getInstance();
		if (savedInstanceState == null) {
            bundleToTimer(getArguments());
		}else if (savedInstanceState != null) {
            bundleToTimer(savedInstanceState);
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
	 */
	public static TimerDetails newInstance() {
		return new TimerDetails();
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
			final boolean invalidindex = timer.getTimerAction() >= postRecordSpinner.getCount();
			postRecordSpinner.setSelection(invalidindex ? 0 : timer.getTimerAction());
			if (!TextUtils.isEmpty(timer.getChannelName())) {
				channelField.setText(timer.getChannelName());
			}
            if(StringUtils.isNotBlank(timer.getPdc())){
                monitoringSpinner.setSelection(timer.getMonitorPDC());
            }else{
                monitoringLabel.setVisibility(View.GONE);
                monitoringSpinner.setVisibility(View.GONE);
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
        timerToBundle(timer, arg0);
	}



	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_timer_details, container, false);
		titleField = (TextView) v.findViewById(R.id.titleField);
		dateField = (DateField) v.findViewById(R.id.dateField);
		activeBox = (SwitchCompat) v.findViewById(R.id.activeBox);
		startField = (DateField) v.findViewById(R.id.startField);
		postRecordSpinner = (Spinner) v.findViewById(R.id.postRecordingSpinner);
        monitoringLabel = (TextView) v.findViewById(R.id.monitoringCaption);
        monitoringSpinner = (Spinner) v.findViewById(R.id.monitoringgSpinner);

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
		Button cancelButton = (Button) v.findViewById(R.id.buttonCancel);
		Button okButton = (Button) v.findViewById(R.id.buttonOk);
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

    public void setOnTimerEditedListener(OnTimerEditedListener onTimerEditedListener) {
        this.mOntimeredEditedListener = onTimerEditedListener;
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
            timer.setMonitorPDC(monitoringSpinner.getSelectedItemPosition());
			if (activeBox.isChecked()){
				timer.unsetFlag(Timer.FLAG_DISABLED);
			}else{
				timer.setFlag(Timer.FLAG_DISABLED);
			}
			String query = buildTimerUrl(timer);
            Callback callback = getCallback();
            ServerRequest.executeAsync(query, callback);

			break;

		default:
			break;
		}
	}

    @NonNull
    private Callback getCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (getDialog() != null && getDialog().isShowing()) {
                        dismiss();
                    }
                    HTTPUtil.checkResponse(response);
                    if (mOntimeredEditedListener != null) {
                        mOntimeredEditedListener.timerEdited(true);
                    }
                } catch (Exception e) {
                    catchException(e);
                }
            }
        };
    }

    @Nullable
	public static String buildTimerUrl(Timer timer) {
		final HTTPUtil.UrlBuilder builder;
		try {
			builder = HTTPUtil.getUrlBuilder(ServerConsts.REC_SERVICE_URL + (timer.getId() < 0l ? ServerConsts.URL_TIMER_CREATE : ServerConsts.URL_TIMER_EDIT));
			final String title = timer.getTitle();
            final String days = String.valueOf(DateUtils.getDaysSinceDelphiNull(timer.getStart()));
            final String start = String.valueOf(DateUtils.getMinutesOfDay(timer.getStart()));
            final String stop = String.valueOf(DateUtils.getMinutesOfDay(timer.getEnd()));
            final String endAction = String.valueOf(timer.getTimerAction());
            final String pre = String.valueOf(timer.getPre());
            final String post  = String.valueOf(timer.getPost());
			builder.addQueryParameter("ch", String.valueOf(timer.getChannelId()));
			builder.addQueryParameter("dor", days);
			builder.addQueryParameter("encoding", "255");
			builder.addQueryParameter("enable", timer.isFlagSet(Timer.FLAG_DISABLED) ? "0" : "1");
			builder.addQueryParameter("start", start);
			builder.addQueryParameter("stop", stop);
			builder.addQueryParameter("title", title);
			builder.addQueryParameter("endact", endAction);
            builder.addQueryParameter("pre", pre);
            builder.addQueryParameter("post", post);
            addIfNotEmpty("pdc", timer.getPdc(), builder);
            addIfPositive("audio", timer.getAllAudio(), builder);
            addIfPositive("subs", timer.getDvbSubs(), builder);
            addIfPositive("ttx", timer.getTeletext(), builder);
            addIfPositive("eit", timer.getEitEPG(), builder);
            final int epgMonitoring = timer.getMonitorPDC() - 1;
            if(epgMonitoring >= 0){
                builder.addQueryParameter("monitorpdc", "1");
                builder.addQueryParameter("monforrec", String.valueOf(epgMonitoring));
            }
			if (timer.getId() >= 0) {
				builder.addQueryParameter("id", String.valueOf(timer.getId()));
			}
			return builder.build().toString();
		} catch (UrlBuilderException e) {
			e.printStackTrace();
		}
		return null;
	}

    private static HTTPUtil.UrlBuilder addIfPositive(String name, int value,  HTTPUtil.UrlBuilder builder){
        if(value > 0){
            builder.addQueryParameter(name, String.valueOf(value));
        }
        return builder;
    }

    private static HTTPUtil.UrlBuilder addIfNotEmpty(String name, String value,  HTTPUtil.UrlBuilder builder){
        if(StringUtils.isNotBlank(value)){
            builder.addQueryParameter(name, value);
        }
        return builder;
    }

	@NonNull
	public static Bundle buildBundle(Timer timer) {
        Bundle bundle = new Bundle();
        timerToBundle(timer, bundle);
		return bundle;
	}

    private void bundleToTimer(Bundle bundle) {
        timer = new Timer();
        timer.setId(bundle.getLong(EXTRA_ID, 0));
        timer.setTitle(bundle.getString(EXTRA_TITLE));
        timer.setChannelName(bundle.getString(EXTRA_CHANNEL_NAME));
        timer.setChannelId(bundle.getLong(EXTRA_CHANNEL_ID, 0));
        timer.setStart(new Date(bundle.getLong(EXTRA_START, System.currentTimeMillis())));
        timer.setEnd(new Date(bundle.getLong(EXTRA_END, System.currentTimeMillis())));
        timer.setTimerAction(bundle.getInt(EXTRA_ACTION, 0));
        timer.setPre(bundle.getInt(EXTRA_PRE, 5));
        timer.setPost(bundle.getInt(EXTRA_POST, 5));
        timer.setEventId(bundle.getString(EXTRA_EVENT_ID));
        timer.setPdc(bundle.getString(EXTRA_PDC));
        timer.setAdjustPAT(bundle.getInt(EXTRA_ADJUST_PAT, -1));
        timer.setAllAudio(bundle.getInt(EXTRA_ALL_AUDIO, -1));
        timer.setDvbSubs(bundle.getInt(EXTRA_DVB_SUBS, -1));
        timer.setTeletext(bundle.getInt(EXTRA_TELETEXT, -1));
        timer.setEitEPG(bundle.getInt(EXTRA_EIT_EPG, -1));
        timer.setMonitorPDC(bundle.getInt(EXTRA_MONITOR_PDC, -1));
        timer.setRunningStatusSplit(bundle.getInt(EXTRA_STATUS_SPLIT, -1));
        if (!bundle.getBoolean(EXTRA_ACTIVE)) {
            timer.setFlag(Timer.FLAG_DISABLED);
        }
    }

    @NonNull
    private static void timerToBundle(Timer timer, Bundle bundle) {
        bundle.putLong(EXTRA_ID, timer.getId());
        bundle.putString(EXTRA_TITLE, timer.getTitle());
        bundle.putString(EXTRA_CHANNEL_NAME, timer.getChannelName());
        bundle.putLong(EXTRA_CHANNEL_ID, timer.getChannelId());
        bundle.putLong(EXTRA_START, timer.getStart().getTime());
        bundle.putLong(EXTRA_END, timer.getEnd().getTime());
        bundle.putInt(EXTRA_ACTION, timer.getTimerAction());
        bundle.putInt(EXTRA_PRE, timer.getPre());
        bundle.putInt(EXTRA_POST, timer.getPost());
        bundle.putString(EXTRA_EVENT_ID, timer.getEventId());
        bundle.putString(EXTRA_PDC, timer.getPdc());
        bundle.putString(EXTRA_PDC, timer.getPdc());
        bundle.putInt(EXTRA_ADJUST_PAT, timer.getAdjustPAT());
        bundle.putInt(EXTRA_ALL_AUDIO, timer.getAllAudio());
        bundle.putInt(EXTRA_DVB_SUBS, timer.getDvbSubs());
        bundle.putInt(EXTRA_TELETEXT, timer.getTeletext());
        bundle.putInt(EXTRA_EIT_EPG, timer.getEitEPG());
        bundle.putInt(EXTRA_MONITOR_PDC, timer.getMonitorPDC());
        bundle.putInt(EXTRA_STATUS_SPLIT, timer.getRunningStatusSplit());
        bundle.putBoolean(TimerDetails.EXTRA_ACTIVE, !timer.isFlagSet(Timer.FLAG_DISABLED));
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
	 */
	public interface OnTimerEditedListener{
		
		/**
		 * Timer edited.
		 *
		 * @param edited the edited
		 */
		void timerEdited(boolean edited);
		
	}
	
}
