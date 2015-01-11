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
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ch.boye.httpclientandroidlib.conn.scheme.PlainSocketFactory;
import ch.boye.httpclientandroidlib.conn.scheme.Scheme;
import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;

/**
 * Util Class for !unsecure! SSL connections.
 *
 * @author RayBa
 * @date 02.03.2014
 */
public class SSLUtil {

	/**
	 * Gets the SSLCcontext. Here is special handling reqired, 
	 * since not all devices support the prefered TLS protocol.
	 * It returns null if neither a SSLContext using the TLS protocol nor
	 * one using the SSL protocol could be created.
	 *
	 * @return the sSL context
	 * @author RayBa
	 * @date 02.03.2014
	 */
	public static SSLContext getSSLContext() {
		SSLContext sslContext = getTlsProtocolContext();
		if (sslContext == null) {
			sslContext = getSslProtocolContext();
		}
		return sslContext;
	}

	/**
	 * Gets an SSLContext using the TLS protocol.
	 *
	 * @return the tls protocol context
	 * @author RayBa
	 * @date 02.03.2014
	 */
	private static SSLContext getTlsProtocolContext() {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[] { new TrustAllTrustManager() }, new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return sslContext;
	}

	/**
	 * Gets an SSLContext using the SSL protocol
	 *
	 * @return the ssl protocol context
	 * @author RayBa
	 * @date 02.03.2014
	 */
	private static SSLContext getSslProtocolContext() {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new TrustManager[] { new TrustAllTrustManager() }, new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
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
	 * @author RayBa
	 * @date 02.03.2014
	 */
	public static Scheme getHttpsScheme(){
		Scheme httpsScheme;
		SSLContext sslContext = getSSLContext();
		if (sslContext != null) {
			SSLSocketFactory sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			httpsScheme = new Scheme("https", 443, sf);
		}else {
			httpsScheme = new Scheme("https", 443, PlainSocketFactory.getSocketFactory());
		}
		return httpsScheme;
	}

	/**
	 * A TrustManager which trusts every server and accepts all certificates.
	 *
	 * @author RayBa
	 * @date 02.03.2014
	 */
	public static class TrustAllTrustManager implements X509TrustManager {

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

	final static public HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	
}
