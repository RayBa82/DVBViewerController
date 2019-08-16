package org.dvbviewer.controller.data.epg.retrofit

import okhttp3.MediaType
import okhttp3.RequestBody
import org.dvbviewer.controller.entities.EpgEntry
import retrofit2.Converter

internal class EpgRequestBodyConverter : Converter<List<EpgEntry>, RequestBody> {

    override fun convert(value: List<EpgEntry>): RequestBody {
        return RequestBody.create(MEDIA_TYPE, value.toString())
    }

    companion object {

        private val MEDIA_TYPE = MediaType.parse("text")
    }

}