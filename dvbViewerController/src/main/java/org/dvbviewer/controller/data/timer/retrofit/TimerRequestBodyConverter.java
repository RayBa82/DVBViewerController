package org.dvbviewer.controller.data.timer.retrofit;

import org.dvbviewer.controller.entities.Timer;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

final class TimerRequestBodyConverter implements Converter<List<Timer>, RequestBody> {

    private static final MediaType MEDIA_TYPE = MediaType.parse("text");

    @Override public RequestBody convert(List<Timer> value) {
        return RequestBody.create(MEDIA_TYPE, value.toString());
    }

}