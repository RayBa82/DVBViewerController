package org.dvbviewer.controller.data.epg.retrofit;


import org.dvbviewer.controller.entities.EpgEntry;
import org.dvbviewer.controller.io.data.EpgEntryHandler;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class EpgPresetResponseBodyConverter implements Converter<ResponseBody, List<EpgEntry>> {

    private final EpgEntryHandler prefsHandler = new EpgEntryHandler();

    @Override public List<EpgEntry> convert(ResponseBody value) {
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