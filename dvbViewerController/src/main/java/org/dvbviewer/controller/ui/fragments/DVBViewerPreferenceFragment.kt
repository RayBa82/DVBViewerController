package org.dvbviewer.controller.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.dvbviewer.controller.R
import org.dvbviewer.controller.entities.DVBViewerPreferences
import org.dvbviewer.controller.ui.phone.ConnectionPreferencesActivity

class DVBViewerPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        val prefMgr = preferenceManager
        prefMgr.sharedPreferencesName = DVBViewerPreferences.PREFS
        addPreferencesFromResource(R.xml.preferences)
        val preference = preferenceScreen.findPreference(KEY_RS_SETTINGS)
        preference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val settings = Intent(context, ConnectionPreferencesActivity::class.java)
            startActivity(settings)
            false
        }
    }

    companion object {

        private const val KEY_RS_SETTINGS = "KEY_RS_SETTINGS"
    }

}
