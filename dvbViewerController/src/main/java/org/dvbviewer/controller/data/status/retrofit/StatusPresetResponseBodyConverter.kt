package org.dvbviewer.controller.data.status.retrofit


import okhttp3.ResponseBody
import org.dvbviewer.controller.data.entities.Status
import org.dvbviewer.controller.io.data.Status2Handler
import retrofit2.Converter

class StatusPresetResponseBodyConverter : Converter<ResponseBody, Status> {

    private val handler = Status2Handler()

    override fun convert(value: ResponseBody): Status? {
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