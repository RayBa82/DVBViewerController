package org.dvbviewer.controller.data.recording.retrofit


import okhttp3.ResponseBody
import org.dvbviewer.controller.data.api.handler.RecordingHandler
import org.dvbviewer.controller.data.entities.Recording
import retrofit2.Converter

class RecordingPresetResponseBodyConverter : Converter<ResponseBody, List<Recording>> {

    private val handler = RecordingHandler()

    override fun convert(value: ResponseBody): List<Recording>? {
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