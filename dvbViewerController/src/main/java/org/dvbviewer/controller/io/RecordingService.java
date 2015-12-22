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

/**
 * Created by RayBa on 30.01.2015.
 */
public class RecordingService {

    public static String getVersionString() {
        String version = null;
        try {
            String versionXml = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_VERSION);
            VersionHandler versionHandler = new VersionHandler();
            version = versionHandler.parse(versionXml);
            //here is a regex required!
            version = version.replace("DVBViewer Recording Service ", "");
            String[] arr = version.split(" ");
            version = arr[0];

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(RecordingService.class.getSimpleName(), "Error getting version from rs");
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
            Log.e(RecordingService.class.getSimpleName(), "Error getting DVBViewer Targets");
            e.printStackTrace();
        }
        return jsonClients;
    }


}
