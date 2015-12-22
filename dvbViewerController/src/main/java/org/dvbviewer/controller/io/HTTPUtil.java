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

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.InputStream;

/**
 * Here is
 * <p/>
 * Created by RayBa
 */
public class HTTPUtil {

    private static OkHttpClient httpClient;


    private static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
            httpClient.setSslSocketFactory(SSLUtil.getSSLServerSocketFactory());
            httpClient.setHostnameVerifier(new SSLUtil.VerifyAllHostnameVerifiyer());
        }
        return httpClient;
    }

    public static String getString(String url, String username, String password) throws Exception {
        String result;
        Response response = getResponse(url, username, password);
        checkResponse(url, response);
        result = response.body().string();
        return result;
    }

    public static InputStream getInputStream(String url, String username, String password) throws Exception {
        Response response = getResponse(url, username, password);
        return response.body().byteStream();
    }

    public static byte[] getByteArrray(String url, String username, String password) throws Exception {
        Response response = getResponse(url, username, password);
        return response.body().bytes();
    }

    public static void executeGet(String url, String username, String password) throws Exception {
        getResponse(url, username, password);
    }

    private static Response getResponse(String url, String username, String password) throws Exception {
        final String credential = Credentials.basic(username, password);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", credential)
                .header("Connection", "close")
                .build();
        Response result = getHttpClient().newCall(request).execute();
        checkResponse(url, result);
        return result;
    }

    private static void checkResponse(String url, Response response) throws AuthenticationException, DefaultHttpException {
        if (!response.isSuccessful()) {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationException();
                default:
                    throw new DefaultHttpException(url);
            }
        }
    }

}
