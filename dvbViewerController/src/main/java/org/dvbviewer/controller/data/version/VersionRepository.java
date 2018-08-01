package org.dvbviewer.controller.data.version;

import android.content.Context;

import org.dvbviewer.controller.data.version.xml.Version;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.io.api.DMSInterface;

import java.io.IOException;

/**
 * Created by rbaun on 02.04.18.
 */

public class VersionRepository {

    private final DVBViewerPreferences prefs;
    private final DMSInterface dmsInterface;

    public VersionRepository(Context context, DMSInterface dmsInterface) {
        prefs = new DVBViewerPreferences(context);
        this.dmsInterface = dmsInterface;
    }

    public boolean isSupported(int minimiumVersion) throws IOException {
        final int savedVersion = prefs.getInt(DVBViewerPreferences.KEY_RS_IVER, -1);
        if (savedVersion >= minimiumVersion) {
            return true;
        } else {
            Version v = dmsInterface.getVersion().execute().body();
            final int newVersion = v.getInternalVersion();
            prefs.getPrefs().edit()
                    .putInt(DVBViewerPreferences.KEY_RS_IVER, newVersion)
                    .apply();
            return newVersion >= minimiumVersion;


        }
    }

}
