package org.dvbviewer.controller.data.recording.retrofit

import okhttp3.MediaType
import okhttp3.RequestBody
import org.dvbviewer.controller.data.entities.Recording
import retrofit2.Converter

internal class RecordingDetailRequestBodyConverter : Converter<Recording, RequestBody> {

    override fun convert(value: Recording): RequestBody {
        return RequestBody.create(MEDIA_TYPE, value.toString())
    }

    companion object {

        private val MEDIA_TYPE = MediaType.parse("text")
    }

}