package org.dvbviewer.controller.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.ui.base.BaseFragment
import org.dvbviewer.controller.ui.widget.InputTypePref
import org.dvbviewer.controller.utils.ServerConsts
import org.dvbviewer.controller.utils.URLUtil

class ConnectionPreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        val prefMgr = preferenceManager
        prefMgr.sharedPreferencesName = DVBViewerPreferences.PREFS
        addPreferencesFromResource(R.xml.rs_preferences)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            DVBViewerPreferences.KEY_DMS_URL -> ServerConsts.DMS_URL = sharedPreferences.getString(key, null)
            DVBViewerPreferences.KEY_RS_PORT -> ServerConsts.REC_SERVICE_PORT = sharedPreferences.getString(key, "")
            DVBViewerPreferences.KEY_RS_USERNAME -> ServerConsts.REC_SERVICE_USER_NAME = sharedPreferences.getString(key, "")
            DVBViewerPreferences.KEY_RS_PASSWORD -> ServerConsts.REC_SERVICE_PASSWORD = sharedPreferences.getString(key, "")
        }
        URLUtil.setRecordingServicesAddress(ServerConsts.DMS_URL, ServerConsts.REC_SERVICE_PORT)
    }

    fun sendConnectionChanged() {
        Log.d(TAG, "Broadcasting connection event")
        val intent = Intent(BaseFragment.CONNECTION_EVENT)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        sendConnectionChanged()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    /*
     * (non-Javadoc)
     *
     * @see com.actionbarsherlock.app.SherlockPreferenceActivity#onPause()
     */
    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }


    override fun onDisplayPreferenceDialog(preference: Preference) {
        val handled = false

        if (preference is InputTypePref) {
            val f = NumberEditTextDialog.newInstance(preference.key, (preference as InputTypePref).inputType)
            f.setTargetFragment(this, 0)
            f.show(fragmentManager!!, DIALOG_FRAGMENT_TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }

    }

    class NumberEditTextDialog : EditTextPreferenceDialogFragmentCompat() {

        override fun onBindDialogView(view: View) {
            super.onBindDialogView(view)
            val inputType = arguments!!.getInt("inputType")
            (view.findViewById<View>(android.R.id.edit) as EditText).inputType = inputType
        }

        companion object {

            fun newInstance(key: String, inputType: Int): NumberEditTextDialog {
                val fragment = NumberEditTextDialog()
                val b = Bundle(1)
                b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
                b.putInt("inputType", inputType)
                fragment.arguments = b
                return fragment
            }
        }

    }

    companion object {

        val TAG = ConnectionPreferenceFragment::class.java.name

        private val DIALOG_FRAGMENT_TAG = "org.dvbviewer.controller.ui.fragments.PreferenceFragment.DIALOG"
    }

}
