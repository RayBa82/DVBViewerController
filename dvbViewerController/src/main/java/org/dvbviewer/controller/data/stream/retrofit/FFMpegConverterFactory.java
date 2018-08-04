package org.dvbviewer.controller.data.stream.retrofit;

import com.google.gson.reflect.TypeToken;

import org.dvbviewer.controller.entities.FFMpegPresetList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

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
public final class FFMpegConverterFactory extends Converter.Factory {

    /** Create an instance conversion. */
    public static FFMpegConverterFactory create() {
        return new FFMpegConverterFactory();
    }

    private final Type ffmpegType;

    private FFMpegConverterFactory(){
        TypeToken<?> typeToken = TypeToken.get(FFMpegPresetList.class);
        ffmpegType = typeToken.getType();
    }


    @Override
    public Converter<ResponseBody, FFMpegPresetList> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        if (type.equals(ffmpegType)) {
            return new FFMpegPresetResponseBodyConverter();
        }
        return null;
    }

    @Override
    public Converter<FFMpegPresetList, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (type.equals(ffmpegType)) {
            return new FFMpegRequestBodyConverter();
        }
        return null;
    }

}