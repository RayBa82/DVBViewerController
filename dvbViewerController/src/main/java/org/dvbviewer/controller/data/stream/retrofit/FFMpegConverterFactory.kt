package org.dvbviewer.controller.data.stream.retrofit

import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dvbviewer.controller.data.entities.FFMpegPresetList
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
class FFMpegConverterFactory private constructor() : Converter.Factory() {

    private val ffmpegType: Type

    init {
        val typeToken = TypeToken.get(FFMpegPresetList::class.java)
        ffmpegType = typeToken.type
    }


    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, FFMpegPresetList>? {
        return if (type == ffmpegType) {
            FFMpegPresetResponseBodyConverter()
        } else null
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<FFMpegPresetList, RequestBody>? {
        return if (type == ffmpegType) {
            FFMpegRequestBodyConverter()
        } else null
    }

    companion object {

        /** Create an instance conversion.  */
        fun create(): FFMpegConverterFactory {
            return FFMpegConverterFactory()
        }
    }

}