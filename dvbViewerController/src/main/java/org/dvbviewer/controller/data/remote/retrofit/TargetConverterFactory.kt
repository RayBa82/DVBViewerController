package org.dvbviewer.controller.data.remote.retrofit

import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dvbviewer.controller.entities.DVBTarget
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * A [converter][Converter.Factory] for the ffmpegprefs.ini files.
 *
 *
 * This converter only applies for class FFMpegPresetList.
 *
 */
class TargetConverterFactory private constructor() : Converter.Factory() {

    private val targetType: Type

    init {
        val typeToken = TypeToken.getParameterized(List::class.java, DVBTarget::class.java)
        targetType = typeToken.type
    }


    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, List<DVBTarget>>? {
        return if (type == targetType) {
            TargetPresetResponseBodyConverter()
        } else null
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<List<DVBTarget>, RequestBody>? {
        return if (type == targetType) {
            TargetResponseBodyConverter()
        } else null
    }

    companion object {

        /** Create an instance conversion.  */
        fun create(): TargetConverterFactory {
            return TargetConverterFactory()
        }
    }

}