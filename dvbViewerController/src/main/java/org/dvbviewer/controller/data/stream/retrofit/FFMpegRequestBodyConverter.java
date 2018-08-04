package org.dvbviewer.controller.data.stream.retrofit;

import org.dvbviewer.controller.entities.FFMpegPresetList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

final class FFMpegRequestBodyConverter implements Converter<FFMpegPresetList, RequestBody> {

    private static final MediaType MEDIA_TYPE = MediaType.parse("text");

    @Override public RequestBody convert(FFMpegPresetList value) {
        return RequestBody.create(MEDIA_TYPE, value.toString());
    }

}