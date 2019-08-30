/*
 * Copyright (C) 2012 dvbviewer-controller Project
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

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import okhttp3.TlsVersion
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * Util Class for !unsecure! SSL connections.
 *
 * @author RayBa
 */
internal object SSLUtil {

    private val TAG = SSLUtil::class.java.simpleName

    val trustAllTrustManager: X509TrustManager
        get() = TrustAllTrustManager()

    /**
     * Gets the SSLCcontext. Here is special handling reqired,
     * since not all devices support the prefered TLS protocol.
     * It returns null if neither a SSLContext using the TLS protocol nor
     * one using the SSL protocol could be created.
     *
     * @return the SSL context
     */
    private fun getSSLContext(trustManager: X509TrustManager): SSLContext? {
        var sslContext = getProtocolContext(TlsVersion.TLS_1_2.javaName(), trustManager)
        if (sslContext == null) {
            sslContext = getProtocolContext(TlsVersion.TLS_1_1.javaName(), trustManager)
        }
        if (sslContext == null) {
            sslContext = getProtocolContext(TlsVersion.TLS_1_0.javaName(), trustManager)
        }
        if (sslContext == null) {
            sslContext = getProtocolContext(TlsVersion.SSL_3_0.javaName(), trustManager)
        }
        if (sslContext == null) {
            sslContext = getDefaultContext(trustManager)
        }
        return sslContext
    }

    fun getSSLServerSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
        return getSSLContext(trustManager)!!.socketFactory
    }


    /**
     * Gets an SSLContext using the TLSv1.2 protocol.
     *
     * @return the tls protocol context
     */
    private fun getProtocolContext(protocol: String?, trustManager: X509TrustManager): SSLContext? {
        var sslContext: SSLContext? = null
        try {
            sslContext = if (TextUtils.isEmpty(protocol)) SSLContext.getDefault() else SSLContext.getInstance(protocol)
            initSslContext(sslContext!!, trustManager)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "Error creating SSL Context", e)
        }

        return sslContext
    }


    private fun getDefaultContext(trustManager: X509TrustManager): SSLContext? {
        return getProtocolContext(null, trustManager)
    }

    private fun initSslContext(sslContext: SSLContext, trustManager: X509TrustManager) {
        try {
            sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        } catch (e: KeyManagementException) {
            Log.e(TAG, "Error initializing SSL Context", e)
        }

    }

    /**
     * Gets the Scheme for https connections.
     * Either one which trusts all Certificates,
     * or one using the PlainSocketFactory.
     *
     * @return the https scheme
     */

    /**
     * A TrustManager which trusts every server and accepts all certificates.
     *
     * @author RayBa
     */
    private class TrustAllTrustManager : X509TrustManager {

        /* (non-Javadoc)
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
         */
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {

        }

        /* (non-Javadoc)
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
         */
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {

        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return emptyArray()
        }

    }

    class VerifyAllHostnameVerifiyer : HostnameVerifier {

        @SuppressLint("BadHostnameVerifier")
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }

}
