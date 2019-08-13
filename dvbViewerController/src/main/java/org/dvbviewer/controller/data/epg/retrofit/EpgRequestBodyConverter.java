package org.dvbviewer.controller.data.epg.retrofit;

import org.dvbviewer.controller.entities.EpgEntry;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

final class EpgRequestBodyConverter implements Converter<List<EpgEntry>, RequestBody> {

    private static final MediaType MEDIA_TYPE = MediaType.parse("text");

    @Override public RequestBody convert(List<EpgEntry> value) {
        return RequestBody.create(MEDIA_TYPE, value.toString());
    }

}