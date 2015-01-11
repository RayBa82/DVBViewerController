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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.dvbviewer.controller.utils.Base64;
import org.dvbviewer.controller.utils.ServerConsts;

import android.util.Log;
import android.webkit.URLUtil;
import ch.boye.httpclientandroidlib.Consts;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpException;
import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpRequest;
import ch.boye.httpclientandroidlib.HttpRequestInterceptor;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.HttpVersion;
import ch.boye.httpclientandroidlib.ParseException;
import ch.boye.httpclientandroidlib.StatusLine;
import ch.boye.httpclientandroidlib.auth.AuthScope;
import ch.boye.httpclientandroidlib.auth.AuthState;
import ch.boye.httpclientandroidlib.auth.AuthenticationException;
import ch.boye.httpclientandroidlib.auth.Credentials;
import ch.boye.httpclientandroidlib.auth.UsernamePasswordCredentials;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.CredentialsProvider;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.protocol.ClientContext;
import ch.boye.httpclientandroidlib.conn.scheme.PlainSocketFactory;
import ch.boye.httpclientandroidlib.conn.scheme.Scheme;
import ch.boye.httpclientandroidlib.conn.scheme.SchemeRegistry;
import ch.boye.httpclientandroidlib.impl.auth.BasicScheme;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.impl.client.cache.CacheConfig;
import ch.boye.httpclientandroidlib.impl.conn.PoolingClientConnectionManager;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.HttpConnectionParams;
import ch.boye.httpclientandroidlib.params.HttpParams;
import ch.boye.httpclientandroidlib.params.HttpProtocolParams;
import ch.boye.httpclientandroidlib.protocol.ExecutionContext;
import ch.boye.httpclientandroidlib.protocol.HttpContext;
import ch.boye.httpclientandroidlib.util.EntityUtils;

/**
 * The Class ServerRequest.
 * 
 * @author RayBa
 * @date 06.04.2012
 */
public class ServerRequest {

	private static DefaultHttpClient	httpClient;
	private static Credentials			clientCredentials;
	private static Credentials			rsCredentials;
	private static AuthScope			clientAuthScope;
	private static AuthScope			rsAuthScope;

	/**
	 * Sends an command to the DVBViewer Client. Every ActionID will be
	 * accepted.
	 *
	 * @param command ActionID
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws URISyntaxException the URI syntax exception
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws AuthenticationException the authentication exception
	 * @author RayBa
	 * @date 06.04.2012
	 */
	public static void sendCommand(String command) throws Exception {
		URI uri = null;
		uri = new URI(ServerConsts.DVBVIEWER_URL + command);
		Log.d(ServerRequest.class.getSimpleName(), "executing DVBViewer command: " + uri);
		HttpClient client = getHttpClient();
		HttpGet request = new HttpGet(uri);
		HttpResponse response = executeGet(client, request, true);
		StatusLine status = response.getStatusLine();

		switch (status.getStatusCode()) {

		case HttpStatus.SC_OK:
			HttpEntity entity = response.getEntity();
			EntityUtils.consume(entity);
			break;

		default:
			break;
		}

	}

	/**
	 * Execute get.
	 *
	 * @param client the client
	 * @param request the request
	 * @return the http response©
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClientProtocolException the client protocol exception
	 * @throws AuthenticationException the authentication exception
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static HttpResponse executeGet(HttpClient client, HttpGet request, boolean log) throws Exception {
		if (log) {
			Log.d(ServerRequest.class.getSimpleName(), "request: " + request.getRequestLine());
		}
		HttpResponse response = client.execute(request);
		StatusLine status = response.getStatusLine();
		Log.d(ServerRequest.class.getSimpleName(), "statusCode: " + status.getStatusCode());

		switch (status.getStatusCode()) {

		case HttpStatus.SC_UNAUTHORIZED:
			throw new AuthenticationException();

		default:
			break;
		}
		return response;
	}

	/**
	 * Gets the http client.
	 * 
	 * @return the http client
	 * @author RayBa
	 * @date 06.04.2012
	 */
	private static HttpClient getHttpClient() {
		if (httpClient == null) {
			try {
				HttpParams httpParams = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is
				// established.
				int timeoutConnection = 10000;
				HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
				// Set the default socket timeout (SO_TIMEOUT)
				// in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = 10000;
				HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);

				HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(httpParams, Consts.UTF_8.toString());

				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
				registry.register(SSLUtil.getHttpsScheme());

				HttpProtocolParams.setUseExpectContinue(httpParams, true);
				PoolingClientConnectionManager connManager = new PoolingClientConnectionManager(registry, 10l, TimeUnit.SECONDS);
				connManager.setDefaultMaxPerRoute(20);
				connManager.setMaxTotal(40);
				AuthRequestInterceptor preemptiveAuth = new AuthRequestInterceptor();
				httpClient = new DefaultHttpClient(connManager, httpParams);
				httpClient.addRequestInterceptor(preemptiveAuth, 0);
				httpClient.addRequestInterceptor(ZipUtil.getGzipRequestInterceptor());
				httpClient.addResponseInterceptor(ZipUtil.getGZipResponseInterceptor());
				CacheConfig cacheConfig = new CacheConfig();
				cacheConfig.setMaxCacheEntries(1000);
				cacheConfig.setMaxObjectSize(8192);

				if (getClientAuthScope() != null) {
					httpClient.getCredentialsProvider().setCredentials(getClientAuthScope(), getClientCredentials());
				}
				if (getRsAuthScope() != null) {
					httpClient.getCredentialsProvider().setCredentials(getRsAuthScope(), getRsCredentials());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return httpClient;
	}

	/**
	 * Resets the HTTP client. This is necessary on preference changes.
	 * 
	 * @author RayBa
	 * @date 13.04.2012
	 */
	public static void resetHttpCLient() {
		httpClient = null;
		rsAuthScope = null;
		clientAuthScope = null;
		rsCredentials = null;
		clientCredentials = null;
	}

	/**
	 * The Class AuthRequestInterceptor.
	 * 
	 * @author RayBa
	 * @date 06.04.2012
	 */
	private static class AuthRequestInterceptor implements HttpRequestInterceptor {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.http.HttpRequestInterceptor#process(org.apache.http.
		 * HttpRequest, org.apache.http.protocol.HttpContext)
		 */
		public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
			AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
			CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
			HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

			if (authState.getAuthScheme() == null) {
				AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
				Credentials creds = credsProvider.getCredentials(authScope);
				if (creds != null) {
					authState.update(new BasicScheme(), creds);
				}
			}
		}

	}

	/**
	 * Gets the rS bytes.
	 *
	 * @param request the request
	 * @return the rS bytes
	 * @throws AuthenticationException the authentication exception
	 * @throws URISyntaxException the URI syntax exception
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @author RayBa
	 * @date 13.04.2012
	 */
	public static byte[] getRSBytes(String request) throws Exception {
		byte[] result = null;
		result = EntityUtils.toByteArray(getRSEntity(request));
		return result;
	}

	/**
	 * Gets the rS string.
	 *
	 * @param request the request
	 * @return the rS string
	 * @throws AuthenticationException the authentication exception
	 * @throws ParseException the parse exception
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the URI syntax exception
	 * @author RayBa
	 * @date 13.04.2012
	 */
	public static String getRSString(String request) throws Exception {
		String result = null;
		result = EntityUtils.toString(getRSEntity(request));
		return result;
	}

	public static String getString(String request) throws Exception {
		String result = null;
		result = EntityUtils.toString(getEntity(request));
		return result;
	}

	/**
	 * Gets the rS entity.
	 *
	 * @param request the request
	 * @return the rS entity
	 * @throws IllegalStateException the illegal state exception
	 * @throws URISyntaxException the URI syntax exception
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws AuthenticationException the authentication exception
	 * @author RayBa
	 * @date 13.04.2012
	 */
	private static HttpEntity getRSEntity(String request) throws Exception {
		HttpEntity result = null;
		HttpClient client = getHttpClient();
		URI uri = null;
		uri = new URI(ServerConsts.REC_SERVICE_URL + request);
		HttpGet getMethod = new HttpGet(uri);
		HttpResponse res = executeGet(client, getMethod, true);

		StatusLine status = res.getStatusLine();
		switch (status.getStatusCode()) {

		case HttpStatus.SC_OK:
			result = res.getEntity();
			break;

		default:
			break;
		}
		return result;
	}
	
	public static InputStream getInputStream(String request) throws IOException, URISyntaxException {
//		HttpEntity result = null;
//		HttpClient client = getHttpClient();
//		URI uri = null;
//		uri = new URI(request);
//		HttpGet getMethod = new HttpGet(uri);
//		HttpResponse res;
//		try {
//			res = executeGet(client, getMethod, true);
//			StatusLine status = res.getStatusLine();
//			switch (status.getStatusCode()) {
//			
//			case HttpStatus.SC_OK:
//				result = res.getEntity();
//				break;
//				
//			default:
//				break;
//			}
//			if (result == null) {
//				throw new IOException("No entity result");
//			}
//		} catch (Exception e) {
//			EntityUtils.consumeQuietly(result);
//			if (e instanceof IOException) {
//				IOException ie = (IOException) e;
//				throw ie;
//			}else if(e instanceof URISyntaxException){
//				URISyntaxException ue = (URISyntaxException) e;
//				throw ue;
//			}else {
//				throw new IOException(e.getMessage());
//			}
//		}
//		
//		return result.getContent();
		
		
		InputStream result = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(request);
			if (url.getProtocol().toLowerCase().equals("https")) {
				HttpsURLConnection
				.setDefaultSSLSocketFactory(SSLUtil.getSSLContext().getSocketFactory());
				HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
				https.setHostnameVerifier(SSLUtil.DO_NOT_VERIFY);
				connection = https;
			} else {
				connection = (HttpURLConnection) url.openConnection();
			}
			String encoded = Base64.encodeToString((ServerConsts.REC_SERVICE_USER_NAME+":"+ServerConsts.REC_SERVICE_PASSWORD).getBytes(), Base64.DEFAULT);
			connection.setRequestProperty("Authorization", "Basic "+encoded);
			result = connection.getInputStream();
		} catch (Exception e) {
			if (e instanceof IOException) {
				IOException ie = (IOException) e;
				throw ie;
			}else if(e instanceof URISyntaxException){
				URISyntaxException ue = (URISyntaxException) e;
				throw ue;
			}else {
				throw new IOException(e.getMessage());
			}
		}
		return result;
	}

	/**
	 * Gets the rS entity.
	 *
	 * @param request the request
	 * @return the rS entity
	 * @throws IllegalStateException the illegal state exception
	 * @throws URISyntaxException the URI syntax exception
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws AuthenticationException the authentication exception
	 * @author RayBa
	 * @date 13.04.2012
	 */
	private static HttpEntity getEntity(String url) throws Exception {
		HttpEntity result = null;
		HttpClient client = getHttpClient();
		URI uri = null;
		uri = new URI(url);
		HttpGet getMethod = new HttpGet(uri);
		HttpResponse res = executeGet(client, getMethod, false);

		StatusLine status = res.getStatusLine();
		switch (status.getStatusCode()) {

		case HttpStatus.SC_OK:
			result = res.getEntity();
			break;

		default:
			break;
		}
		return result;
	}

	/**
	 * Executes a Recording Service Get request, with no return value.
	 * 
	 * For example used to send Timer to the Recording Service.
	 *
	 * @param request the request
	 * @throws URISyntaxException the URI syntax exception
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws AuthenticationException the authentication exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @author RayBa
	 * @date 13.04.2012
	 */
	public static void executeRSGet(String request) throws Exception {
		HttpClient client = getHttpClient();
		URI uri = null;
		Log.d(ServerRequest.class.getSimpleName(), "request: " + request);
		uri = new URI(ServerConsts.REC_SERVICE_URL + request);
		HttpGet getMethod = new HttpGet(uri);
		HttpResponse res = executeGet(client, getMethod, true);
		EntityUtils.consume(res.getEntity());
	}

	/**
	 * Gets the client credentials.
	 * 
	 * @return the client credentials
	 * @author RayBa
	 * @date 13.04.2012
	 */
	private static Credentials getClientCredentials() {
		if (clientCredentials == null) {
			clientCredentials = new UsernamePasswordCredentials(ServerConsts.DVBVIEWER_USER_NAME, ServerConsts.DVBVIEWER_PASSWORD);
		}
		return clientCredentials;
	}

	/**
	 * Gets the Recording service credentials.
	 * 
	 * @return the rs credentials
	 * @author RayBa
	 * @date 13.04.2012
	 */
	private static Credentials getRsCredentials() {
		if (rsCredentials == null) {
			rsCredentials = new UsernamePasswordCredentials(ServerConsts.REC_SERVICE_USER_NAME, ServerConsts.REC_SERVICE_PASSWORD);
		}
		return rsCredentials;
	}

	/**
	 * Gets the client auth scope.
	 * 
	 * @return the client auth scope
	 * @author RayBa
	 * @date 13.04.2012
	 */
	private static AuthScope getClientAuthScope() {
		if (clientAuthScope == null) {
			if (URLUtil.isValidUrl(ServerConsts.DVBVIEWER_URL)) {
				URI uri;
				try {
					uri = new URI(ServerConsts.DVBVIEWER_URL);
					clientAuthScope = new AuthScope(uri.getHost(), uri.getPort());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		return clientAuthScope;
	}

	/**
	 * Gets the Recording service auth scope.
	 * 
	 * @return the rs auth scope
	 * @author RayBa
	 * @date 13.04.2012
	 */
	private static AuthScope getRsAuthScope() {
		if (rsAuthScope == null) {
			if (URLUtil.isValidUrl(ServerConsts.REC_SERVICE_URL)) {
				URI uri;
				try {
					uri = new URI(ServerConsts.REC_SERVICE_URL);
					rsAuthScope = new AuthScope(uri.getHost(), uri.getPort());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		return rsAuthScope;
	}

	/**
	 * The Class RecordingServiceGet.
	 *
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public static class RecordingServiceGet implements Runnable {
		String	request;

		/**
		 * Instantiates a new recording service get.
		 *
		 * @param request the request
		 * @author RayBa
		 * @date 05.07.2012
		 */
		public RecordingServiceGet(String request) {
			this.request = request;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				ServerRequest.executeRSGet(request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * The Class DVBViewerCommand.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class DVBViewerCommand implements Runnable {
		String	request;

		public DVBViewerCommand(String request) {
			this.request = request;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				ServerRequest.sendCommand(request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
