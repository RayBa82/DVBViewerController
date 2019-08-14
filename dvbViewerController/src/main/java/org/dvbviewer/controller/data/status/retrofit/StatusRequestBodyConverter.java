package org.dvbviewer.controller.data.status.retrofit;

import org.dvbviewer.controller.entities.Status;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

final class StatusRequestBodyConverter implements Converter<Status, RequestBody> {

    private static final MediaType MEDIA_TYPE = MediaType.parse("text");

    @Override public RequestBody convert(Status value) {
        return RequestBody.create(MEDIA_TYPE, value.toString());
    }

}