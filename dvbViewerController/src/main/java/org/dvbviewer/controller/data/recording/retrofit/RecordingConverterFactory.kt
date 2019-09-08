package org.dvbviewer.controller.data.recording.retrofit

import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dvbviewer.controller.data.entities.Recording
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
class RecordingConverterFactory private constructor() : Converter.Factory() {

    private val typeToCheck: Type

    init {
        val typeToken = TypeToken.getParameterized(List::class.java, Recording::class.java)
        typeToCheck = typeToken.type
    }


    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, List<Recording>>? {
        return if (typeToCheck == type) {
            RecordingPresetResponseBodyConverter()
        } else null
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<List<Recording>, RequestBody>? {
        return if (typeToCheck == type) {
            RecordingRequestBodyConverter()
        } else null
    }

    companion object {

        /** Create an instance conversion.  */
        fun create(): RecordingConverterFactory {
            return RecordingConverterFactory()
        }
    }

}