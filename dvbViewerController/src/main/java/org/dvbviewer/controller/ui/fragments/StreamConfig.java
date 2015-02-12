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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.dvbviewer.controller.App;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class StreamConfig.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class StreamConfig extends DialogFragment implements OnClickListener, DialogInterface.OnClickListener, OnItemSelectedListener {

	public static final String	TAG_URI					= "_uri";
	public static final String	EXTRA_FILE_ID			= "_fileID";
	public static final String	EXTRA_FILE_TYPE			= "_fileType";
	public static final String	EXTRA_STREAM_TYPE		= "_streamType";
	public static final String	EXTRA_DIALOG_TITLE_RES	= "_dialog_title_res";
	public static final int		FILE_TYPE_LIVE			= 0;
	public static final int		FILE_TYPE_RECORDING		= 1;
	public static final int		STREAM_TYPE_DIRECT		= 0;
	public static final int		STREAM_TYPE_TRANSCODE	= 1;
	private String				flashUrl				= ServerConsts.REC_SERVICE_URL + ServerConsts.URL_FLASHSTREAM;
	private String				liveUrl					= "http://"+ServerConsts.REC_SERVICE_HOST + ":" + ServerConsts.REC_SERVICE_LIVE_STREAM_PORT + "/upnp/channelstream/";
	private String				mediaUrl				= "http://"+ServerConsts.REC_SERVICE_HOST + ":" + ServerConsts.REC_SERVICE_MEDIA_STREAM_PORT + "/upnp/recordings/";
	private Spinner				qualitySpinner;
	private Spinner				aspectSpinner;
	private Spinner				ffmpegSpinner;
	private Spinner				widthSpinner;
	private Spinner				heightSpinner;
	private Button				startButton;
	private Button				startDirectStreamButton;
	private EditText			startHours;
	private EditText			startMinutes;
	private EditText			startSeconds;
	private int					title					= 0;
	boolean						seekable				= false;
	String						preTime;
	private int					mFileType				= 0;
	private int					mStreamType				= 0;
	private int					mFileId					= -1;
	private Context				mContext;
	
	private SharedPreferences	prefs;

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		DVBViewerPreferences dvbvPrefs = new DVBViewerPreferences(mContext);
		prefs = dvbvPrefs.getStreamPrefs();
		if (savedInstanceState != null) {
			title = savedInstanceState.getInt("titleRes");
		}
		mFileId = getArguments().getInt(EXTRA_FILE_ID);
		mFileType = getArguments().getInt(EXTRA_FILE_TYPE, FILE_TYPE_LIVE);
		mStreamType = getArguments().getInt(EXTRA_FILE_TYPE, STREAM_TYPE_DIRECT);
		seekable = mFileType != FILE_TYPE_LIVE && mStreamType != STREAM_TYPE_DIRECT;
		if (seekable) {
			DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
			preTime = String.valueOf(prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, 0));
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dia = super.onCreateDialog(savedInstanceState);
		dia.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		dia.setTitle(R.string.streamConfig);
		return dia;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		qualitySpinner.requestFocus();
		startHours.clearFocus();
		super.onViewCreated(view, savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		if (getDialog() != null && title > 0) {
			getDialog().setTitle(title);
		}
		startHours.clearFocus();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_stream_config, container, false);
		qualitySpinner = (Spinner) v.findViewById(R.id.qualitySpinner);
		int qualityIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_QUALITY, 7);
		qualitySpinner.setOnItemSelectedListener(this);
		qualitySpinner.setSelection(qualityIndex);
		
		aspectSpinner = (Spinner) v.findViewById(R.id.aspectSpinner);
		int aspectIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_ASPECT_RATIO, 0);
		aspectSpinner.setSelection(aspectIndex);
		aspectSpinner.setOnItemSelectedListener(this);
		
		ffmpegSpinner = (Spinner) v.findViewById(R.id.ffmpegSpinner);
		int ffmpegIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_FFMPEG_PRESET, 5);
		ffmpegSpinner.setSelection(ffmpegIndex);
		ffmpegSpinner.setOnItemSelectedListener(this);
		
		widthSpinner = (Spinner) v.findViewById(R.id.widthSpinner);
		int widthIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_MAX_WIDTH, 0);
		widthSpinner.setSelection(widthIndex);
		widthSpinner.setOnItemSelectedListener(this);
		
		heightSpinner = (Spinner) v.findViewById(R.id.heightSpinner);
		int heightIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_MAX_HEIGHT, 0);
		heightSpinner.setSelection(heightIndex);
		heightSpinner.setOnItemSelectedListener(this);
		
		
		startButton = (Button) v.findViewById(R.id.startTranscodedButton);
		startButton.setOnClickListener(this);
		startDirectStreamButton = (Button) v.findViewById(R.id.startDirectButton);
		startDirectStreamButton.setOnClickListener(this);
		startHours = (EditText) v.findViewById(R.id.stream_hours);
		startMinutes = (EditText) v.findViewById(R.id.stream_minutes);
		startSeconds = (EditText) v.findViewById(R.id.stream_seconds);
		View positionContainer = v.findViewById(R.id.streamPositionContainer);

		/**
		 * Hide Position Row if streaming non seekable content
		 */
		if (!seekable) {
			positionContainer.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(preTime)) {
			startMinutes.setText(preTime);
		}
		qualitySpinner.requestFocus();
		return v;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putInt("titleRes", title);
	}

	/**
	 * New instance.
	 *
	 * @return the stream config©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static StreamConfig newInstance() {
		StreamConfig config = new StreamConfig();
		return config;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Intent videoIntent = null;
		switch (v.getId()) {
		case R.id.startTranscodedButton:
			prefs.edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, false).commit();
			mStreamType = STREAM_TYPE_TRANSCODE;
			videoIntent = getVideoIntent();
			try {
				startActivity(videoIntent);
				if (UIUtils.isTablet(getActivity())) {
					getDialog().dismiss();
				} else {
					getActivity().finish();
				}
				Tracker t = ((App) getActivity().getApplication()).getTracker();
				// Build and send an Event.
				t.send(new HitBuilders.EventBuilder()
						.setCategory("Streaming")
						.setAction("Transcoded stream")
						.build());
			} catch (ActivityNotFoundException e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(getResources().getString(R.string.noFlashPlayerFound)).setPositiveButton(getResources().getString(R.string.yes), this).setNegativeButton(getResources().getString(R.string.no), this).show();
				e.printStackTrace();
			}
			break;
		case R.id.startDirectButton:
			prefs.edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true).commit();
			mStreamType = STREAM_TYPE_DIRECT;
			videoIntent = getVideoIntent();
			try {
				startActivity(videoIntent);
				if (UIUtils.isTablet(getActivity())) {
					getDialog().dismiss();
				} else {
					getActivity().finish();
				}
				Tracker t = ((App) getActivity().getApplication()).getTracker();
				// Build and send an Event.
				t.send(new HitBuilders.EventBuilder()
						.setCategory("Streaming")
						.setAction("Direct stream")
						.build());
			} catch (ActivityNotFoundException e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(getResources().getString(R.string.noFlashPlayerFound)).setPositiveButton(getResources().getString(R.string.yes), this).setNegativeButton(getResources().getString(R.string.no), this).show();

				e.printStackTrace();
			}
			break;

		default:
			break;
		}
	}

	/**
	 * Gets the video intent.
	 *
	 * @return the video intent
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private Intent getVideoIntent() {
		String videoUrl = null;
		String videoType = null;
		switch (mStreamType) {
		case STREAM_TYPE_DIRECT:
			switch (mFileType) {
			case FILE_TYPE_LIVE:
				videoUrl = liveUrl + mFileId + ".ts";
				break;
			case FILE_TYPE_RECORDING:
				videoUrl = mediaUrl + mFileId + ".ts";
				break;

			default:
				break;
			}
			videoType = "video/*";

			break;
		case STREAM_TYPE_TRANSCODE:
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Preset", String.valueOf(qualitySpinner.getSelectedItemPosition())));
			params.add(new BasicNameValuePair("aspect", aspectSpinner.getSelectedItem().toString()));
			params.add(new BasicNameValuePair("ffPreset", ffmpegSpinner.getSelectedItem().toString()));

			/**
			 * Check if height is set from user, otherwise the the default values are used
			 */
			if (widthSpinner.getSelectedItemPosition() > 0) {
				params.add(new BasicNameValuePair("maxwidth", widthSpinner.getSelectedItem().toString()));
			}
			if (heightSpinner.getSelectedItemPosition() > 0) {
				params.add(new BasicNameValuePair("maxheight", heightSpinner.getSelectedItem().toString()));
			}

			/**
			 * Calculate startposition in seconds
			 */
			int hours = TextUtils.isEmpty(startHours.getText()) ? 0 : Integer.valueOf(startHours.getText().toString());
			int minutes = TextUtils.isEmpty(startMinutes.getText()) ? 0 : Integer.valueOf(startMinutes.getText().toString());
			int seconds = TextUtils.isEmpty(startSeconds.getText()) ? 0 : Integer.valueOf(startSeconds.getText().toString());
			int start = 3600 * hours + 60 * minutes + seconds;
			params.add(new BasicNameValuePair("start", String.valueOf(start)));

			switch (mFileType) {
			case FILE_TYPE_LIVE:
				params.add(new BasicNameValuePair("chid", String.valueOf(mFileId)));
				break;
			case FILE_TYPE_RECORDING:
				params.add(new BasicNameValuePair("recid", String.valueOf(mFileId)));
				break;

			default:
				break;
			}
			String query = URLEncodedUtils.format(params, "utf-8");
			videoUrl = flashUrl + query;
			break;

		default:
			break;
		}
		Log.i(StreamConfig.class.getSimpleName(), "url: " + videoUrl);

		Intent videoIntent;
		videoType = "video/mpeg";
		videoIntent = new Intent(Intent.ACTION_VIEW);
		videoIntent.setDataAndType(Uri.parse(videoUrl), videoType);
		return videoIntent;
	}

	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			Editor editor = prefs.edit();
			editor.putBoolean("stream_external", false);
			editor.commit();
			onClick(startButton);
			if (UIUtils.isTablet(getActivity())) {
				getDialog().dismiss();
			} else {
				getActivity().finish();
			}
			break;

		default:
			break;
		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Editor editor = prefs.edit();
		switch (parent.getId()) {
		case R.id.qualitySpinner:
			editor.putInt(DVBViewerPreferences.KEY_STREAM_QUALITY, position);
			break;
		case R.id.aspectSpinner:
			editor.putInt(DVBViewerPreferences.KEY_STREAM_ASPECT_RATIO, position);
			break;
		case R.id.ffmpegSpinner:
			editor.putInt(DVBViewerPreferences.KEY_STREAM_FFMPEG_PRESET, position);
			break;
		case R.id.widthSpinner:
			editor.putInt(DVBViewerPreferences.KEY_STREAM_MAX_WIDTH, position);
			break;
		case R.id.heightSpinner:
			editor.putInt(DVBViewerPreferences.KEY_STREAM_MAX_HEIGHT, position);
			break;

		default:
			break;
		}
		editor.commit();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	
	public static String buildLiveUrl(Context context, int position){
		DVBViewerPreferences dvbPrefs = new DVBViewerPreferences(context);
		return buildLiveUrl(context, dvbPrefs.getStreamPrefs(), position);
	}
	
	private static String buildLiveUrl(Context context, SharedPreferences prefs, int position){
		boolean direct = prefs.getBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true);
		StringBuffer result = new StringBuffer();
		if (direct) {
			result.append("http://"+ServerConsts.REC_SERVICE_HOST + ":" + ServerConsts.REC_SERVICE_LIVE_STREAM_PORT + "/upnp/channelstream/");
			result.append(position+".ts");
		}else {
			result.append(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_FLASHSTREAM);
			int qualityIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_QUALITY, 7);
			int aspectIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_ASPECT_RATIO, 0);
			int ffmpegIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_FFMPEG_PRESET, 5);
			int widthIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_MAX_WIDTH, 0);
			int heightIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_MAX_HEIGHT, 0);
			String quality = String.valueOf(qualityIndex);
			String aspect = context.getResources().getStringArray(R.array.aspect)[aspectIndex];
			String ffmpeg = context.getResources().getStringArray(R.array.ffmpegPresets)[ffmpegIndex];
			String width = context.getResources().getStringArray(R.array.width)[widthIndex];
			String height = context.getResources().getStringArray(R.array.height)[heightIndex];
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Preset" ,quality));
			params.add(new BasicNameValuePair("aspect", aspect));
			params.add(new BasicNameValuePair("ffPreset", ffmpeg));
			/**
			 * Check if height is set from user, otherwise the the default values are used
			 */
			if (widthIndex > 0) {
				params.add(new BasicNameValuePair("maxwidth", width));
			}
			if (heightIndex > 0) {
				params.add(new BasicNameValuePair("maxheight", height));
			}
			params.add(new BasicNameValuePair("chid", String.valueOf(position)));
			String query = URLEncodedUtils.format(params, "utf-8");
			result.append(query);
		}
		return result.toString();
	}
	

}
