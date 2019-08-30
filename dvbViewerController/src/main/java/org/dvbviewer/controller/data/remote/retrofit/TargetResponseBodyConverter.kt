package org.dvbviewer.controller.data.remote.retrofit


import okhttp3.MediaType
import okhttp3.RequestBody
import org.dvbviewer.controller.data.entities.DVBTarget
import retrofit2.Converter

class TargetResponseBodyConverter : Converter<List<DVBTarget>, RequestBody> {

    override fun convert(value: List<DVBTarget>): RequestBody? {
        return RequestBody.create(MEDIA_TYPE, value.toString())
    }

    companion object {

        private val MEDIA_TYPE = MediaType.parse("text")
    }

}