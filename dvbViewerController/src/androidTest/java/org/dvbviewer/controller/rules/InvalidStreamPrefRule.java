package org.dvbviewer.controller.rules;

import android.app.Activity;

import org.dvbviewer.controller.entities.DVBViewerPreferences;

/**
 * Created by r.baun on 14.06.2015.
 */
public class InvalidStreamPrefRule<T extends Activity> extends DefaultDvbViewerRule {


	public InvalidStreamPrefRule(Class<T> activityClass) {
		super(activityClass);
	}

	@Override
	protected void beforeActivityLaunched() {
		super.beforeActivityLaunched();
		prefs.getStreamPrefs().edit().putInt(DVBViewerPreferences.KEY_STREAM_ENCODING_SPEED, 9).commit();
	}


}
