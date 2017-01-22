/*
 * Copyright © 2013 dvbviewer-controller Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dvbviewer.controller.io;

import android.util.Log;

import org.dvbviewer.controller.utils.ServerConsts;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Here is
 * <p/>
 * Created by RayBa
 */
public class HTTPUtil {

    private static OkHttpClient httpClient;


    private static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            final X509TrustManager trustManager = SSLUtil.getTrustAllTrustManager();
            httpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(SSLUtil.getSSLServerSocketFactory(trustManager), trustManager)
                    .hostnameVerifier(new SSLUtil.VerifyAllHostnameVerifiyer())
                    .connectTimeout(5000, TimeUnit.MILLISECONDS)
                    .readTimeout(5000, TimeUnit.MILLISECONDS)
                    .build();
        }
        return httpClient;
    }

    public static String getString(String url, String username, String password) throws Exception {
        return getResponse(url, username, password).body().string();
    }

    public static UrlBuilder getUrlBuilder(String url) throws UrlBuilderException {
        final  HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null){
            throw new UrlBuilderException(url);
        }
        return new UrlBuilder(httpUrl.toString());
    }

    public static InputStream getInputStream(String url, String username, String password) throws Exception {
        Response response = getResponse(url, username, password);
        return response.body().byteStream();
    }

    public static void executeGet(String url, String username, String password) throws Exception {
        getResponse(url, username, password);
    }

    private static Response getResponse(String url, String username, String password) throws Exception {
        final String credential = Credentials.basic(username, password);
        Log.d("DVBViewerServerRequest", url);
        Request request = getBuilder(url, credential).build();
        Response result;
        try {
            result = getHttpClient().newCall(request).execute();
            checkResponse(result);
        }catch (Exception e){
            throw new DefaultHttpException(ServerConsts.REC_SERVICE_URL, e);
        }
        return result;
    }

    private static Request.Builder getBuilder(String url, String credentials) throws UrlBuilderException {
        UrlBuilder urlBuilder = new UrlBuilder(url);
        return new Request.Builder()
                .url(urlBuilder.toString())
                .header("Authorization", credentials)
                .header("Connection", "close");
    }

    private static void checkResponse(Response response) throws AuthenticationException, UnsuccessfullHttpException {
        if (response != null && !response.isSuccessful()) {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationException();
                default:
                    throw new UnsuccessfullHttpException(response.code());
            }
        }
    }

    public static class UrlBuilder{

        private HttpUrl.Builder builder;

        public UrlBuilder(String url) throws UrlBuilderException {
            final HttpUrl httpUrl = HttpUrl.parse(url);
            if (httpUrl == null) {
                throw new UrlBuilderException(url);
            }
            builder = httpUrl.newBuilder();
        }

        public UrlBuilder addQueryParameter(String name, String value){
            builder.addQueryParameter(name, value);
            return this;
        }

        public HttpUrl build(){
            return builder.build();
        }


        @Override
        public String toString() {
            return build().toString();
        }
    }

}
