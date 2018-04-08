package org.dvbviewer.controller.io.api;

import org.dvbviewer.controller.io.HTTPUtil;
import org.dvbviewer.controller.utils.ServerConsts;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class APIClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        String baseUrl = ServerConsts.REC_SERVICE_URL;
        if(!baseUrl.endsWith("/")) {
            baseUrl = baseUrl.concat("/");
        }
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .client(HTTPUtil.getHttpClient())
                .build();

        return retrofit;
    }

}