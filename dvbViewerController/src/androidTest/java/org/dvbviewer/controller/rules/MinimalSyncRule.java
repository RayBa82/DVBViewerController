package org.dvbviewer.controller.rules;

import android.app.Activity;

import org.dvbviewer.controller.entities.DVBViewerPreferences;

/**
 * Created by r.baun on 14.06.2015.
 */
public class MinimalSyncRule<T extends Activity> extends DefaultDvbViewerRule {


    public MinimalSyncRule(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void beforeActivityLaunched() {
        FAVOURITES_KEY = null;
        FFMPEGPREFS_KEY = null;
        TARGETS_KEY = null;
        super.beforeActivityLaunched();
        prefs.getPrefs().edit().putBoolean(DVBViewerPreferences.KEY_SHOW_QUICK_STREAM_HINT, false).commit();
    }

}
