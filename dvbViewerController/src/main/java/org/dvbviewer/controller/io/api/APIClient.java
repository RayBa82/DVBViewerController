package org.dvbviewer.controller.io.api;

import com.tickaroo.tikxml.TikXml;
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory;

import org.dvbviewer.controller.data.stream.retrofit.FFMpegConverterFactory;
import org.dvbviewer.controller.io.HTTPUtil;
import org.dvbviewer.controller.utils.ServerConsts;

import retrofit2.Retrofit;

public class APIClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        String baseUrl = ServerConsts.REC_SERVICE_URL;
        final TikXml tikXml = new TikXml.Builder()
                .exceptionOnUnreadXml(false)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(FFMpegConverterFactory.create())
                .addConverterFactory(TikXmlConverterFactory.create(tikXml))
                .client(HTTPUtil.getHttpClient())
                .build();

        return retrofit;
    }

}