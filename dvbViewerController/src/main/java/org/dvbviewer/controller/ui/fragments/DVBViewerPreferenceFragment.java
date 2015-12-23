package org.dvbviewer.controller.ui.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;

public class DVBViewerPreferenceFragment extends PreferenceFragmentCompat {
	
	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(DVBViewerPreferences.PREFS);
		addPreferencesFromResource(R.xml.preferences);
	}

}
