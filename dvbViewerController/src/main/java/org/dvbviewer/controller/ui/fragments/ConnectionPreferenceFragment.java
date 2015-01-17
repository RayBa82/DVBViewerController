package org.dvbviewer.controller.ui.fragments;

import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.URLUtil;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;

public class ConnectionPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		int id = getArguments().getInt("id");
		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(DVBViewerPreferences.PREFS);
		addPreferencesFromResource(id);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(DVBViewerPreferences.KEY_RS_URL)) {
			ServerConsts.REC_SERVICE_URL = sharedPreferences.getString(key, "http://");
		} else if (key.equals(DVBViewerPreferences.KEY_RS_PORT)) {
			ServerConsts.REC_SERVICE_PORT = sharedPreferences.getString(key, "");
		} else if (key.equals(DVBViewerPreferences.KEY_RS_USERNAME)) {
			ServerConsts.REC_SERVICE_USER_NAME = sharedPreferences.getString(key, "");
		} else if (key.equals(DVBViewerPreferences.KEY_RS_PASSWORD)) {
			ServerConsts.REC_SERVICE_PASSWORD = sharedPreferences.getString(key, "");
		} else if (key.equals(DVBViewerPreferences.KEY_RS_LIVE_STREAM_PORT)) {
			ServerConsts.REC_SERVICE_LIVE_STREAM_PORT = sharedPreferences.getString(key, ServerConsts.REC_SERVICE_LIVE_STREAM_PORT);
		} else if (key.equals(DVBViewerPreferences.KEY_RS_MEDIA_STREAM_PORT)) {
			ServerConsts.REC_SERVICE_MEDIA_STREAM_PORT = sharedPreferences.getString(key, ServerConsts.REC_SERVICE_MEDIA_STREAM_PORT);
		}
		URLUtil.setRecordingServicesAddress(ServerConsts.REC_SERVICE_URL, ServerConsts.REC_SERVICE_PORT);
		ServerRequest.resetHttpCLient();
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actionbarsherlock.app.SherlockPreferenceActivity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

}
