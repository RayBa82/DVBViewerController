package org.dvbviewer.controller.data.timer.retrofit

import okhttp3.MediaType
import okhttp3.RequestBody
import org.dvbviewer.controller.data.entities.Timer
import retrofit2.Converter

internal class TimerRequestBodyConverter : Converter<List<Timer>, RequestBody> {

    override fun convert(value: List<Timer>): RequestBody {
        return RequestBody.create(MEDIA_TYPE, value.toString())
    }

    companion object {

        private val MEDIA_TYPE = MediaType.parse("text")
    }

}