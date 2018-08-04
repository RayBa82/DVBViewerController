package org.dvbviewer.controller.data.version

import android.content.Context
import org.dvbviewer.controller.entities.DVBViewerPreferences
import org.dvbviewer.controller.io.api.DMSInterface

/**
 * Created by rbaun on 02.04.18.
 */

class VersionRepository(context: Context, private val dmsInterface: DMSInterface) {

    private val prefs = DVBViewerPreferences(context)

    fun isSupported(minimumVersion: Int): Boolean {
        val savedVersion = prefs.getInt(DVBViewerPreferences.KEY_RS_IVER, -1)
        if (savedVersion >= minimumVersion) {
            return true
        }
        val v = dmsInterface.version.execute().body()
        val newVersion = v!!.internalVersion
        prefs.prefs.edit()
                .putInt(DVBViewerPreferences.KEY_RS_IVER, newVersion)
                .apply()
        return newVersion >= minimumVersion
    }

    fun isSupported(minimumVersion: String): Boolean {
        val version = minimumVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val int1 = Integer.valueOf(version[0]) shl 24
        val int2 = Integer.valueOf(version[1]) shl 16
        val int3 = Integer.valueOf(version[2]) shl 8
        val int4 = Integer.valueOf(version[3])
        val versionNumber = int1 + int2 + int3 + int4
        return isSupported(versionNumber)
    }

}
