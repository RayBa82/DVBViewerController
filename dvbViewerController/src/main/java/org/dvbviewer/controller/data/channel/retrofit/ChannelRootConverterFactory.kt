package org.dvbviewer.controller.data.channel.retrofit

import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dvbviewer.controller.data.entities.ChannelRoot
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
class ChannelRootConverterFactory private constructor() : Converter.Factory() {

    private val channelRootType: Type

    init {
        val typeToken = TypeToken.getParameterized(List::class.java, ChannelRoot::class.java)
        channelRootType = typeToken.type
    }


    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, List<ChannelRoot>>? {
        return if (type == channelRootType) {
            ChannelRootPresetResponseBodyConverter()
        } else null
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<List<ChannelRoot>, RequestBody>? {
        return if (type == channelRootType) {
            ChannelRequestBodyConverter()
        } else null
    }

    companion object {

        /** Create an instance conversion.  */
        fun create(): ChannelRootConverterFactory {
            return ChannelRootConverterFactory()
        }
    }

}