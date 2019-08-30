package org.dvbviewer.controller.data.timer.retrofit


import okhttp3.ResponseBody
import org.dvbviewer.controller.data.api.handler.TimerHandler
import org.dvbviewer.controller.data.entities.Timer
import retrofit2.Converter

class TimerPresetResponseBodyConverter : Converter<ResponseBody, List<Timer>> {

    private val prefsHandler = TimerHandler()

    override fun convert(value: ResponseBody): List<Timer>? {
        try {
            return prefsHandler.parse(value.byteStream())
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            value.close()
        }
    }

}