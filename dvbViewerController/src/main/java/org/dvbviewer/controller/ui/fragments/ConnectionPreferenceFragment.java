package org.dvbviewer.controller.ui.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.URLUtil;

public class ConnectionPreferenceFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(DVBViewerPreferences.PREFS);
		addPreferencesFromResource(R.xml.rs_preferences);
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
		}
		URLUtil.setRecordingServicesAddress(ServerConsts.REC_SERVICE_URL, ServerConsts.REC_SERVICE_PORT);
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
