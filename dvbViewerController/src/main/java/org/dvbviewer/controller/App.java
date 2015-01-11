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

import java.util.HashMap;
import java.util.Map;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpPostSender;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.io.imageloader.AuthImageDownloader;
import org.dvbviewer.controller.utils.Config;
import org.dvbviewer.controller.utils.NetUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.URLUtil;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.app.Application;
import android.text.TextUtils;

/**
 * The Class App.
 *
 * @author RayBa
 * @date 11.08.2012
 */
@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.TOAST, resToastText = R.string.error_sending_report)
public class App extends Application {
	

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		
		/**
		 * Acra initialisation
		 */
		boolean initAcra = getResources().getBoolean(R.bool.init_acra);
		String acraUrl = getResources().getString(R.string.url_acra_error);
		if (initAcra && !TextUtils.isEmpty(acraUrl)) {
			ACRA.init(this);
			Map<ReportField, String> mapping = new HashMap<ReportField, String>();
			mapping.put(ReportField.INSTALLATION_ID, "installationId");
			mapping.put(ReportField.PACKAGE_NAME, "package");
			mapping.put(ReportField.ANDROID_VERSION, "androidVersion");
			mapping.put(ReportField.BRAND, "brand");
			mapping.put(ReportField.PHONE_MODEL, "phoneModel");
			mapping.put(ReportField.APP_VERSION_CODE, "appVerCode");
			mapping.put(ReportField.APP_VERSION_NAME, "appVerName");
			mapping.put(ReportField.STACK_TRACE, "stackTrace");
			// remove any default report sender
			ErrorReporter.getInstance().removeAllReportSenders();
			// create your own instance with your specific mapping
			ErrorReporter.getInstance().addReportSender(new HttpPostSender(acraUrl, mapping));
		}
		
		DVBViewerPreferences prefs = new DVBViewerPreferences(this);
		Config.IS_FIRST_START = prefs.getBoolean(DVBViewerPreferences.KEY_IS_FIRST_START, true);
		Config.CHANNELS_SYNCED = prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_SYNCED, false);

		/**
		 * Read DVBViewer preferences
		 */
		ServerConsts.DVBVIEWER_URL = prefs.getString(DVBViewerPreferences.KEY_DVBV_URL, "http://");
		ServerConsts.DVBVIEWER_PORT = prefs.getString(DVBViewerPreferences.KEY_DVBV_PORT, "80");
		URLUtil.setViewerAddress(ServerConsts.DVBVIEWER_URL, ServerConsts.DVBVIEWER_PORT);
		ServerConsts.DVBVIEWER_USER_NAME = prefs.getString(DVBViewerPreferences.KEY_DVBV_USERNAME, "");
		ServerConsts.DVBVIEWER_PASSWORD = prefs.getString(DVBViewerPreferences.KEY_DVBV_PASSWORD, "");
		
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

	
}
