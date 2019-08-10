package org.dvbviewer.controller.io.api

import okhttp3.ResponseBody
import org.dvbviewer.controller.data.media.xml.VideoDirsFiles
import org.dvbviewer.controller.data.task.xml.TaskList
import org.dvbviewer.controller.data.version.xml.Version
import org.dvbviewer.controller.entities.FFMpegPresetList
import org.dvbviewer.controller.entities.Timer
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface DMSInterface {

    @get:GET(VERSION)
    val version: Call<Version>


    @GET(TASK_API)
    fun getTaskList(@Query("all") all: Int): Call<TaskList>

    @GET(TASK_API)
    fun executeTask(@Query("action") action: String): Call<ResponseBody>

    @GET(REC_DEL_API)
    fun deleteRecording(@Query("recid") id: Long, @Query("delfile") delete: Int): Call<ResponseBody>

    @GET(TIMER_LIST)
    fun getTimer(): Call<List<Timer>>

    @GET(TIMER_ADD_API)
    fun addTimer(@QueryMap params: Map<String, String>): Call<ResponseBody>

    @GET(TIMER_DELETE)
    fun deleteTimer(@Query("id") id: String): Call<ResponseBody>

    @GET(MEDIA_DIRS)
    fun getMediaDir(@Query("dirid") id: Long): Call<VideoDirsFiles>

    @GET(CONFIG_FILE)
    fun getConfigFile(@Query("file") file: String): Call<FFMpegPresetList>

    companion object {

        const val API = "/api"

        const val TASK_API = "$API/tasks.html"

        const val REC_DEL_API = "$API/recdelete.html"

        const val TIMER_LIST = "$API/timerlist.html?utf8=2"

        const val TIMER_ADD_API = "$API/timeradd.html"

        const val TIMER_DELETE = "$API/timerdelete.html"

        const val MEDIA_DIRS = "$API/mediafiles.html?content=3&recursive=0&thumbs=1"

        const val VERSION = "$API/version.html"

        const val CONFIG_FILE = "$API/api/getconfigfile.html"
    }

}