package org.dvbviewer.controller.data.version

import okhttp3.ResponseBody
import org.dvbviewer.controller.entities.Timer
import org.dvbviewer.controller.io.api.DMSInterface
import retrofit2.Call

class TimerRepository(private val dmsInterface: DMSInterface) {

    fun getTimer(): List<Timer>? {
        return dmsInterface.getTimer().execute().body()
    }

    fun deleteTimer(timerList: List<Timer>): Call<ResponseBody> {
        return dmsInterface.deleteTimer(timerList.map { it.id }.joinToString(","))
    }

}
