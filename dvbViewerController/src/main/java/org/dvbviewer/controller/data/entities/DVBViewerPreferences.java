/*
 * Copyright © 2013 dvbviewer-controller Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dvbviewer.controller.data.entities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wrapper Class For Application Preferencefile.
 */
public class DVBViewerPreferences {

    private SharedPreferences prefs;
    private SharedPreferences streamPrefs;
    /**
     * Name of preferences xml-file
     */
    public static final String PREFS = "dvbviewer_preferences";
    /**
     * Name of stream preferences xml-file
     */
    private static final String STREAM_PREFS = "dvbviewer_stream_preferences";

    /**
     * Preferences Keys
     */
    public static final String KEY_IS_FIRST_START = "KEY_IS_FIRST_START";
    public static final String KEY_RS_IVER = "KEY_RS_IVER_VERSION";
    public static final String KEY_DMS_URL = "KEY_RS_URL";
    public static final String KEY_RS_PORT = "KEY_RS_PORT";
    public static final String KEY_RS_MAC_ADDRESS = "KEY_RS_MAC_ADDRESS";
    public static final String KEY_RS_WOL_ON_START = "KEY_RS_WOL_ON_START";
    public static final String KEY_RS_USERNAME = "KEY_RS_USERNAME";
    public static final String KEY_RS_PASSWORD = "KEY_RS_PASSWORD";
    public static final String KEY_RS_CLIENTS = "KEY_RS_CLIENTS";
    public static final String KEY_SELECTED_CLIENT = "KEY_SELECTED_CLIENT";
    public static final String KEY_CHANNELS_SHOW_NOW_PLAYING = "KEY_CHANNELS_SHOW_NOW_PLAYING";
    public static final String KEY_CHANNELS_SHOW_NOW_PLAYING_WIFI_ONLY = "KEY_CHANNELS_SHOW_NOW_PLAYING_WIFI_ONLY";
    public static final String KEY_CHANNELS_USE_FAVS = "KEY_CHANNELS_USE_FAVS";
    public static final String KEY_CHANNELS_SYNCED = "KEY_CHANNELS_SYNCED_1_5_0-5-3";
    public static final String KEY_TIMER_TIME_BEFORE = "KEY_TIMER_TIME_BEFORE";
    public static final String KEY_TIMER_TIME_AFTER = "KEY_TIMER_TIME_AFTER";
    public static final String KEY_TIMER_DEF_AFTER_RECORD = "KEY_TIMER_DEF_AFTER_RECORD";

    public static final int DEFAULT_TIMER_TIME_BEFORE = 5;
    public static final int DEFAULT_TIMER_TIME_AFTER = 10;

    /**
     * Streaming Preferences Keys
     */
    public static final String KEY_STREAM_DIRECT = "KEY_STREAM_DIRECT";
    public static final String KEY_STREAM_PRESET = "KEY_STREAM_PRESET";
    public static final String KEY_STREAM_ENCODING_SPEED = "KEY_STREAM_FFMPEG_PRESET";

    /**
     * Instantiates a new dVB viewer preferences.
     *
     * @param context the context
     */
    public DVBViewerPreferences(Context context) {
        this.prefs = context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
        this.streamPrefs = context.getSharedPreferences(STREAM_PREFS, Activity.MODE_PRIVATE);
    }

    /**
     * Gets the app shared prefs.
     *
     * @return the app shared prefs
     */
    public SharedPreferences getPrefs() {
        return prefs;
    }

    /**
     * Gets the string.
     *
     * @param key the key
     * @return the string
     */
    public String getString(String key) {
        return prefs.getString(key, "");
    }

    /**
     * Gets the string.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the string
     */
    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    /**
     * Gets the int.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the int
     */
    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    /**
     * Gets the boolean.
     *
     * @param key      the key
     * @param defValue the def value
     * @return the boolean
     */
    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public SharedPreferences getStreamPrefs() {
        return streamPrefs;
    }
}
