package org.dvbviewer.controller.data.stream.retrofit;

import org.dvbviewer.controller.entities.FFMpegPresetList;
import org.simpleframework.xml.core.Persister;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * A {@linkplain Converter.Factory converter} which uses Simple Framework for XML.
 * <p>
 * This converter only applies for class types. Parameterized types (e.g., {@code List<Foo>}) are
 * not handled.
 *
 */
public final class FFMpegConverterFactory extends Converter.Factory {
    /** Create an instance using a default {@link Persister} instance for conversion. */


    public static FFMpegConverterFactory create() {
        return new FFMpegConverterFactory();
    }


    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        if (!(type.getClass().equals(FFMpegPresetList.class.getClass()))) {
            return null;
        }
        return new FFMpegPresetResponseBodyConverter();
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (!(type.getClass().equals(FFMpegPresetList.class.getClass()))) {
            return null;
        }
        return new FFMpegRequestBodyConverter<>(null);
    }
}
