package org.dvbviewer.controller.data.stream

import android.content.Context
import android.util.Log
import org.dvbviewer.controller.R
import org.dvbviewer.controller.entities.FFMpegPresetList
import org.dvbviewer.controller.io.api.DMSInterface
import org.dvbviewer.controller.io.data.FFMPEGPrefsHandler

/**
 * Created by rbaun on 02.04.18.
 */

class StreamRepository(private val dmsInterface: DMSInterface) {

    private val TAG = StreamRepository::class.java.name

    fun getFFMpegPresets(context: Context): FFMpegPresetList {
        val prefsHandler = FFMPEGPrefsHandler()
        val result = getPrefs(context, IPHONE_PREFS,
                prefsHandler,
                R.raw.iphoneprefs)
        val ffMpegPrefs = getPrefs(context, FFMPEG_PREFS,
                prefsHandler, R.raw.ffmpegprefs)
        result.presets.addAll(ffMpegPrefs.presets)
        return result
    }

    private fun getPrefs(context: Context, file: String, handler: FFMPEGPrefsHandler, defaults: Int): FFMpegPresetList {
        var result: FFMpegPresetList
        try {
            result = dmsInterface.getConfigFile(file).execute().body()!!
        } catch (e: Exception) {
            result =  getDefaultPrefs(context, handler, defaults)
        }
       return result
    }

    private fun getDefaultPrefs(context: Context, handler: FFMPEGPrefsHandler, defaults: Int): FFMpegPresetList {
        try {
            val res = context.resources
            val stream = res.openRawResource(defaults)
            stream.use {
                val b = ByteArray(it.available())
                stream.read(b)
                return handler.parse(String(b))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error reading default presets", e)
        }

        return FFMpegPresetList()
    }

    companion object {

        private const val IPHONE_PREFS = "config\\iphoneprefs.ini"
        private const val FFMPEG_PREFS = "config\\ffmpegprefs.ini"
    }

}