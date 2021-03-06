package org.dvbviewer.controller.data.api

import okhttp3.ResponseBody
import org.dvbviewer.controller.data.entities.*
import org.dvbviewer.controller.data.media.xml.VideoDirsFiles
import org.dvbviewer.controller.data.task.xml.TaskList
import org.dvbviewer.controller.data.version.xml.Version
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface DMSInterface {

    @get:GET(VERSION)
    val version: Call<Version>

    @GET(CHANNEL_LIST)
    fun getChannelList(): Call<List<ChannelRoot>>

    @GET(FAV_LIST)
    fun getFavList(): Call<List<ChannelRoot>>

    @GET(EPG)
    fun getProgramm(@Query("start") start: String, @Query("end") stop: String): Call<List<EpgEntry>>

    @GET(EPG)
    fun getChannelProgramm(@Query("channel") epgId: String, @Query("start") start: String, @Query("end") stop: String): Call<List<EpgEntry>>

    @GET(TASK_API)
    fun getTaskList(@Query("all") all: Int): Call<TaskList>

    @GET(TASK_API)
    fun executeTask(@Query("action") action: String): Call<ResponseBody>

    @GET(RECORDING_LIST)
    fun getRecordings(): Call<List<Recording>>

    @GET(RECORDING_DELETE)
    fun deleteRecording(@Query("recid") recid: String): Call<ResponseBody>

    @GET(TIMER_LIST)
    fun getTimer(): Call<List<Timer>>

    @GET(TIMER_ADD_API)
    fun addTimer(@QueryMap params: Map<String, String>): Call<ResponseBody>

    @GET(TIMER_EDIT)
    fun editTimer(@QueryMap params: Map<String, String>): Call<ResponseBody>

    @GET(TIMER_DELETE)
    fun deleteTimer(@Query("id") id: String): Call<ResponseBody>

    @GET(STATUS)
    fun getStatus(): Call<Status>

    @GET(STATUS2)
    fun getStatus2(): Call<Status>

    @GET(TARGETS)
    fun getTargets(): Call<List<DVBTarget>>

    @GET(TARGETS)
    fun sendCommand(@Query("target") target: String, @Query("cmd") command: String): Call<ResponseBody>

    @GET(MEDIA_DIRS)
    fun getMediaDir(@Query("dirid") id: Long): Call<VideoDirsFiles>

    @GET(CONFIG_FILE)
    fun getConfigFile(@Query("file") file: String): Call<FFMpegPresetList>

    companion object {

        private const val API = "/api"

        const val CHANNEL_LIST = "$API/getchannelsxml.html?logo=1"

        const val FAV_LIST = "$API/getchannelsxml.html?logo=1&favonly=1"

        const val TASK_API = "$API/tasks.html"

        const val EPG = "$API/epg.html?utf8=1&lvl=2"

        const val RECORDING_LIST = "$API/recordings.html?utf8=1&images=1"

        const val RECORDING_DELETE = "$API/recdelete.html"

        const val TIMER_LIST = "$API/timerlist.html?utf8=2"

        const val TIMER_ADD_API = "$API/timeradd.html"

        const val TIMER_EDIT = "$API/timeredit.html"

        const val TIMER_DELETE = "$API/timerdelete.html"

        const val STATUS = "$API/status.html"

        const val STATUS2 = "$API/status2.html"

        const val TARGETS = "$API/dvbcommand.html"

        const val MEDIA_DIRS = "$API/mediafiles.html?content=3&recursive=0&thumbs=1"

        const val VERSION = "$API/version.html"

        const val CONFIG_FILE = "$API/api/getconfigfile.html"
    }

}