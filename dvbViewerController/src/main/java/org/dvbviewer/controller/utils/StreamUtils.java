package org.dvbviewer.controller.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.Preset;

import java.util.Arrays;

import okhttp3.HttpUrl;

/**
 * Created by RayBa82 on 24.01.16.
 *
 * Util class for Streaming
 */
public class StreamUtils {

    private static final String	Tag				        = StreamUtils.class.getSimpleName();
    private static final Gson gson					    = new Gson();
    public static final String	DEFAULT_ENCODING_SPEED	= "ultrafast";
    public static final String EXTRA_TITLE = "title";
    public static final String M3U8_MIME_TYPE = "video/m3u8";

    public static Preset getDefaultPreset(SharedPreferences prefs) {
        Preset p = null;
        try {
            final String jsonPreset = prefs.getString(DVBViewerPreferences.KEY_STREAM_PRESET, null);
            p = gson.fromJson(jsonPreset, Preset.class);
        } catch (Exception e) {
            Log.d(Tag, "Error parsing default Preset", e);
        }
        if (p == null) {
            p = new Preset();
            p.setTitle("HLS Mid 1200 kbit");
            p.setMimeType(M3U8_MIME_TYPE);
        }
        return p;
    }

    public static int getEncodingSpeedIndex(final Context context, final SharedPreferences prefs) {
        int encodingSpeed = prefs.getInt(DVBViewerPreferences.KEY_STREAM_ENCODING_SPEED, 4);
        String[] availableSpeeds = context.getResources().getStringArray(R.array.ffmpegPresets);
        if (encodingSpeed < 0 || encodingSpeed >= availableSpeeds.length){
            encodingSpeed = Arrays.asList(availableSpeeds).indexOf(DEFAULT_ENCODING_SPEED);
        }
        return encodingSpeed;
    }

    public static String getEncodingSpeedName(final Context context, final Preset preset) {
        return context.getResources().getStringArray(R.array.ffmpegPresets)[preset.getEncodingSpeed()];
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

    public static Intent getTranscodedUrl(Context context, final long id, String title, final Preset preset, final FileType fileType, final int start) {
        final HttpUrl.Builder builder = URLUtil.buildProtectedRSUrl();
        if (M3U8_MIME_TYPE.equals(preset.getMimeType())) {
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
