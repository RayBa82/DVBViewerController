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
package org.dvbviewer.controller.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class URLUtil.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class URLUtil {

	private static final String	LOGTAG			= "URLUtil";

	// to refer to bar.png under your package's asset/foo/ directory, use
	// "file:///android_asset/foo/bar.png".
	static final String			ASSET_BASE		= "file:///android_asset/";
	// to refer to bar.png under your package's res/drawable/ directory, use
	// "file:///android_res/drawable/bar.png". Use "drawable" to refer to
	// "drawable-hdpi" directory as well.
	static final String			RESOURCE_BASE	= "file:///android_res/";
	static final String			FILE_BASE		= "file://";
	static final String			PROXY_BASE		= "file:///cookieless_proxy/";
	static final String			CONTENT_BASE	= "content:";

	/**
	 * Sets the recording services address.
	 *
	 * @param url the url
	 * @param port the port
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static void setRecordingServicesAddress(String url, String port){
		try {
			String prefUrl = guessUrl(url);
			URL baseUrl = new URL(prefUrl);
			ServerConsts.REC_SERVICE_PROTOCOL = baseUrl.getProtocol();
			ServerConsts.REC_SERVICE_HOST = baseUrl.getHost();
			ServerConsts.REC_SERVICE_PORT = port;
			String path = baseUrl.getPath();
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			StringBuffer buf = new StringBuffer(ServerConsts.REC_SERVICE_PROTOCOL+"://"+ServerConsts.REC_SERVICE_HOST);
			if (!TextUtils.isEmpty(ServerConsts.REC_SERVICE_PORT)) {
				buf.append(":"+ServerConsts.REC_SERVICE_PORT);
			}
			buf.append(path);
			ServerConsts.REC_SERVICE_URL = buf.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public static String buildProtectedRSUrl(String url){
		StringBuffer result = new StringBuffer();
		try {
			String prefUrl = guessUrl(url);
			URL baseUrl = new URL(prefUrl);
			String path = baseUrl.getPath();
			result.append(baseUrl.getProtocol()).append("://");
			if((!TextUtils.isEmpty(ServerConsts.REC_SERVICE_USER_NAME)) && (!TextUtils.isEmpty(ServerConsts.REC_SERVICE_PASSWORD))) {
				result.append(ServerConsts.REC_SERVICE_USER_NAME).append(":").append(ServerConsts.REC_SERVICE_PASSWORD).append("@");
			}
			result.append(baseUrl.getHost());
			int port = baseUrl.getPort();
			if (port > 0){
				result.append(":").append(baseUrl.getPort());
			}
			result.append(path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * Sets the recording services address.
	 *
	 * @param url the new recording services address
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static void setRecordingServicesAddress(String url) {
		try {
			String prefUrl = android.webkit.URLUtil.guessUrl(url);
			URL baseUrl = new URL(prefUrl);
			ServerConsts.REC_SERVICE_PROTOCOL = baseUrl.getProtocol();
			ServerConsts.REC_SERVICE_HOST = baseUrl.getHost();
			ServerConsts.REC_SERVICE_URL = ServerConsts.REC_SERVICE_PROTOCOL + "://" + ServerConsts.REC_SERVICE_HOST + ":" + ServerConsts.REC_SERVICE_PORT;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the recording services address.
	 *
	 * @param scheme the scheme
	 * @param url the url
	 * @param port the port
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static void setRecordingServicesAddress(String scheme, String url, String port) {
		setRecordingServicesAddress(scheme + "://" + url, port);
	}

	/**
	 * Cleans up (if possible) user-entered web addresses
	 */
	public static String guessUrl(String inUrl) {

		String retVal = inUrl;
		WebAddress webAddress;

		if (inUrl.length() == 0)
			return inUrl;
		if (inUrl.startsWith("about:"))
			return inUrl;
		// Do not try to interpret data scheme URLs
		if (inUrl.startsWith("data:"))
			return inUrl;
		// Do not try to interpret file scheme URLs
		if (inUrl.startsWith("file:"))
			return inUrl;
		// Do not try to interpret javascript scheme URLs
		if (inUrl.startsWith("javascript:"))
			return inUrl;

		// bug 762454: strip period off end of url
		if (inUrl.endsWith(".") == true) {
			inUrl = inUrl.substring(0, inUrl.length() - 1);
		}

		try {
			webAddress = new WebAddress(inUrl);
		} catch (Exception e) {
			e.printStackTrace();
			Log.v(LOGTAG, "smartUrlFilter: failed to parse url = " + inUrl);
			return retVal;
		}

		// Check host
		if (webAddress.getHost().indexOf('.') == -1) {
			// no dot: user probably entered a bare domain. try .com
			webAddress.setHost(webAddress.getHost());
		}
		return webAddress.toString();
	}

	public static String composeSearchUrl(String inQuery, String template, String queryPlaceHolder) {
		int placeHolderIndex = template.indexOf(queryPlaceHolder);
		if (placeHolderIndex < 0) {
			return null;
		}

		String query;
		StringBuilder buffer = new StringBuilder();
		buffer.append(template.substring(0, placeHolderIndex));

		try {
			query = java.net.URLEncoder.encode(inQuery, "utf-8");
			buffer.append(query);
		} catch (UnsupportedEncodingException ex) {
			return null;
		}

		buffer.append(template.substring(placeHolderIndex + queryPlaceHolder.length()));

		return buffer.toString();
	}

	public static byte[] decode(byte[] url) throws IllegalArgumentException {
		if (url.length == 0) {
			return new byte[0];
		}

		// Create a new byte array with the same length to ensure capacity
		byte[] tempData = new byte[url.length];

		int tempCount = 0;
		for (int i = 0; i < url.length; i++) {
			byte b = url[i];
			if (b == '%') {
				if (url.length - i > 2) {
					b = (byte) (parseHex(url[i + 1]) * 16 + parseHex(url[i + 2]));
					i += 2;
				} else {
					throw new IllegalArgumentException("Invalid format");
				}
			}
			tempData[tempCount++] = b;
		}
		byte[] retData = new byte[tempCount];
		System.arraycopy(tempData, 0, retData, 0, tempCount);
		return retData;
	}

	/**
	 * @return True iff the url is correctly URL encoded
	 */
	static boolean verifyURLEncoding(String url) {
		int count = url.length();
		if (count == 0) {
			return false;
		}

		int index = url.indexOf('%');
		while (index >= 0 && index < count) {
			if (index < count - 2) {
				try {
					parseHex((byte) url.charAt(++index));
					parseHex((byte) url.charAt(++index));
				} catch (IllegalArgumentException e) {
					return false;
				}
			} else {
				return false;
			}
			index = url.indexOf('%', index + 1);
		}
		return true;
	}

	private static int parseHex(byte b) {
		if (b >= '0' && b <= '9')
			return (b - '0');
		if (b >= 'A' && b <= 'F')
			return (b - 'A' + 10);
		if (b >= 'a' && b <= 'f')
			return (b - 'a' + 10);

		throw new IllegalArgumentException("Invalid hex char '" + b + "'");
	}

	/**
	 * @return True iff the url is an asset file.
	 */
	public static boolean isAssetUrl(String url) {
		return (null != url) && url.startsWith(ASSET_BASE);
	}

	/**
	 * @return True iff the url is a resource file.
	 * @hide
	 */
	public static boolean isResourceUrl(String url) {
		return (null != url) && url.startsWith(RESOURCE_BASE);
	}

	/**
	 * @return True iff the url is a proxy url to allow cookieless network
	 * requests from a file url.
	 * @deprecated Cookieless proxy is no longer supported.
	 */
	@Deprecated
	public static boolean isCookielessProxyUrl(String url) {
		return (null != url) && url.startsWith(PROXY_BASE);
	}

	/**
	 * @return True iff the url is a local file.
	 */
	public static boolean isFileUrl(String url) {
		return (null != url) && (url.startsWith(FILE_BASE) && !url.startsWith(ASSET_BASE) && !url.startsWith(PROXY_BASE));
	}

	/**
	 * @return True iff the url is an about: url.
	 */
	public static boolean isAboutUrl(String url) {
		return (null != url) && url.startsWith("about:");
	}

	/**
	 * @return True iff the url is a data: url.
	 */
	public static boolean isDataUrl(String url) {
		return (null != url) && url.startsWith("data:");
	}

	/**
	 * @return True iff the url is a javascript: url.
	 */
	public static boolean isJavaScriptUrl(String url) {
		return (null != url) && url.startsWith("javascript:");
	}

	/**
	 * @return True iff the url is an http: url.
	 */
	public static boolean isHttpUrl(String url) {
		return (null != url) && (url.length() > 6) && url.substring(0, 7).equalsIgnoreCase("http://");
	}

	/**
	 * @return True iff the url is an https: url.
	 */
	public static boolean isHttpsUrl(String url) {
		return (null != url) && (url.length() > 7) && url.substring(0, 8).equalsIgnoreCase("https://");
	}

	/**
	 * @return True iff the url is a network url.
	 */
	public static boolean isNetworkUrl(String url) {
		if (url == null || url.length() == 0) {
			return false;
		}
		return isHttpUrl(url) || isHttpsUrl(url);
	}

	/**
	 * @return True iff the url is a content: url.
	 */
	public static boolean isContentUrl(String url) {
		return (null != url) && url.startsWith(CONTENT_BASE);
	}

	/**
	 * @return True iff the url is valid.
	 */
	public static boolean isValidUrl(String url) {
		if (url == null || url.length() == 0) {
			return false;
		}

		return (isAssetUrl(url) || isResourceUrl(url) || isFileUrl(url) || isAboutUrl(url) || isHttpUrl(url) || isHttpsUrl(url) || isJavaScriptUrl(url) || isContentUrl(url));
	}

	/**
	 * Strips the url of the anchor.
	 */
	public static String stripAnchor(String url) {
		int anchorIndex = url.indexOf('#');
		if (anchorIndex != -1) {
			return url.substring(0, anchorIndex);
		}
		return url;
	}

	/**
	 * Guesses canonical filename that a download would have, using
	 * the URL and contentDisposition. File extension, if not defined,
	 * is added based on the mimetype
	 * @param url Url to the content
	 * @param contentDisposition Content-Disposition HTTP header or null
	 * @param mimeType Mime-type of the content or null
	 * 
	 * @return suggested filename
	 */
	public static final String guessFileName(String url, String contentDisposition, String mimeType) {
		String filename = null;
		String extension = null;

		// If we couldn't do anything with the hint, move toward the content
		// disposition
		if (contentDisposition != null) {
			filename = parseContentDisposition(contentDisposition);
			if (filename != null) {
				int index = filename.lastIndexOf('/') + 1;
				if (index > 0) {
					filename = filename.substring(index);
				}
			}
		}

		// If all the other http-related approaches failed, use the plain uri
		if (filename == null) {
			String decodedUrl = Uri.decode(url);
			if (decodedUrl != null) {
				int queryIndex = decodedUrl.indexOf('?');
				// If there is a query string strip it, same as desktop browsers
				if (queryIndex > 0) {
					decodedUrl = decodedUrl.substring(0, queryIndex);
				}
				if (!decodedUrl.endsWith("/")) {
					int index = decodedUrl.lastIndexOf('/') + 1;
					if (index > 0) {
						filename = decodedUrl.substring(index);
					}
				}
			}
		}

		// Finally, if couldn't get filename from URI, get a generic filename
		if (filename == null) {
			filename = "downloadfile";
		}

		// Split filename between base and extension
		// Add an extension if filename does not have one
		int dotIndex = filename.indexOf('.');
		if (dotIndex < 0) {
			if (mimeType != null) {
				extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
				if (extension != null) {
					extension = "." + extension;
				}
			}
			if (extension == null) {
				if (mimeType != null && mimeType.toLowerCase().startsWith("text/")) {
					if (mimeType.equalsIgnoreCase("text/html")) {
						extension = ".html";
					} else {
						extension = ".txt";
					}
				} else {
					extension = ".bin";
				}
			}
		} else {
			if (mimeType != null) {
				// Compare the last segment of the extension against the mime
				// type.
				// If there's a mismatch, discard the entire extension.
				int lastDotIndex = filename.lastIndexOf('.');
				String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(filename.substring(lastDotIndex + 1));
				if (typeFromExt != null && !typeFromExt.equalsIgnoreCase(mimeType)) {
					extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
					if (extension != null) {
						extension = "." + extension;
					}
				}
			}
			if (extension == null) {
				extension = filename.substring(dotIndex);
			}
			filename = filename.substring(0, dotIndex);
		}

		return filename + extension;
	}

	/** Regex used to parse content-disposition headers */
	private static final Pattern	CONTENT_DISPOSITION_PATTERN	= Pattern.compile("attachment;\\s*filename\\s*=\\s*(\"?)([^\"]*)\\1\\s*$", Pattern.CASE_INSENSITIVE);

	/*
	 * Parse the Content-Disposition HTTP Header. The format of the header is
	 * defined here: http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html This
	 * header provides a filename for content that is going to be downloaded to
	 * the file system. We only support the attachment type. Note that RFC 2616
	 * specifies the filename value must be double-quoted. Unfortunately some
	 * servers do not quote the value so to maintain consistent behaviour with
	 * other browsers, we allow unquoted values too.
	 */
	static String parseContentDisposition(String contentDisposition) {
		try {
			Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
			if (m.find()) {
				return m.group(2);
			}
		} catch (IllegalStateException ex) {
			// This function is defined as returning null when it can't parse
			// the header
		}
		return null;
	}

	static class WebAddress {

		private String				mScheme;
		private String				mHost;
		private int					mPort;
		private String				mPath;
		private String				mAuthInfo;

		public static final String	GOOD_IRI_CHAR			= "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";

		static final int			MATCH_GROUP_SCHEME		= 1;
		static final int			MATCH_GROUP_AUTHORITY	= 2;
		static final int			MATCH_GROUP_HOST		= 3;
		static final int			MATCH_GROUP_PORT		= 4;
		static final int			MATCH_GROUP_PATH		= 5;

		static Pattern				sAddressPattern			= Pattern.compile(
															/* scheme */"(?:(http|https|file)\\:\\/\\/)?" +
															/* authority */"(?:([-A-Za-z0-9$_.+!*'(),;?&=]+(?:\\:[-A-Za-z0-9$_.+!*'(),;?&=]+)?)@)?" +
															/* host */"([" + GOOD_IRI_CHAR + "%_-][" + GOOD_IRI_CHAR + "%_\\.-]*|\\[[0-9a-fA-F:\\.]+\\])?" +
															/* port */"(?:\\:([0-9]*))?" +
															/* path */"(\\/?[^#]*)?" +
															/* anchor */".*", Pattern.CASE_INSENSITIVE);

		/** parses given uriString. */
		public WebAddress(String address) throws Exception {
			if (address == null) {
				throw new NullPointerException();
			}

			// android.util.Log.d(LOGTAG, "WebAddress: " + address);

			mScheme = "";
			mHost = "";
			mPort = -1;
			mPath = "/";
			mAuthInfo = "";

			Matcher m = sAddressPattern.matcher(address);
			String t;
			if (m.matches()) {
				t = m.group(MATCH_GROUP_SCHEME);
				if (t != null)
					mScheme = t.toLowerCase(Locale.getDefault());
				t = m.group(MATCH_GROUP_AUTHORITY);
				if (t != null)
					mAuthInfo = t;
				t = m.group(MATCH_GROUP_HOST);
				if (t != null)
					mHost = t;
				t = m.group(MATCH_GROUP_PORT);
				if (t != null && t.length() > 0) {
					// The ':' character is not returned by the regex.
					try {
						mPort = Integer.parseInt(t);
					} catch (NumberFormatException ex) {
						throw new Exception("Bad port");
					}
				}
				t = m.group(MATCH_GROUP_PATH);
				if (t != null && t.length() > 0) {
					/*
					 * handle busted myspace frontpage redirect with missing
					 * initial "/"
					 */
					if (t.charAt(0) == '/') {
						mPath = t;
					} else {
						mPath = "/" + t;
					}
				}

			} else {
				// nothing found... outa here
				throw new Exception("Bad address");
			}

			/*
			 * Get port from scheme or scheme from port, if necessary and
			 * possible
			 */
			if (mPort == 443 && mScheme.equals("")) {
				mScheme = "https";
			} else if (mPort == -1) {
				if (mScheme.equals("https"))
					mPort = 443;
				else
					mPort = 80; // default
			}
			if (mScheme.equals(""))
				mScheme = "http";
		}

		@Override
		public String toString() {
			String port = "";
			if ((mPort != 443 && mScheme.equals("https")) || (mPort != 80 && mScheme.equals("http"))) {
				port = ":" + Integer.toString(mPort);
			}
			String authInfo = "";
			if (mAuthInfo.length() > 0) {
				authInfo = mAuthInfo + "@";
			}

			return mScheme + "://" + authInfo + mHost + port + mPath;
		}

		public void setScheme(String scheme) {
			mScheme = scheme;
		}

		public String getScheme() {
			return mScheme;
		}

		public void setHost(String host) {
			mHost = host;
		}

		public String getHost() {
			return mHost;
		}

		public void setPort(int port) {
			mPort = port;
		}

		public int getPort() {
			return mPort;
		}

		public void setPath(String path) {
			mPath = path;
		}

		public String getPath() {
			return mPath;
		}

		public void setAuthInfo(String authInfo) {
			mAuthInfo = authInfo;
		}

		public String getAuthInfo() {
			return mAuthInfo;
		}

	}
}