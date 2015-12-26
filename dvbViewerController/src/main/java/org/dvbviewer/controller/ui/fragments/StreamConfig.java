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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.squareup.okhttp.HttpUrl;

import org.dvbviewer.controller.App;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.FfMpegPrefs;
import org.dvbviewer.controller.entities.Preset;
import org.dvbviewer.controller.io.AuthenticationException;
import org.dvbviewer.controller.io.DefaultHttpException;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.data.FFMPEGPrefsHandler;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;
import org.xml.sax.SAXException;

import java.util.List;


/**
 * The Class StreamConfig.
 *
 * @author RayBa
 */
public class StreamConfig extends DialogFragment implements OnClickListener, DialogInterface.OnClickListener, OnItemSelectedListener {

	private static final String	Tag						= StreamConfig.class.getSimpleName();
	public static final String	EXTRA_FILE_ID			= "_fileID";
	public static final String	EXTRA_FILE_TYPE			= "_fileType";
	public static final String	EXTRA_DIALOG_TITLE_RES	= "_dialog_title_res";
	public static final int		FILE_TYPE_LIVE			= 0;
	public static final int		FILE_TYPE_RECORDING		= 1;
	public static final int		STREAM_TYPE_DIRECT		= 0;
	public static final int		STREAM_TYPE_TRANSCODE	= 1;
	private static String		flashUrl				= ServerConsts.REC_SERVICE_URL + ServerConsts.URL_FLASHSTREAM;
	private String				liveUrl					= "http://"+ServerConsts.REC_SERVICE_HOST + ":" + ServerConsts.REC_SERVICE_LIVE_STREAM_PORT + "/upnp/channelstream/";
	private String				mediaUrl				= "http://"+ServerConsts.REC_SERVICE_HOST + ":" + ServerConsts.REC_SERVICE_MEDIA_STREAM_PORT + "/upnp/recordings/";
	private Spinner				qualitySpinner;
	private Spinner				ffmpegSpinner;
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
	private List<String> 		ffmpegprefs;
	private static Gson 		gson					= new Gson();
	
	private SharedPreferences	prefs;
	private FfMpegPrefs ffMpegPrefs;

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

		rebuildProtectedFlashUrl();
		if (seekable) {
			DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
			preTime = String.valueOf(prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, 0));
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateQualitySpinner();
			}
		}).start();

	}

	private void updateQualitySpinner() {
		try {
            String ffmpegprefsString = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_FFMPEGPREFS);
            if (!TextUtils.isEmpty(ffmpegprefsString)){
                FFMPEGPrefsHandler prefsHandler = new FFMPEGPrefsHandler();

				ffMpegPrefs = prefsHandler.parse(ffmpegprefsString);
				final ArrayAdapter<Preset> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, ffMpegPrefs.getPresets());
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						qualitySpinner.setAdapter(dataAdapter);
						int pos = ffMpegPrefs.getPresets().indexOf(getDefaultPreset(prefs));
						qualitySpinner.setSelection(pos);
					}
				});
            }

        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (DefaultHttpException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	private static String rebuildProtectedFlashUrl() {
		if((!TextUtils.isEmpty(ServerConsts.REC_SERVICE_USER_NAME)) && (!TextUtils.isEmpty(ServerConsts.REC_SERVICE_PASSWORD))) {
			flashUrl = ServerConsts.REC_SERVICE_PROTOCOL + "://" +
					   ServerConsts.REC_SERVICE_USER_NAME + ":" +
					   ServerConsts.REC_SERVICE_PASSWORD + "@" +
					   ServerConsts.REC_SERVICE_HOST + ":" +
					   ServerConsts.REC_SERVICE_PORT +
					   ServerConsts.URL_FLASHSTREAM;
		}
		return flashUrl;
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
		qualitySpinner.setOnItemSelectedListener(this);

		ffmpegSpinner = (Spinner) v.findViewById(R.id.ffmpegSpinner);
		int ffmpegIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_FFMPEG_PRESET, 5);
		ffmpegSpinner.setSelection(ffmpegIndex);
		ffmpegSpinner.setOnItemSelectedListener(this);
		
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
		String videoType = "video/mpeg";
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
			break;
		case STREAM_TYPE_TRANSCODE:
			Preset p = (Preset) qualitySpinner.getSelectedItem();
			HttpUrl httpUrl = HttpUrl.parse(rebuildProtectedFlashUrl()+p.getExtension());
			HttpUrl.Builder builder = httpUrl.newBuilder();
			builder.addQueryParameter("preset", p.getTitle());
			builder.addQueryParameter("ffPreset", ffmpegSpinner.getSelectedItem().toString());

			/**
			 * Calculate startposition in seconds
			 */
			int hours = TextUtils.isEmpty(startHours.getText()) ? 0 : Integer.valueOf(startHours.getText().toString());
			int minutes = TextUtils.isEmpty(startMinutes.getText()) ? 0 : Integer.valueOf(startMinutes.getText().toString());
			int seconds = TextUtils.isEmpty(startSeconds.getText()) ? 0 : Integer.valueOf(startSeconds.getText().toString());
			int start = 3600 * hours + 60 * minutes + seconds;
			builder.addQueryParameter("start", String.valueOf(start));

			switch (mFileType) {
			case FILE_TYPE_LIVE:
				builder.addQueryParameter("chid", String.valueOf(mFileId));
				break;
			case FILE_TYPE_RECORDING:
				builder.addQueryParameter("recid", String.valueOf(mFileId));
				break;

			default:
				break;
			}
			videoUrl = builder.build().toString();
			videoType = p.getMimeType();
			break;

		default:
			break;
		}
		Log.i(StreamConfig.class.getSimpleName(), "url: " + videoUrl);
		Intent videoIntent;
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
			Preset p = (Preset) qualitySpinner.getSelectedItem();
			editor.putString(DVBViewerPreferences.KEY_STREAM_PRESET, gson.toJson(p));
			break;
		case R.id.ffmpegSpinner:
			editor.putInt(DVBViewerPreferences.KEY_STREAM_FFMPEG_PRESET, position);
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
	
	
	public static Intent buildLiveUrl(Context context, int position){
		SharedPreferences prefs = new DVBViewerPreferences(context).getStreamPrefs();
		boolean direct = prefs.getBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true);
		StringBuffer result = new StringBuffer();
		if (direct) {
			return getDirectUrl(position);
		}else {
			return getTranscodedUrl(context, position, prefs);
		}
	}

	private static Preset getDefaultPreset(SharedPreferences prefs) {
		Preset p = null;
		String jsonPreset = prefs.getString(DVBViewerPreferences.KEY_STREAM_PRESET, null);
		try {
			p = gson.fromJson(jsonPreset, Preset.class);
		} catch (Exception e) {
			Log.d(Tag, "Error parsing default Preset", e);
		}
		if (p == null) {
			p = new Preset();
			p.setTitle("TS Mid 1200 kbit");
			p.setExtension(".ts");
			p.setMimeType("video/mpeg");
		}
		return p;
	}

	private static Intent getTranscodedUrl(Context context, int position, SharedPreferences prefs) {
		Preset p = getDefaultPreset(prefs);
		HttpUrl httpUrl = HttpUrl.parse(rebuildProtectedFlashUrl() + p.getExtension());
		HttpUrl.Builder builder = httpUrl.newBuilder();
		int ffmpegIndex = prefs.getInt(DVBViewerPreferences.KEY_STREAM_FFMPEG_PRESET, 5);
		String quality = p.getTitle();
		String ffmpeg = context.getResources().getStringArray(R.array.ffmpegPresets)[ffmpegIndex];
		builder.addQueryParameter("preset", quality);
		builder.addQueryParameter("ffPreset", ffmpeg);
		/**
         * Check if height is set from user, otherwise the the default values are used
         */
		builder.addQueryParameter("chid", String.valueOf(position));
		Intent videoIntent = new Intent(Intent.ACTION_VIEW);
		Log.d(Tag, "playing video: "+builder.build().toString());
		videoIntent.setDataAndType(Uri.parse(builder.build().toString()), p.getMimeType());
		return videoIntent;
	}

	public static Intent getDirectUrl(int position){
		StringBuffer result = new StringBuffer();
		result.append("http://"+ServerConsts.REC_SERVICE_HOST + ":" + ServerConsts.REC_SERVICE_LIVE_STREAM_PORT + "/upnp/channelstream/");
		result.append(position+".ts");
		Log.d(Tag, "playing video: "+result.toString());
		Intent videoIntent = new Intent(Intent.ACTION_VIEW);
		videoIntent.setDataAndType(Uri.parse(result.toString()), "video/mpeg");
		return videoIntent;
	}
	

}
