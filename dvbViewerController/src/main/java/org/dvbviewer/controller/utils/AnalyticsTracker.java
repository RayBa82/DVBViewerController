package org.dvbviewer.controller.utils;

import android.app.Application;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.dvbviewer.controller.App;
import org.dvbviewer.controller.BuildConfig;

/**
 * Class to track some Events for Google Analytics.
 *
 * Created by RayBa on 09.01.16.
 */
public class AnalyticsTracker {

    private static void trackEvent(Application app, String category, String action){
        if (!BuildConfig.DEBUG){
            Tracker t = ((App) app).getTracker();
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .build());
        }
    }

    public static void trackQuickRecordingStream(Application app){
        trackEvent(app, "Streaming", "Quickrecordingstream");
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

    public static void trackMediaStream(Application app){
        trackEvent(app, "MediaStreaming", "Quickstream");
    }

}
