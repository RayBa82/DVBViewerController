package org.dvbviewer.controller.data.channel.retrofit;

import org.dvbviewer.controller.entities.ChannelRoot;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

final class ChannelRequestBodyConverter implements Converter<List<ChannelRoot>, RequestBody> {

    private static final MediaType MEDIA_TYPE = MediaType.parse("text");

    @Override public RequestBody convert(List<ChannelRoot> value) {
        return RequestBody.create(MEDIA_TYPE, value.toString());
    }

}