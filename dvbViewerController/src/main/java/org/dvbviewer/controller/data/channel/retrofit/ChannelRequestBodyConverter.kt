package org.dvbviewer.controller.data.channel.retrofit

import okhttp3.MediaType
import okhttp3.RequestBody
import org.dvbviewer.controller.entities.ChannelRoot
import retrofit2.Converter

internal class ChannelRequestBodyConverter : Converter<List<ChannelRoot>, RequestBody> {

    override fun convert(value: List<ChannelRoot>): RequestBody {
        return RequestBody.create(MEDIA_TYPE, value.toString())
    }

    companion object {

        private val MEDIA_TYPE = MediaType.parse("text")
    }

}