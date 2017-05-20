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

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
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

import com.google.gson.Gson;
import com.nineoldandroids.animation.IntEvaluator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.FfMpegPrefs;
import org.dvbviewer.controller.entities.Preset;
import org.dvbviewer.controller.io.HTTPUtil;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.UrlBuilderException;
import org.dvbviewer.controller.io.data.FFMPEGPrefsHandler;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.BaseDialogFragment;
import org.dvbviewer.controller.utils.AnalyticsTracker;
import org.dvbviewer.controller.utils.FileType;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.StreamType;
import org.dvbviewer.controller.utils.StreamUtils;
import org.dvbviewer.controller.utils.URLUtil;

import java.io.InputStream;

/**
 * DialogFragment to show the stream settings.
 *
 */
public class StreamConfig extends BaseDialogFragment implements OnClickListener, DialogInterface.OnClickListener, OnItemSelectedListener, LoaderManager.LoaderCallbacks<FfMpegPrefs> {

	private static final String	Tag						= StreamConfig.class.getSimpleName();
	private static final Gson 	gson					= new Gson();
	public static final String	EXTRA_FILE_ID			= "_fileID";
	public static final String	M3U8_MIME_TYPE			= "video/m3u8";
	public static final String	EXTRA_FILE_TYPE			= "_fileType";
	public static final String	EXTRA_DIALOG_TITLE_RES	= "_dialog_title_res";
	public static final String  EXTRA_TITLE             = "title";
	private EditText			startHours;
	private EditText			startMinutes;
	private EditText			startSeconds;
	private Spinner				qualitySpinner;
	private Spinner 			encodingSpeedSpinner;
	private Button				startButton;
	private String				preTime;
	private int					title					= 0;
	private boolean				seekable				= false;
    private String				mTitle				    = StringUtils.EMPTY;
	private StreamType			mStreamType;
    private FileType            mFileType;
	private long				mFileId					= -1;
	private SharedPreferences	prefs;
	private View collapsable;


	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DVBViewerPreferences dvbvPrefs = new DVBViewerPreferences(getContext());
		prefs = dvbvPrefs.getStreamPrefs();
		if (savedInstanceState != null) {
			title = savedInstanceState.getInt("titleRes");
		}
		mFileId = getArguments().getLong(EXTRA_FILE_ID);
		mFileType = getArguments().getParcelable(EXTRA_FILE_TYPE);
		mStreamType = StreamType.DIRECT;
        mTitle = getArguments().getString(EXTRA_TITLE);
		seekable = mFileType != FileType.CHANNEL;

		if (seekable) {
			DVBViewerPreferences prefs = new DVBViewerPreferences(getContext());
			preTime = String.valueOf(prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, DVBViewerPreferences.DEFAULT_TIMER_TIME_BEFORE));
		}
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@NonNull
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
		getLoaderManager().initLoader(0, arg0, this);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_stream_config, container, false);

		collapsable = v.findViewById(R.id.collapsable);
		collapsable.setVisibility(View.GONE);
		qualitySpinner = (Spinner) v.findViewById(R.id.qualitySpinner);
		qualitySpinner.setOnItemSelectedListener(this);

		encodingSpeedSpinner = (Spinner) v.findViewById(R.id.encodingSpeedSpinner);
		int encodingSpeed = StreamUtils.getEncodingSpeedIndex(getContext(), prefs);
		encodingSpeedSpinner.setSelection(encodingSpeed);
		encodingSpeedSpinner.setOnItemSelectedListener(this);
		
		startButton = (Button) v.findViewById(R.id.startTranscodedButton);
		startButton.setOnClickListener(this);
		Button startDirectStreamButton = (Button) v.findViewById(R.id.startDirectButton);
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
	 */
	public static StreamConfig newInstance() {
		return new StreamConfig();
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.startTranscodedButton:
			prefs.edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, false).commit();
			mStreamType = StreamType.TRANSCODED;
			startStreaming(false, mFileType);
			break;
		case R.id.startDirectButton:
			prefs.edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true).commit();
			mStreamType = StreamType.DIRECT;
			startStreaming(true, mFileType);
			break;

		default:
			break;
		}
	}

	private void startStreaming(boolean direct, FileType fileType) {
		try {
            startVideoIntent(fileType);
			if (direct){
				AnalyticsTracker.trackDirectStream(getActivity().getApplication());
			}else{
				AnalyticsTracker.trackTranscodedStream(getActivity().getApplication());
			}
        } catch (ActivityNotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getResources().getString(R.string.noFlashPlayerFound)).setPositiveButton(getResources().getString(R.string.yes), this).setNegativeButton(getResources().getString(R.string.no), this).show();
        } catch (UrlBuilderException e) {
			Log.e(Tag, "Error creating Stream URL", e);
        }
	}

	/**
	 * starts an {@link Intent} to play a video stream or throws an Exception if the video url
	 * could not be determined.
	 *
	 * @throws UrlBuilderException
	 */
	private void startVideoIntent(FileType fileType) throws UrlBuilderException {
		Intent videoIntent;
		videoIntent = getVideoIntent(fileType);
		startActivity(videoIntent);
		if (getDialog() != null) {
            getDialog().dismiss();
        } else {
            getActivity().finish();
        }
	}

	/**
	 * Gets the video intent.
	 *
	 * @return the video intent
	 */
	private Intent getVideoIntent(FileType fileType) throws UrlBuilderException {
		Intent videoIntent;
		if(mStreamType == StreamType.DIRECT){
            videoIntent = getDirectUrl(mFileId, mTitle, fileType);
        }else{
            final Preset preset = (Preset) qualitySpinner.getSelectedItem();
            final String encodingSpeed = encodingSpeedSpinner.getSelectedItem().toString();
            int hours = TextUtils.isEmpty(startHours.getText()) ? 0 : NumberUtils.toInt(startHours.getText().toString());
            int minutes = TextUtils.isEmpty(startMinutes.getText()) ? 0 : NumberUtils.toInt(startMinutes.getText().toString());
            int seconds = TextUtils.isEmpty(startSeconds.getText()) ? 0 : NumberUtils.toInt(startSeconds.getText().toString());
            int start = 3600 * hours + 60 * minutes + seconds;
            videoIntent = getTranscodedUrl(mFileId, mTitle, preset, encodingSpeed, fileType, start);
        }
		return videoIntent;
	}

	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
			Editor editor = prefs.edit();
			editor.putBoolean("stream_external", false);
			editor.commit();
			onClick(startButton);
			if (getDialog() != null) {
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
		case R.id.encodingSpeedSpinner:
			editor.putInt(DVBViewerPreferences.KEY_STREAM_ENCODING_SPEED, position);
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
	
	
	public static Intent buildLiveUrl(Context context, long id, String title, FileType fileType) throws UrlBuilderException {
		final SharedPreferences prefs = new DVBViewerPreferences(context).getStreamPrefs();
		boolean direct = prefs.getBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true);
		if (direct) {
			return getDirectUrl(id, title, fileType);
		}else {
			final String encodingSpeed = StreamUtils.getEncodingSpeedName(context, prefs);
			return getTranscodedUrl(id, title, StreamUtils.getDefaultPreset(prefs), encodingSpeed, fileType, 0);
		}
	}

	private static Intent addTitle(Intent intent, String title) {
		intent.putExtra(EXTRA_TITLE, title);
		return intent;
	}


	private static Intent getTranscodedUrl(final long id, String title, final Preset preset, final String encodingSpeed, final FileType fileType, final int start) throws UrlBuilderException {
		final StringBuilder baseUrl = new StringBuilder(ServerConsts.REC_SERVICE_URL);
		if (StreamConfig.M3U8_MIME_TYPE.equals(preset.getMimeType())){
			baseUrl.append(ServerConsts.URL_M3U8);
		} else {
			baseUrl.append(ServerConsts.URL_FLASHSTREAM + preset.getExtension());
		}
		final HTTPUtil.UrlBuilder builder = HTTPUtil.getUrlBuilder(URLUtil.buildProtectedRSUrl(baseUrl.toString()));
		builder.addQueryParameter("preset", preset.getTitle());
		builder.addQueryParameter("ffPreset", encodingSpeed);
		builder.addQueryParameter(fileType.transcodedParam, String.valueOf(id));
		builder.addQueryParameter("track", "-1");
		if (start > 0) {
			builder.addQueryParameter("start", String.valueOf(start));
		}
		final Intent videoIntent = new Intent(Intent.ACTION_VIEW);
		final String url = builder.build().toString();
		Log.d(Tag, "playing video: " + url);
		videoIntent.setDataAndType(Uri.parse(url), preset.getMimeType());
        addTitle(videoIntent, title);
		return videoIntent;
	}

	private static Intent getDirectUrl(long id, String title, FileType fileType){
		final StringBuilder baseUrl = new StringBuilder(ServerConsts.REC_SERVICE_URL).append("/upnp/" + fileType.directPath).append(id).append(".ts");
		final String videoUrl = URLUtil.buildProtectedRSUrl(baseUrl.toString());
		Log.d(Tag, "playing video: " + videoUrl);
		Intent videoIntent = new Intent(Intent.ACTION_VIEW);
		videoIntent.setDataAndType(Uri.parse(videoUrl), "video/mpeg");
        addTitle(videoIntent, title);
		return videoIntent;
	}

	@Override
	public Loader<FfMpegPrefs> onCreateLoader(int id, Bundle args) {
		return new AsyncLoader<FfMpegPrefs>(getContext()) {

			@Override
			public FfMpegPrefs loadInBackground() {
				final FFMPEGPrefsHandler prefsHandler = new FFMPEGPrefsHandler();
				FfMpegPrefs result = getPrefs(ServerConsts.URL_IPHONE_FFMPEGPREFS,
						prefsHandler,
                        R.raw.iphoneprefs);
				final FfMpegPrefs ffMpegPrefs = getPrefs(ServerConsts.URL_FFMPEGPREFS,
						prefsHandler, R.raw.ffmpegprefs);
				result.getPresets().addAll(ffMpegPrefs.getPresets());
				return result;
			}

			private FfMpegPrefs getPrefs(String url, FFMPEGPrefsHandler handler, int defaults) {
				try {
					final String prefString = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + url);
					return handler.parse(prefString);
				}catch (Exception e){
					return getDefaultPrefs(handler, defaults);
				}

			}
		};
	}

    @Nullable
    private FfMpegPrefs getDefaultPrefs(FFMPEGPrefsHandler handler, int defaults) {
        try {
            final Resources res = getResources();
            final InputStream in_s = res.openRawResource(defaults);
            final byte[] b = new byte[in_s.available()];
            in_s.read(b);
            return handler.parse(new String(b));
        } catch (Exception e) {
            Log.e(StreamConfig.class.getSimpleName(), "Error reading default presets", e);
        }
        return new FfMpegPrefs();
    }

    @Override
	public void onLoadFinished(Loader<FfMpegPrefs> loader, FfMpegPrefs data) {
		if (data != null && !data.getPresets().isEmpty()) {
			final ArrayAdapter<Preset> dataAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, data.getPresets());
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			int pos = data.getPresets().indexOf(StreamUtils.getDefaultPreset(prefs));
			startHours.clearFocus();
			qualitySpinner.setAdapter(dataAdapter);
			qualitySpinner.setSelection(pos);
			ViewGroup vg = (ViewGroup) collapsable.getParent();
			int widthMeasureSpec = ViewGroup.MeasureSpec.makeMeasureSpec(vg.getWidth(), View.MeasureSpec.AT_MOST);
			int heightMeasureSpec = ViewGroup.MeasureSpec.makeMeasureSpec(1073741823, View.MeasureSpec.AT_MOST);
			collapsable.measure(widthMeasureSpec, heightMeasureSpec);
			collapsable.setVisibility(View.VISIBLE);
//			ValueAnimator animator = ValueAnimator.ofObject(new HeightEvaluator(collapsable), 0, collapsable.getMeasuredHeight());
//			animator.setDuration(500);
//			animator.start();
		}
	}

	@Override
	public void onLoaderReset(Loader<FfMpegPrefs> loader) {

	}


	private static class HeightEvaluator extends IntEvaluator {

		private final View v;
		public HeightEvaluator(View v) {
			this.v = v;
		}

		@Override
		public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
			int height = super.evaluate(fraction, startValue, endValue);
			final ViewGroup.LayoutParams params = v.getLayoutParams();
			params.height = height;
			v.setLayoutParams(params);
			return height;
		}
	}


}
