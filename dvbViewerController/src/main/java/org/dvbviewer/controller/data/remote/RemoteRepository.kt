package org.dvbviewer.controller.data.remote

import okhttp3.ResponseBody
import org.dvbviewer.controller.data.api.DMSInterface
import org.dvbviewer.controller.data.entities.DVBTarget
import retrofit2.Call

class RemoteRepository(private val dmsInterface: DMSInterface) {

    fun getTargets(): List<DVBTarget>? {
        return dmsInterface.getTargets().execute().body()
    }

    fun sendCommand(target: String, command: String): Call<ResponseBody> {
        return dmsInterface.sendCommand(target, "-x$command")
    }

    fun switchChannel(target: String, channel: String): Call<ResponseBody> {
        return dmsInterface.sendCommand(target, "-c:$channel")
    }

}
