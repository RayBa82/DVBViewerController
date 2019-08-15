package org.dvbviewer.controller.data.epg.retrofit;

import com.google.gson.reflect.TypeToken;

import org.dvbviewer.controller.entities.EpgEntry;

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
public final class EpgConverterFactory extends Converter.Factory {

    /** Create an instance conversion. */
    public static EpgConverterFactory create() {
        return new EpgConverterFactory();
    }

    private final Type epgType;

    private EpgConverterFactory(){
        TypeToken<?> typeToken = TypeToken.getParameterized(List.class, EpgEntry.class);
        epgType = typeToken.getType();
    }


    @Override
    public Converter<ResponseBody, List<EpgEntry>> responseBodyConverter(Type type, Annotation[] annotations,
                                                                         Retrofit retrofit) {
        if (type.equals(epgType)) {
            return new EpgPresetResponseBodyConverter();
        }
        return null;
    }

    @Override
    public Converter<List<EpgEntry>, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (type.equals(epgType)) {
            return new EpgRequestBodyConverter();
        }
        return null;
    }

}