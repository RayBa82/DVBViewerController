package org.dvbviewer.controller.data.channel.retrofit;

import com.google.gson.reflect.TypeToken;

import org.dvbviewer.controller.entities.ChannelRoot;

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
public final class ChannelRootConverterFactory extends Converter.Factory {

    /** Create an instance conversion. */
    public static ChannelRootConverterFactory create() {
        return new ChannelRootConverterFactory();
    }

    private final Type channelRootType;

    private ChannelRootConverterFactory(){
        TypeToken<?> typeToken = TypeToken.getParameterized(List.class, ChannelRoot.class);
        channelRootType = typeToken.getType();
    }


    @Override
    public Converter<ResponseBody, List<ChannelRoot>> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        if (type.equals(channelRootType)) {
            return new ChannelRootPresetResponseBodyConverter();
        }
        return null;
    }

    @Override
    public Converter<List<ChannelRoot>, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (type.equals(channelRootType)) {
            return new ChannelRequestBodyConverter();
        }
        return null;
    }

}