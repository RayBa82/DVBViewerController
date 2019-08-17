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
package org.dvbviewer.controller.data.api.io

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.dvbviewer.controller.data.api.io.okhttp3.DMSInterceptor
import java.util.concurrent.TimeUnit

/**
 * Here is
 *
 *
 * Created by RayBa
 */
object HTTPUtil {

    private var httpClient: OkHttpClient? = null

    fun getHttpClient(): OkHttpClient {
        if (httpClient == null) {
            val trustManager = SSLUtil.getTrustAllTrustManager()
            val dmsInterceptor = DMSInterceptor()
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            httpClient = OkHttpClient.Builder()
                    .sslSocketFactory(SSLUtil.getSSLServerSocketFactory(trustManager), trustManager)
                    .hostnameVerifier(SSLUtil.VerifyAllHostnameVerifiyer())
                    .connectTimeout(5000, TimeUnit.MILLISECONDS)
                    .readTimeout(5000, TimeUnit.MILLISECONDS)
                    .addInterceptor(dmsInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .build()
        }
        return httpClient!!
    }

}