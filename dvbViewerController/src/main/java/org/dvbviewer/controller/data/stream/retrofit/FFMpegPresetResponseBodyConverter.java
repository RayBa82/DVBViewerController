package org.dvbviewer.controller.data.stream.retrofit;


import org.dvbviewer.controller.entities.FFMpegPresetList;
import org.dvbviewer.controller.io.data.FFMPEGPrefsHandler;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class FFMpegPresetResponseBodyConverter implements Converter<ResponseBody, FFMpegPresetList> {

    private final FFMPEGPrefsHandler prefsHandler = new FFMPEGPrefsHandler();

    @Override public FFMpegPresetList convert(ResponseBody value) {
        try {
            return prefsHandler.parse(value.string());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            value.close();
        }
    }
}