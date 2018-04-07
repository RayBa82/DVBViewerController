package org.dvbviewer.controller.data.version;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import org.dvbviewer.controller.data.version.xml.Version;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.io.api.APIClient;
import org.dvbviewer.controller.io.api.DMSInterface;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rbaun on 02.04.18.
 */

public class VersionRepository {

    private static final String TAG = VersionRepository.class.getName();

    private final static Pattern versionPattern    = Pattern.compile("(?!\\.)(\\d+(\\.\\d+)+)(?![\\d\\.])");

    private DMSInterface dmsInterface;

    private MutableLiveData<String> version;
    private final DVBViewerPreferences prefs;

    public VersionRepository(Context context) {
        prefs = new DVBViewerPreferences(context);
        dmsInterface = APIClient.getClient().create(DMSInterface.class);
    }


    public MutableLiveData<String> getVersion() {
        if (version == null) {
            version = new MutableLiveData<>();
            final String savedVersion = prefs.getString(DVBViewerPreferences.KEY_RS_VERSION);
            version.setValue(savedVersion);
            loadVersion();
        }
        return version;
    }

    private void loadVersion() {
        dmsInterface.getVersion().enqueue(new Callback<Version>() {
            @Override
            public void onResponse(Call<Version> call, Response<Version> response) {
                Version root = response.body();
                final String rawVersion = root.getVersion();
                final Matcher matcher = versionPattern.matcher(rawVersion);
                if(matcher.find()) {
                    final String v = matcher.group();
                    final String oldVersion = version.getValue();
                    if(oldVersion != null && !oldVersion.equals(v)) {
                        version.setValue(v);
                        prefs.getPrefs().
                                edit()
                                .putString(DVBViewerPreferences.KEY_RS_VERSION, v)
                                .apply();
                    }
                }else {
                    version.setValue(null);
                }

            }

            @Override
            public void onFailure(Call<Version> call, Throwable t) {
                Log.e(TAG, "Error getting version", t);
                version.setValue(null);
            }
        });
    }

}
