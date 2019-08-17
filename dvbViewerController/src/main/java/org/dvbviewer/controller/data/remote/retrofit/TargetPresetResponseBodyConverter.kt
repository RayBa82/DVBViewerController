package org.dvbviewer.controller.data.remote.retrofit


import okhttp3.ResponseBody
import org.dvbviewer.controller.data.entities.DVBTarget
import org.dvbviewer.controller.io.data.TargetHandler
import retrofit2.Converter

class TargetPresetResponseBodyConverter : Converter<ResponseBody, List<DVBTarget>> {

    private val handler = TargetHandler()

    override fun convert(value: ResponseBody): List<DVBTarget>? {
        try {
            return handler.parse(value.byteStream())
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            value.close()
        }
    }

}