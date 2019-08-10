package org.dvbviewer.controller.io

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.io.data.TargetHandler
import org.dvbviewer.controller.io.data.VersionHandler
import org.dvbviewer.controller.utils.ServerConsts
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author RayBa
 * on 30.01.2015.
 */
object RecordingService {

    private val TAG = RecordingService::class.java.simpleName

    private val versionPattern = Pattern.compile("(?!\\.)(\\d+(\\.\\d+)+)(?![\\d.])")

    fun getVersionString(): String? {
        var version: String? = null
        try {
            val stream = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_VERSION)
            stream.use {
                val versionHandler = VersionHandler()
                val rawVersion = versionHandler.parse(it)
                val matcher = getVersionMatcher(rawVersion)
                val matchFound = matcher.find()
                if (matchFound) {
                    version = getVersionFromMatcher(matcher)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting version from rs", e)
        }
        return version
    }

    fun getDvbViewerTargets(): String? {
            try {
                val stream = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_TARGETS)
                stream.use {
                    val handler = TargetHandler()
                    val targets = handler.parse(it)
                    Collections.sort(targets)
                    val type = object : TypeToken<List<String>>() {
                    }.type
                    val gson = Gson()
                    return gson.toJson(targets, type)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting DVBViewer Targets", e)
            }
            return StringUtils.EMPTY
        }

    fun getVersionMatcher(version: String): Matcher {
        return versionPattern.matcher(version)
    }

    fun getVersionFromMatcher(matcher: Matcher): String {
        return matcher.group()
    }


}
