/*
 * Copyright © 2012 dvbviewer-controller Project
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
package org.dvbviewer.controller;

import android.app.Application;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.io.imageloader.AuthImageDownloader;
import org.dvbviewer.controller.utils.Config;
import org.dvbviewer.controller.utils.NetUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.URLUtil;



/**
 * The Class App.
 *
 * @author RayBa
 * @date 11.08.2012
 */
public class App extends Application {

    private Tracker tracker;
	

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		
		DVBViewerPreferences prefs = new DVBViewerPreferences(this);
		Config.IS_FIRST_START = prefs.getBoolean(DVBViewerPreferences.KEY_IS_FIRST_START, true);
		Config.CHANNELS_SYNCED = prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_SYNCED, false);

		/**
		 * Read Recordingservice Preferences
		 */
		String serviceUrl = prefs.getString(DVBViewerPreferences.KEY_RS_URL, "http://");
		String prefPort = prefs.getString(DVBViewerPreferences.KEY_RS_PORT, "8089");
		URLUtil.setRecordingServicesAddress(serviceUrl, prefPort);
		ServerConsts.REC_SERVICE_USER_NAME = prefs.getString(DVBViewerPreferences.KEY_RS_USERNAME, "");
		ServerConsts.REC_SERVICE_PASSWORD = prefs.getString(DVBViewerPreferences.KEY_RS_PASSWORD, "");
		ServerConsts.REC_SERVICE_LIVE_STREAM_PORT = prefs.getString(DVBViewerPreferences.KEY_RS_LIVE_STREAM_PORT, ServerConsts.REC_SERVICE_LIVE_STREAM_PORT);
		ServerConsts.REC_SERVICE_MEDIA_STREAM_PORT = prefs.getString(DVBViewerPreferences.KEY_RS_MEDIA_STREAM_PORT, ServerConsts.REC_SERVICE_MEDIA_STREAM_PORT);
		ServerConsts.REC_SERVICE_MAC_ADDRESS = prefs.getString(DVBViewerPreferences.KEY_RS_MAC_ADDRESS);
		ServerConsts.REC_SERVICE_WOL_PORT = prefs.getInt(DVBViewerPreferences.KEY_RS_WOL_PORT, 9);
		super.onCreate();
		
		/**
		 * Thread to send a wake on lan request
		 */
		Runnable wakeOnLanRunnabel = new Runnable() {
			
			@Override
			public void run() {
				NetUtils.sendWakeOnLan(ServerConsts.REC_SERVICE_MAC_ADDRESS, ServerConsts.REC_SERVICE_WOL_PORT);
			}
		};
		
		boolean sendWakeOnLan = prefs.getBoolean(DVBViewerPreferences.KEY_RS_WOL_ON_START, true);
		if (sendWakeOnLan && !TextUtils.isEmpty(ServerConsts.REC_SERVICE_MAC_ADDRESS)) {
			Thread wakeOnLanThread = new Thread(wakeOnLanRunnabel);
			wakeOnLanThread.start();
		}
		
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.displayer(new FadeInBitmapDisplayer(500, true, true, false))
		.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		.imageDownloader(new AuthImageDownloader(getApplicationContext()))
		.defaultDisplayImageOptions(options)
		.build();

		ImageLoader.getInstance().init(config);
	}

    public synchronized Tracker getTracker(){
        if (tracker == null){
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.app_tracker);
        }
        return tracker;
    }

	
}
