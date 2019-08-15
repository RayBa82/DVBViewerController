package org.dvbviewer.controller.data.channel.retrofit;


import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.io.data.ChannelHandler;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class ChannelRootPresetResponseBodyConverter implements Converter<ResponseBody, List<ChannelRoot>> {

    private final ChannelHandler prefsHandler = new ChannelHandler();

    @Override public List<ChannelRoot> convert(ResponseBody value) {
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