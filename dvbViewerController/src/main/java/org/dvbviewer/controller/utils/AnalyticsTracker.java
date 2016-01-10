package org.dvbviewer.controller.utils;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.dvbviewer.controller.App;
import org.dvbviewer.controller.BuildConfig;
import org.dvbviewer.controller.R;

import java.io.IOException;

/**
 * Created by rbaun on 09.01.16.
 */
public class AnalyticsTracker {

    private static final MediaType      JSON        = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient   client      = new OkHttpClient();
    private static final String         TAG         = "AnalyticsTracker";

    private static Callback callback;

    public static void trackEvent(Application app, String category, String action){
        if (!BuildConfig.DEBUG){
            Tracker t = ((App) app).getTracker();
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());
        }
    }

    public static void trackSync(Context context, String trackingData){
        try {
            String url = context.getString(R.string.tracking_url);
            if (!TextUtils.isEmpty(url)){
                RequestBody body = RequestBody.create(JSON, trackingData);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                callback = new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.e(TAG, "Error sending AnalyticsData", e);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()){
                            Log.e(TAG, "Error sending AnalyticsData: Status code " +response.code());
                        }
                    }
                };
                client.newCall(request).enqueue(callback);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
