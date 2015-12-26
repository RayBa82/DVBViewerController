package org.dvbviewer.controller.io;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dvbviewer.controller.io.data.TargetHandler;
import org.dvbviewer.controller.io.data.VersionHandler;
import org.dvbviewer.controller.utils.ServerConsts;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by RayBa on 30.01.2015.
 */
public class RecordingService {

    private static  final String TAG = RecordingService.class.getSimpleName();

    private final static Pattern versionPattern    = Pattern.compile("(?!\\.)(\\d+(\\.\\d+)+)(?![\\d\\.])");

    public static String getVersionString() {
        String version = null;
        try {
            String versionXml = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_VERSION);
            VersionHandler versionHandler = new VersionHandler();
            final String rawVersion = versionHandler.parse(versionXml);
            Matcher matcher    = versionPattern.matcher(rawVersion);
            boolean matchFound = matcher.find();
            if (matchFound) {
                version = matcher.group(1);
                Log.d(TAG, "Version number: " + version);
            } else {
                // ugly fallback if anything goes wrong,
                // this will be removed when someone wrote some unit tests for the above regex ;-)
                version = version.replace("DVBViewer Recording Service ", "");
                String[] arr = version.split(" ");
                version = arr[0];
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting version from rs", e);
        }
        return version;
    }

    public static String getDVBViewerTargets() {
        String jsonClients = null;
        try {
            String xml = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_TARGETS);
            TargetHandler handler = new TargetHandler();
            List<String> targets = handler.parse(xml);
            Collections.sort(targets);
            Type type = new TypeToken<List<String>>() {
            }.getType();
            Gson gson = new Gson();
            jsonClients = gson.toJson(targets, type);
        } catch (Exception e) {
            Log.e(TAG, "Error getting DVBViewer Targets", e);
        }
        return jsonClients;
    }


}
