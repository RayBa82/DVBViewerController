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

/**
 * Util Class for !unsecure! SSL connections.
 *
 * @author RayBa
 * @date 02.03.2014
 */
class SSLUtil {

	/**
	 * Gets the SSLCcontext. Here is special handling reqired,
	 * since not all devices support the prefered TLS protocol.
	 * It returns null if neither a SSLContext using the TLS protocol nor
	 * one using the SSL protocol could be created.
	 *
	 * @return the SSL context
	 */
	private static SSLContext getSSLContext() {
		SSLContext sslContext = getTlsProtocolContext();
		if (sslContext == null) {
			sslContext = getSslProtocolContext();
		}
		return sslContext;
	}

	public static SSLSocketFactory getSSLServerSocketFactory() {
		return getSSLContext().getSocketFactory();
	}

	/**
	 * Gets an SSLContext using the TLS protocol.
	 *
	 * @return the tls protocol context
	 */
	private static SSLContext getTlsProtocolContext() {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[]{new TrustAllTrustManager()}, new SecureRandom());
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}
		return sslContext;
	}

	/**
	 * Gets an SSLContext using the SSL protocol
	 *
	 * @return the ssl protocol context
	 */
	private static SSLContext getSslProtocolContext() {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new TrustManager[]{new TrustAllTrustManager()}, new SecureRandom());
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}
		return sslContext;
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

		/* (non-Javadoc)
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	}

	public static class VerifyAllHostnameVerifiyer implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

}
