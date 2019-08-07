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
package org.dvbviewer.controller

import android.app.Application
import android.util.Log

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.google.android.gms.security.ProviderInstaller
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

import org.dvbviewer.controller.entities.DVBViewerPreferences
import org.dvbviewer.controller.io.HTTPUtil
import org.dvbviewer.controller.utils.Config
import org.dvbviewer.controller.utils.NetUtils
import org.dvbviewer.controller.utils.ServerConsts
import org.dvbviewer.controller.utils.URLUtil


/**
 * The Class App.
 *
 * @author RayBa
 */
class App : Application() {

    private var tracker: Tracker? = null


    /* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
    override fun onCreate() {
        super.onCreate()
        val prefs = DVBViewerPreferences(this)
        Config.IS_FIRST_START = prefs.getBoolean(DVBViewerPreferences.KEY_IS_FIRST_START, true)
        Config.CHANNELS_SYNCED = prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_SYNCED, false)

        val serviceUrl = prefs.getString(DVBViewerPreferences.KEY_DMS_URL)
        val prefPort = prefs.getString(DVBViewerPreferences.KEY_RS_PORT, "8089")
        URLUtil.setRecordingServicesAddress(serviceUrl, prefPort)
        ServerConsts.REC_SERVICE_USER_NAME = prefs.getString(DVBViewerPreferences.KEY_RS_USERNAME, "")
        ServerConsts.REC_SERVICE_PASSWORD = prefs.getString(DVBViewerPreferences.KEY_RS_PASSWORD, "")
        ServerConsts.REC_SERVICE_MAC_ADDRESS = prefs.getString(DVBViewerPreferences.KEY_RS_MAC_ADDRESS)
        ServerConsts.REC_SERVICE_WOL_PORT = 9

        val sendWakeOnLan = prefs.getBoolean(DVBViewerPreferences.KEY_RS_WOL_ON_START, true)
        if (sendWakeOnLan) {
            val wakeOnLanRunnabel = { NetUtils.sendWakeOnLan(prefs, ServerConsts.REC_SERVICE_WOL_PORT) }
            val wakeOnLanThread = Thread(wakeOnLanRunnabel)
            wakeOnLanThread.start()
        }

        installPlayServiceSecurityUpdates()

        val picassoBuilder = Picasso.Builder(applicationContext)
        val downloader = OkHttp3Downloader(HTTPUtil.getHttpClient())
        val picasso = picassoBuilder.downloader(downloader).build()
        Picasso.setSingletonInstance(picasso)
    }

    /**
     * installing latest security fixes through play services, e.g. TLS1.2 support.
     * But dont push anybody by annoying messages to upgrade Play Services, we dont rely on it.
     */
    private fun installPlayServiceSecurityUpdates() {
        try {
            ProviderInstaller.installIfNeeded(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error installing play service features", e)
        }

    }

    @Synchronized
    fun getTracker(): Tracker? {
        if (tracker == null) {
            val analytics = GoogleAnalytics.getInstance(this)
            tracker = analytics.newTracker(R.xml.app_tracker)
        }
        return tracker
    }

    companion object {
        val TAG = "DVBViewerController"
    }


}
