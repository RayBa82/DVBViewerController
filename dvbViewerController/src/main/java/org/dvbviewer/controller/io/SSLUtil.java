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
package org.dvbviewer.controller.io;

import android.text.TextUtils;
import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.TlsVersion;

/**
 * Util Class for !unsecure! SSL connections.
 *
 * @author RayBa
 */
class SSLUtil {

	private static final String TAG = SSLUtil.class.getSimpleName();

	/**
	 * Gets the SSLCcontext. Here is special handling reqired,
	 * since not all devices support the prefered TLS protocol.
	 * It returns null if neither a SSLContext using the TLS protocol nor
	 * one using the SSL protocol could be created.
	 *
	 * @return the SSL context
	 */
	private static SSLContext getSSLContext(X509TrustManager trustManager) {
		SSLContext sslContext = getProtocolContext(TlsVersion.TLS_1_2.javaName(), trustManager);
		if (sslContext == null) {
			sslContext = getProtocolContext(TlsVersion.TLS_1_0.javaName(), trustManager);
		}
		if (sslContext == null) {
			sslContext = getProtocolContext(TlsVersion.SSL_3_0.javaName(), trustManager);
		}
		if (sslContext == null) {
			sslContext = getDefaultContext(trustManager);
		}
		return sslContext;
	}

	public static SSLSocketFactory getSSLServerSocketFactory(X509TrustManager trustManager) {
		return getSSLContext(trustManager).getSocketFactory();
	}

	public static X509TrustManager getTrustAllTrustManager() {
		return new TrustAllTrustManager();
	}


	/**
	 * Gets an SSLContext using the TLSv1.2 protocol.
	 *
	 * @return the tls protocol context
	 */
	private static SSLContext getProtocolContext(String protocol, X509TrustManager trustManager) {
		SSLContext sslContext = null;
		try {
			sslContext = TextUtils.isEmpty(protocol) ? SSLContext.getDefault() : SSLContext.getInstance(protocol);
			initSslContext(sslContext, trustManager);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "Error creating SSL Context", e);
		}
		return sslContext;
	}


	private static SSLContext getDefaultContext(X509TrustManager trustManager) {
		return getProtocolContext(null, trustManager);
	}

	private static void initSslContext(SSLContext sslContext, X509TrustManager trustManager){
		try {
			sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
		} catch (KeyManagementException e) {
			Log.e(TAG, "Error initializing SSL Context", e);
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
	private static class TrustAllTrustManager implements X509TrustManager {

		/* (non-Javadoc)
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
         */
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

		}

		/* (non-Javadoc)
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
         */
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

	}

	public static class VerifyAllHostnameVerifiyer implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

}
