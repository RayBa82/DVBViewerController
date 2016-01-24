package org.dvbviewer.controller.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.Preset;

import java.util.Arrays;

/**
 * Created by RayBa82 on 24.01.16.
 *
 * Util class for Streaming
 */
public class StreamUtils {

    private static final String	Tag				        = StreamUtils.class.getSimpleName();
    private static final Gson gson					    = new Gson();
    public static final String	DEFAULT_ENCODING_SPEED	= "ultrafast";

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
            p.setTitle("TS Mid 1200 kbit");
            p.setExtension(".ts");
            p.setMimeType("video/mpeg");
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

    public static String getEncodingSpeedName(final Context context, final SharedPreferences prefs) {
        final int index = getEncodingSpeedIndex(context, prefs);
        return context.getResources().getStringArray(R.array.ffmpegPresets)[index];
    }
}
