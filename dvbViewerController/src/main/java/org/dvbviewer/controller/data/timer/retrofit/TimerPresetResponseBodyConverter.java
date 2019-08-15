package org.dvbviewer.controller.data.timer.retrofit;


import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.io.data.TimerHandler;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class TimerPresetResponseBodyConverter implements Converter<ResponseBody, List<Timer>> {

    private final TimerHandler prefsHandler = new TimerHandler();

    @Override public List<Timer> convert(ResponseBody value) {
        try {
            return prefsHandler.parse(value.byteStream());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            value.close();
        }
    }

}