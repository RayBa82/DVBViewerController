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
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.ApiResponse;
import org.dvbviewer.controller.data.stream.StreamRepository;
import org.dvbviewer.controller.data.stream.StreamViewModel;
import org.dvbviewer.controller.data.stream.StreamViewModelFactory;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.FFMpegPresetList;
import org.dvbviewer.controller.entities.Preset;
import org.dvbviewer.controller.io.UrlBuilderException;
import org.dvbviewer.controller.io.api.APIClient;
import org.dvbviewer.controller.io.api.DMSInterface;
import org.dvbviewer.controller.ui.base.BaseDialogFragment;
import org.dvbviewer.controller.utils.AnalyticsTracker;
import org.dvbviewer.controller.utils.FileType;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.StreamType;
import org.dvbviewer.controller.utils.StreamUtils;
import org.dvbviewer.controller.utils.URLUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.HttpUrl;

/**
 * DialogFragment to show the stream settings.
 */
public class StreamConfig extends BaseDialogFragment implements OnClickListener, DialogInterface.OnClickListener, OnItemSelectedListener {

    private static final String Tag = StreamConfig.class.getSimpleName();
    private static final Gson gson = new Gson();
    public static final String EXTRA_FILE_ID = "_fileID";
    public static final String M3U8_MIME_TYPE = "video/m3u8";
    public static final String EXTRA_FILE_TYPE = "_fileType";
    public static final String EXTRA_DIALOG_TITLE_RES = "_dialog_title_res";
    public static final String EXTRA_TITLE = "title";

    @BindView(R.id.collapsable)
    View collapsable;

    @BindView(R.id.startDirectButton)
    Button startDirectStreamButton;

    @BindView(R.id.startTranscodedButton)
    Button startButton;

    @BindView(R.id.stream_hours)
    EditText startHours;

    @BindView(R.id.stream_minutes)
    EditText startMinutes;

    @BindView(R.id.stream_seconds)
    EditText startSeconds;

    @BindView(R.id.qualitySpinner)
    Spinner qualitySpinner;

    @BindView(R.id.encodingSpeedSpinner)
    Spinner encodingSpeedSpinner;

    @BindView(R.id.audioSpinner)
    Spinner audioTrackSpinner;

    @BindView(R.id.subTitleSpinner)
    Spinner subTitleSpinner;

    private String preTime;
    private int title = 0;
    private boolean seekable = false;
    private String mTitle = StringUtils.EMPTY;
    private StreamType mStreamType;
    private FileType mFileType;
    private long mFileId = -1;
    private SharedPreferences prefs;
    private DMSInterface dmsInterface;


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
            preTime = String.valueOf(dvbvPrefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, DVBViewerPreferences.DEFAULT_TIMER_TIME_BEFORE));
        }
        dmsInterface = APIClient.getClient().create(DMSInterface.class);
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dia = super.onCreateDialog(savedInstanceState);
        dia.setTitle(R.string.streamConfig);
        return dia;
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        qualitySpinner.requestFocus();
        startHours.clearFocus();
        collapsable.setVisibility(View.GONE);
        qualitySpinner.setOnItemSelectedListener(this);
        int encodingSpeed = StreamUtils.getEncodingSpeedIndex(getContext(), prefs);
        encodingSpeedSpinner.setSelection(encodingSpeed);
        encodingSpeedSpinner.setOnItemSelectedListener(this);
        audioTrackSpinner.setOnItemSelectedListener(this);
        final List<String> audioTracks = new LinkedList<>();
        audioTracks.add(getResources().getString(R.string.def));
        audioTracks.add(getResources().getString(R.string.common_all));
        audioTracks.addAll(Arrays.asList(getResources().getStringArray(R.array.tracks)));
        String[] audio = new String[audioTracks.size()];
        audio = audioTracks.toArray(audio);
        ArrayAdapter<String> audioAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, audio); //selected item will look like a spinner set from XML
        audioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        audioTrackSpinner.setAdapter(audioAdapter);
        subTitleSpinner.setOnItemSelectedListener(this);
        final List<String> subTitleTracks = new LinkedList<>();
        subTitleTracks.add(getResources().getString(R.string.none));
        subTitleTracks.add(getResources().getString(R.string.common_all));
        subTitleTracks.addAll(Arrays.asList(getResources().getStringArray(R.array.tracks)));
        String[] subs = new String[subTitleTracks.size()];
        subs = subTitleTracks.toArray(subs);
        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, subs); //selected item will look like a spinner set from XML
        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subTitleSpinner.setAdapter(subAdapter);
        startButton.setOnClickListener(this);
        startDirectStreamButton.setOnClickListener(this);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        final StreamRepository streamRepository = new StreamRepository(dmsInterface);
        final StreamViewModelFactory mediaFac = new StreamViewModelFactory(getActivity().getApplication(), streamRepository);
        final StreamViewModel streamViewModel = ViewModelProviders.of(this, mediaFac)
                .get(StreamViewModel.class);
        final Observer<ApiResponse<FFMpegPresetList>> mediaObserver = new Observer<ApiResponse<FFMpegPresetList>>() {
            @Override
            public void onChanged(@Nullable final ApiResponse<FFMpegPresetList> response) {
                final FFMpegPresetList presets = response.data;
                if (presets != null && !presets.getPresets().isEmpty()) {
                    final ArrayAdapter<Preset> dataAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, presets.getPresets());
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    int pos = presets.getPresets().indexOf(StreamUtils.getDefaultPreset(prefs));
                    startHours.clearFocus();
                    qualitySpinner.setAdapter(dataAdapter);
                    qualitySpinner.setSelection(pos);
                    ViewGroup vg = (ViewGroup) collapsable.getParent();
                    int widthMeasureSpec = ViewGroup.MeasureSpec.makeMeasureSpec(vg.getWidth(), View.MeasureSpec.AT_MOST);
                    int heightMeasureSpec = ViewGroup.MeasureSpec.makeMeasureSpec(1073741823, View.MeasureSpec.AT_MOST);
                    collapsable.measure(widthMeasureSpec, heightMeasureSpec);
                    collapsable.setVisibility(View.VISIBLE);
                }
            }
        };
        streamViewModel.getFFMpegPresets().observe(StreamConfig.this, mediaObserver);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_stream_config, container, false);
        ButterKnife.bind(this, v);
        final View positionContainer = v.findViewById(R.id.streamPositionContainer);
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
                prefs.edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, false).apply();
                mStreamType = StreamType.TRANSCODED;
                startStreaming(false, mFileType);
                break;
            case R.id.startDirectButton:
                prefs.edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true).apply();
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
            if (direct) {
                AnalyticsTracker.trackDirectStream(getActivity().getApplication());
            } else {
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
    private Intent getVideoIntent(FileType fileType) {
        Intent videoIntent;
        final Preset preset = (Preset) qualitySpinner.getSelectedItem();
        if (mStreamType == StreamType.DIRECT) {
            videoIntent = getDirectUrl(mFileId, mTitle, fileType);
        } else {
            final int encodingSpeed = encodingSpeedSpinner.getSelectedItemPosition();
            int hours = TextUtils.isEmpty(startHours.getText()) ? 0 : NumberUtils.toInt(startHours.getText().toString());
            int minutes = TextUtils.isEmpty(startMinutes.getText()) ? 0 : NumberUtils.toInt(startMinutes.getText().toString());
            int seconds = TextUtils.isEmpty(startSeconds.getText()) ? 0 : NumberUtils.toInt(startSeconds.getText().toString());
            int start = 3600 * hours + 60 * minutes + seconds;
            preset.setEncodingSpeed(encodingSpeed);
            videoIntent = getTranscodedUrl(getContext(), mFileId, mTitle, preset, fileType, start);
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
                editor.apply();
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
        Preset p = (Preset) qualitySpinner.getSelectedItem();
        switch (parent.getId()) {
            case R.id.encodingSpeedSpinner:
                p.setEncodingSpeed(position);
                break;
            case R.id.audioSpinner:
                int audioTrack;
                switch (position) {
                    case 0:
                        audioTrack = 0;
                        break;
                    case 1:
                        audioTrack = -1;
                        break;
                    default:
                        audioTrack = position - 2;
                        break;
                }
                p.setAudioTrack(audioTrack);
                break;
            case R.id.subTitleSpinner:
                int subtTitleTrack;
                switch (position) {
                    case 0:
                        subtTitleTrack = -1;
                        break;
                    case 1:
                        subtTitleTrack = 0;
                        break;
                    default:
                        subtTitleTrack = position - 2;
                        break;
                }
                p.setSubTitle(subtTitleTrack);
                break;
            default:
                break;
        }
        editor.putString(DVBViewerPreferences.KEY_STREAM_PRESET, gson.toJson(p));
        editor.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }


    public static Intent buildQuickUrl(Context context, long id, String title, FileType fileType) {
        final SharedPreferences prefs = new DVBViewerPreferences(context).getStreamPrefs();
        boolean direct = prefs.getBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true);
        if (direct) {
            return getDirectUrl(id, title, fileType);
        } else {
            return getTranscodedUrl(context, id, title, StreamUtils.getDefaultPreset(prefs), fileType, 0);
        }
    }

    private static Intent addTitle(Intent intent, String title) {
        intent.putExtra(EXTRA_TITLE, title);
        return intent;
    }

    public static Intent getTranscodedUrl(Context context, final long id, String title, final FileType fileType) {
        final SharedPreferences prefs = new DVBViewerPreferences(context).getStreamPrefs();
        return getTranscodedUrl(context, id, title, StreamUtils.getDefaultPreset(prefs), fileType, 0);
    }

    private static Intent getTranscodedUrl(Context context, final long id, String title, final Preset preset, final FileType fileType, final int start) {
        final HttpUrl.Builder builder = URLUtil.buildProtectedRSUrl();
        if (StreamConfig.M3U8_MIME_TYPE.equals(preset.getMimeType())) {
            builder.addPathSegment(ServerConsts.URL_M3U8);
        } else {
            builder.addPathSegments(ServerConsts.URL_FLASHSTREAM + preset.getExtension());
        }
        builder.addQueryParameter("preset", preset.getTitle());
        builder.addQueryParameter("ffPreset", StreamUtils.getEncodingSpeedName(context, preset));
        builder.addQueryParameter(fileType.transcodedParam, String.valueOf(id));
        builder.addQueryParameter("track", String.valueOf(preset.getAudioTrack()));
        if (start > 0) {
            builder.addQueryParameter("start", String.valueOf(start));
        }
        if (preset.getSubTitle() >= 0) {
            builder.addQueryParameter("subs", String.valueOf(preset.getSubTitle()));
        }
        final Intent videoIntent = new Intent(Intent.ACTION_VIEW);
        final String url = builder.build().toString();
        Log.d(Tag, "playing video: " + url);
        videoIntent.setDataAndType(Uri.parse(url), preset.getMimeType());
        addTitle(videoIntent, title);
        return videoIntent;
    }

    public static Intent getDirectUrl(long id, String title, FileType fileType) {
        final HttpUrl.Builder builder = URLUtil.buildProtectedRSUrl();
        builder.addPathSegment("upnp");
        builder.addPathSegment(fileType.directPath);
        builder.addPathSegment(String.valueOf(id) + ".ts");
        final String videoUrl = builder.build().toString();
        Log.d(Tag, "playing video: " + videoUrl);
        Intent videoIntent = new Intent(Intent.ACTION_VIEW);
        videoIntent.setDataAndType(Uri.parse(videoUrl), "video/mpeg");
        addTitle(videoIntent, title);
        return videoIntent;
    }

}