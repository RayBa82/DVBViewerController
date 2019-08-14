package org.dvbviewer.controller.data.status.retrofit;


import androidx.annotation.NonNull;

import org.dvbviewer.controller.entities.Status;
import org.dvbviewer.controller.io.data.Status2Handler;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class StatusPresetResponseBodyConverter implements Converter<ResponseBody, Status> {

    private final Status2Handler handler = new Status2Handler();

    @Override public Status convert(@NonNull ResponseBody value) {
        try {
            return handler.parse(value.byteStream());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            value.close();
        }
    }

}