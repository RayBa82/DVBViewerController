package org.dvbviewer.controller.data.stream.retrofit

import okhttp3.MediaType
import okhttp3.RequestBody
import org.dvbviewer.controller.data.entities.FFMpegPresetList
import retrofit2.Converter

internal class FFMpegRequestBodyConverter : Converter<FFMpegPresetList, RequestBody> {

    override fun convert(value: FFMpegPresetList): RequestBody {
        return RequestBody.create(MEDIA_TYPE, value.toString())
    }

    companion object {

        private val MEDIA_TYPE = MediaType.parse("text")
    }

}