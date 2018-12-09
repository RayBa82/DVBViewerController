package org.dvbviewer.controller.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.ui.phone.ConnectionPreferencesActivity;

public class DVBViewerPreferenceFragment extends PreferenceFragmentCompat {

	private static final String KEY_RS_SETTINGS = "KEY_RS_SETTINGS";

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(DVBViewerPreferences.PREFS);
		addPreferencesFromResource(R.xml.preferences);
		final Preference preference = getPreferenceScreen().findPreference(KEY_RS_SETTINGS);
		preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Intent settings = new Intent(getContext(), ConnectionPreferencesActivity.class);
				startActivity(settings);
				return false;
			}
		});
	}

}
