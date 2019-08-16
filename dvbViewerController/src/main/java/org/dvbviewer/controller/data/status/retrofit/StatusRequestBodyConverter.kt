package org.dvbviewer.controller.data.status.retrofit

import okhttp3.MediaType
import okhttp3.RequestBody
import org.dvbviewer.controller.entities.Status
import retrofit2.Converter

internal class StatusRequestBodyConverter : Converter<Status, RequestBody> {

    override fun convert(value: Status): RequestBody {
        return RequestBody.create(MEDIA_TYPE, value.toString())
    }

    companion object {

        private val MEDIA_TYPE = MediaType.parse("text")
    }

}