package org.dvbviewer.controller.data.stream;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.Log;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.FFMpegPresetList;
import org.dvbviewer.controller.io.api.DMSInterface;
import org.dvbviewer.controller.io.data.FFMPEGPrefsHandler;
import org.dvbviewer.controller.ui.fragments.StreamConfig;

import java.io.InputStream;

/**
 * Created by rbaun on 02.04.18.
 */

public class StreamRepository {

    private final DMSInterface dmsInterface;

    public StreamRepository(DMSInterface dmsInterface) {
        this.dmsInterface = dmsInterface;
    }

    public static final String 	IPHONE_PREFS		= "config\\iphoneprefs.ini";
    public static final String 	FFMPEGPREFS			= "config\\ffmpegprefs.ini";

    public FFMpegPresetList getFFMpegPresets(Context context) {
        final FFMPEGPrefsHandler prefsHandler = new FFMPEGPrefsHandler();
        FFMpegPresetList result = getPrefs(context, IPHONE_PREFS,
                prefsHandler,
                R.raw.iphoneprefs);
        final FFMpegPresetList ffMpegPrefs = getPrefs(context, FFMPEGPREFS,
                prefsHandler, R.raw.ffmpegprefs);
        result.getPresets().addAll(ffMpegPrefs.getPresets());
        return result;
    }

    private FFMpegPresetList getPrefs(Context context, String file, FFMPEGPrefsHandler handler, int defaults) {
        try {
            return dmsInterface.getConfigFile(file).execute().body();
        } catch (Exception e) {
            return getDefaultPrefs(context, handler, defaults);
        }

    }

    @Nullable
    private FFMpegPresetList getDefaultPrefs(Context context, FFMPEGPrefsHandler handler, int defaults) {
        try {
            final Resources res = context.getResources();
            final InputStream in_s = res.openRawResource(defaults);
            final byte[] b = new byte[in_s.available()];
            in_s.read(b);
            return handler.parse(new String(b));
        } catch (Exception e) {
            Log.e(StreamConfig.class.getSimpleName(), "Error reading default presets", e);
        }
        return new FFMpegPresetList();
    }

}