package org.dvbviewer.controller.data.timer.retrofit;

import com.google.gson.reflect.TypeToken;

import org.dvbviewer.controller.entities.Timer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * A {@linkplain Converter.Factory converter} for the ffmpegprefs.ini files.
 * <p>
 * This converter only applies for class FFMpegPresetList.
 *
 */
public final class TimerConverterFactory extends Converter.Factory {

    /** Create an instance conversion. */
    public static TimerConverterFactory create() {
        return new TimerConverterFactory();
    }

    private final Type timerList;

    private TimerConverterFactory(){
        TypeToken<?> typeToken = TypeToken.getParameterized(List.class, Timer.class);
        timerList = typeToken.getType();
    }


    @Override
    public Converter<ResponseBody, List<Timer>> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        if (type.equals(timerList)) {
            return new TimerPresetResponseBodyConverter();
        }
        return null;
    }

    @Override
    public Converter<List<Timer>, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (type.equals(timerList)) {
            return new TimerRequestBodyConverter();
        }
        return null;
    }

}