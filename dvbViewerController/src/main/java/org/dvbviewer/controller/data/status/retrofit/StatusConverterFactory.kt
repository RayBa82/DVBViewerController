package org.dvbviewer.controller.data.status.retrofit

import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dvbviewer.controller.entities.Status
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class StatusConverterFactory private constructor() : Converter.Factory() {

    private val statusType: Type

    init {
        val typeToken = TypeToken.get(Status::class.java)
        statusType = typeToken.type
    }


    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, Status>? {
        return if (type == statusType) {
            StatusPresetResponseBodyConverter()
        } else null
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<Status, RequestBody>? {
        return if (type == statusType) {
            StatusRequestBodyConverter()
        } else null
    }

    companion object {

        /** Create an instance conversion.  */
        fun create(): StatusConverterFactory {
            return StatusConverterFactory()
        }
    }

}