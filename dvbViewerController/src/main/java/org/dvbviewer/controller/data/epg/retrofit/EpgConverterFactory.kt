package org.dvbviewer.controller.data.epg.retrofit

import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dvbviewer.controller.data.entities.EpgEntry
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
class EpgConverterFactory private constructor() : Converter.Factory() {

    private val epgType: Type

    init {
        val typeToken = TypeToken.getParameterized(List::class.java, EpgEntry::class.java)
        epgType = typeToken.type
    }


    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, List<EpgEntry>>? {
        return if (type == epgType) {
            EpgPresetResponseBodyConverter()
        } else null
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<List<EpgEntry>, RequestBody>? {
        return if (type == epgType) {
            EpgRequestBodyConverter()
        } else null
    }

    companion object {

        /** Create an instance conversion.  */
        fun create(): EpgConverterFactory {
            return EpgConverterFactory()
        }
    }

}