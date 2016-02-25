package org.dvbviewer.controller.utils;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.dvbviewer.controller.App;
import org.dvbviewer.controller.BuildConfig;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.io.HTTPUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Class to track some Events for Google Analytics.
 *
 * Created by RayBa on 09.01.16.
 */
public class AnalyticsTracker {

    private static final MediaType JSON        = MediaType.parse("application/json; charset=utf-8");
    private static final String     TAG         = "AnalyticsTracker";
    private static Callback callback;

    public static JSONObject buildTracker() {
        return new JSONObject();
    }

    public static JSONObject addData(@Nullable JSONObject tracker, String key, String data) {
        if (tracker != null && !TextUtils.isEmpty(data)){
            try {
                tracker.put(key,data);
            } catch (JSONException e) {
                tracker = addData(tracker,key, "Error creating JsonData from String");
            }
        }
        return tracker;
    }

    private static Callback getCallback() {
        if (callback == null) {
            callback = new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error sending AnalyticsData", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()){
                        Log.e(TAG, "Error sending AnalyticsData: Status code " + response.code());
                    }
                    response.body().close();
                }

            };
        }
        return callback;
    }

    private static void trackEvent(Application app, String category, String action){
        if (!BuildConfig.DEBUG){
            Tracker t = ((App) app).getTracker();
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());
        }
    }

    public static void trackSync(Context context, JSONObject trackingData){
        try {
            String url = context.getString(R.string.tracking_url);
            if (!TextUtils.isEmpty(url) && trackingData.length() != 0) {
                RequestBody body = RequestBody.create(JSON, trackingData.toString());
                HTTPUtil.executeAsync(url, "", "", getCallback(), body);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending AnalyticsData", e);
        }
    }

    public static void trackQuickStream(Application app){
        trackEvent(app, "Streaming", "Quickstream");
    }

    public static void trackTranscodedStream(Application app){
        trackEvent(app, "Streaming", "Transcoded stream");
    }

    public static void trackDirectStream(Application app){
        trackEvent(app, "Streaming", "Direct stream");
    }

}
