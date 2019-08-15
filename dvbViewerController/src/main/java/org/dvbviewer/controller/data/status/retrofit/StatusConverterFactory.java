package org.dvbviewer.controller.data.status.retrofit;

import com.google.gson.reflect.TypeToken;

import org.dvbviewer.controller.entities.Status;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class StatusConverterFactory extends Converter.Factory {

    /** Create an instance conversion. */
    public static StatusConverterFactory create() {
        return new StatusConverterFactory();
    }

    private final Type statusType;

    private StatusConverterFactory(){
        TypeToken<?> typeToken = TypeToken.get(Status.class);
        statusType = typeToken.getType();
    }


    @Override
    public Converter<ResponseBody, Status> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        if (type.equals(statusType)) {
            return new StatusPresetResponseBodyConverter();
        }
        return null;
    }

    @Override
    public Converter<Status, RequestBody> requestBodyConverter(Type type,
                                                                Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (type.equals(statusType)) {
            return new StatusRequestBodyConverter();
        }
        return null;
    }

}