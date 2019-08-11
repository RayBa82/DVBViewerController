package org.dvbviewer.controller.data.version

import okhttp3.ResponseBody
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.entities.Timer
import org.dvbviewer.controller.io.api.DMSInterface
import org.dvbviewer.controller.utils.DateUtils
import retrofit2.Call
import java.util.*

class TimerRepository(private val dmsInterface: DMSInterface) {

    fun getTimer(): List<Timer>? {
        return dmsInterface.getTimer().execute().body()
    }

    fun deleteTimer(timerList: List<Timer>): Call<ResponseBody> {
        return dmsInterface.deleteTimer(timerList.map { it.id }.joinToString(","))
    }

    fun saveTimer(timer: Timer): Call<ResponseBody> {
        val params = getTimerParameters(timer)
        var call: Call<ResponseBody>
        call = if(timer.id > 0) {
            dmsInterface.editTimer(params)
        }else {
            dmsInterface.addTimer(params)
        }
        return call
    }

    fun getTimerParameters(timer: Timer): Map<String, String> {
        val params = HashMap<String, String>()
        val startDate = DateUtils.addMinutes(timer.start, timer.pre * -1)
        val stopDate = DateUtils.addMinutes(timer.end, timer.post)
        val days = DateUtils.getDaysSinceDelphiNull(startDate).toString()
        val start = DateUtils.getMinutesOfDay(startDate).toString()
        val stop = DateUtils.getMinutesOfDay(stopDate).toString()
        val endAction = timer.timerAction.toString()
        val pre = timer.pre.toString()
        val post = timer.post.toString()

        params["ch"] = timer.channelId.toString()
        params["dor"] = days
        params["encoding"] = "255"
        params["enable"] = if (timer.isFlagSet(Timer.FLAG_DISABLED)) "0" else "1"
        params["start"] = start
        params["stop"] = stop
        timer.title?.let { params.put("title", it) }
        params["endact"] = endAction
        params["pre"] = pre
        params["post"] = post
        addIfNotEmpty("pdc", timer.pdc, params)
        addIfNotEmpty("epgevent", timer.eventId, params)
        addIfPositive("audio", timer.allAudio, params)
        addIfPositive("subs", timer.dvbSubs, params)
        addIfPositive("ttx", timer.teletext, params)
        addIfPositive("eit", timer.eitEPG, params)
        val epgMonitoring = timer.monitorPDC - 1
        if (epgMonitoring >= 0) {
            params["monitorpdc"] = "1"
            params["monforrec"] = epgMonitoring.toString()
        }
        if (timer.id > 0) {
            params["id"] = timer.id.toString()
        }
        return params
    }

    private fun addIfPositive(name: String, value: Int, params: MutableMap<String, String>) {
        if (value > 0) {
            params[name] = value.toString()
        }
    }

    private fun addIfNotEmpty(name: String, value: String?, params: MutableMap<String, String>) {
        if (StringUtils.isNotBlank(value)) {
            value?.let { params.put(name, it) }
        }
    }

}
