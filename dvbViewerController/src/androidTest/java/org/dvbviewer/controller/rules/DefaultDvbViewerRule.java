package org.dvbviewer.controller.rules;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;


import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.test.R;
import org.dvbviewer.controller.util.TestUtils;
import org.dvbviewer.controller.utils.Config;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.URLUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.HttpUrl;


/**
 * Created by r.baun on 14.06.2015.
 *
 * Default template for an {@link ActivityTestRule}. It starts a {@link MockWebServer} to provide
 * basic functionallity for testing.
 */
public class DefaultDvbViewerRule<T extends Activity> extends ActivityTestRule {

    protected final Context context = InstrumentationRegistry.getInstrumentation().getContext();
    protected final Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    protected final DVBViewerPreferences prefs = new DVBViewerPreferences(targetContext);
    public final String GENERIC_CHANNEL_SUFFIX = "?utf8=1&lvl=2&channel";
    protected MockWebServer server;
    protected static String VERSION_KEY = "version";
    protected static String CHANNELS_KEY = "channels";
    protected static String FAVOURITES_KEY = "favourites";
    protected static String STATUS_KEY = "status";
    protected static String STATUS2_KEY = "status2";
    protected static String FFMPEGPREFS_KEY = "ffmpegPrefsIni";
    protected static String TARGETS_KEY = "dvbviewerTargets";

    public DefaultDvbViewerRule(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        // Create a MockWebServer. These are lean enough that you can create a new
        // instance for every unit test.
        server = new MockWebServer();
        // Schedule some responses.
        try {
            String user1 = TestUtils.getStringFromFile(context, R.raw.user_1);
            JSONObject userJson = new JSONObject(user1);
            final String version = getJsonString(userJson, VERSION_KEY);
            final String channels = getJsonString(userJson, CHANNELS_KEY);
            final String favourites = getJsonString(userJson, FAVOURITES_KEY);
            final String status = getJsonString(userJson, STATUS_KEY);
            final String status2 = getJsonString(userJson, STATUS2_KEY);
            final String ffmpegPrefs = getJsonString(userJson, FFMPEGPREFS_KEY);
            final String targets = getJsonString(userJson, TARGETS_KEY);
            final String defaultEpg = TestUtils.getStringFromFile(context, R.raw.default_epg);
            server.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request)
                        throws InterruptedException {
                    Log.i(DefaultDvbViewerRule.class.getSimpleName(), "requestPath: " + request.getPath());
                    MockResponse response = new MockResponse();

                    if (request.getPath().contains(ServerConsts.URL_EPG + GENERIC_CHANNEL_SUFFIX)) {
                        response.setBody(defaultEpg);
                    } else if (request.getPath().equalsIgnoreCase(ServerConsts.URL_STATUS)) {
                        response.setBody(status);
                    } else if (request.getPath().equalsIgnoreCase(ServerConsts.URL_VERSION)) {
                        response.setBody(version);
                    } else if (request.getPath().equalsIgnoreCase(ServerConsts.URL_CHANNELS)) {
                        response.setBody(channels);
                    } else if (request.getPath().equalsIgnoreCase(ServerConsts.URL_FAVS)) {
                        response.setBody(favourites);
                    } else if (request.getPath().startsWith(ServerConsts.URL_EPG)) {
                        response.setBody("");
                    } else if (request.getPath().equalsIgnoreCase(ServerConsts.URL_TARGETS)) {
                        response.setBody(targets);
                    }else if (request.getPath().equalsIgnoreCase(ServerConsts.URL_STATUS2)) {
                        response.setBody(status2);
                    }else if (request.getPath().equalsIgnoreCase(ServerConsts.URL_FFMPEGPREFS)) {
                        response.setBody(ffmpegPrefs);
                    }
                    return response; // this could have been more sophisticated
                }
            });
            server.start();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        // Ask the server for its URL. You'll need this to make HTTP requests.
        HttpUrl serverUrl = server.url("");
        URL baseUrl = serverUrl.url();
        Config.IS_FIRST_START = false;
        Config.CHANNELS_SYNCED = false;
        URLUtil.setRecordingServicesAddress(baseUrl.getProtocol() + "://" + baseUrl.getHost(), String.valueOf(baseUrl.getPort()));
    }

    protected void afterActivityFinished() {
        try {
            server.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String getJsonString(JSONObject json, String key) {
        String result = "";
        if (json.has(key)) {
            try {
                result = json.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
