package org.dvbviewer.controller.utils;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.dvbviewer.controller.App;
import org.dvbviewer.controller.BuildConfig;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.io.HTTPUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by rbaun on 09.01.16.
 */
public class AnalyticsTracker {

    private static final MediaType  JSON        = MediaType.parse("application/json; charset=utf-8");
    private static final String     TAG         = "AnalyticsTracker";
    private static Callback         callback;

    public static JSONObject buildTracker() {
        return new JSONObject();
    }

    public static JSONObject addData(@Nullable JSONObject tracker, String key, String data) {
        if (tracker != null){
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
                public void onFailure(Request request, IOException e) {
                    Log.e(TAG, "Error sending AnalyticsData", e);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()){
                        Log.e(TAG, "Error sending AnalyticsData: Status code " + response.code());
                    }
                    response.body().close();
                }
            };
        }
        return callback;
    }

    public static void trackEvent(Application app, String category, String action){
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
            String data = trackingData.toString();
            if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(data)) {
                RequestBody body = RequestBody.create(JSON, data);
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
