package org.dvbviewer.controller.data.timer.retrofit

import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dvbviewer.controller.data.entities.Timer
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
class TimerConverterFactory private constructor() : Converter.Factory() {

    private val timerList: Type

    init {
        val typeToken = TypeToken.getParameterized(List::class.java, Timer::class.java)
        timerList = typeToken.type
    }


    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>?,
                                       retrofit: Retrofit?): Converter<ResponseBody, List<Timer>>? {
        return if (timerList == type) {
            TimerPresetResponseBodyConverter()
        } else null
    }

    override fun requestBodyConverter(type: Type,
                                      parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<List<Timer>, RequestBody>? {
        return if (timerList == type) {
            TimerRequestBodyConverter()
        } else null
    }

    companion object {

        /** Create an instance conversion.  */
        fun create(): TimerConverterFactory {
            return TimerConverterFactory()
        }
    }

}