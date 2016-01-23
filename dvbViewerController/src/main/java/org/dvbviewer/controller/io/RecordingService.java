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
 * @author RayBa
 * on 30.01.2015.
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
            Matcher matcher    = getVersionMatcher(rawVersion);
            boolean matchFound = matcher.find();
            if (matchFound) {
                version = getVersionFromMatcher(matcher);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting version from rs", e);
        }
        return version;
    }

    public static String getFfmpegPrefs() {
        String versionXml = null;
        try {
            versionXml = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_FFMPEGPREFS);
        } catch (Exception e) {
            Log.e(TAG, "Error getting version from rs", e);
        }
        return versionXml;
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

    protected static Matcher getVersionMatcher(String version){
        return  versionPattern.matcher(version);
    }

    protected static String getVersionFromMatcher(Matcher matcher){
        return  matcher.group();
    }


}
