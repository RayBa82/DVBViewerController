package org.dvbviewer.controller.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.ui.base.BaseFragment;
import org.dvbviewer.controller.ui.widget.InputTypePref;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.URLUtil;

public class ConnectionPreferenceFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

    public static final String TAG = ConnectionPreferenceFragment.class.getName();

    private static final String DIALOG_FRAGMENT_TAG =
            "org.dvbviewer.controller.ui.fragments.PreferenceFragment.DIALOG";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(DVBViewerPreferences.PREFS);
        addPreferencesFromResource(R.xml.rs_preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case DVBViewerPreferences.KEY_RS_URL:
                ServerConsts.REC_SERVICE_URL = sharedPreferences.getString(key, "http://");
                break;
            case DVBViewerPreferences.KEY_RS_PORT:
                ServerConsts.REC_SERVICE_PORT = sharedPreferences.getString(key, "");
                break;
            case DVBViewerPreferences.KEY_RS_USERNAME:
                ServerConsts.REC_SERVICE_USER_NAME = sharedPreferences.getString(key, "");
                break;
            case DVBViewerPreferences.KEY_RS_PASSWORD:
                ServerConsts.REC_SERVICE_PASSWORD = sharedPreferences.getString(key, "");
                break;
        }
        URLUtil.setRecordingServicesAddress(ServerConsts.REC_SERVICE_URL, ServerConsts.REC_SERVICE_PORT);
    }

    public void sendConnectionChanged() {
        Log.d(TAG, "Broadcasting connection event");
        Intent intent = new Intent(BaseFragment.CONNECTION_EVENT);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendConnectionChanged();
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


    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        boolean handled = false;

        if (preference instanceof InputTypePref) {
            DialogFragment f = NumberEditTextDialog.newInstance(preference.getKey(), ((InputTypePref) preference).getInputType());
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }

    }

    public static class NumberEditTextDialog extends EditTextPreferenceDialogFragmentCompat {

        public static NumberEditTextDialog newInstance(String key, int inputType) {
            final NumberEditTextDialog
                    fragment = new NumberEditTextDialog();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            b.putInt("inputType", inputType);
            fragment.setArguments(b);
            return fragment;
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            final int inputType = getArguments().getInt("inputType");
            ((EditText) view.findViewById(android.R.id.edit)).setInputType(inputType);
        }

    }

}
