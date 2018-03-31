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

import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;

/**
 * The Class URLUtil.
 *
 * @author RayBa
 */
public class URLUtil {

    private static final String LOGTAG = "URLUtil";


    /**
     * Sets the recording services address.
     *
     * @param url  the url
     * @param port the port
     */
    public static void setRecordingServicesAddress(String url, String port) {
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
            StringBuilder buf = new StringBuilder(ServerConsts.REC_SERVICE_PROTOCOL)
                    .append("://")
                    .append(ServerConsts.REC_SERVICE_HOST);
            if (!TextUtils.isEmpty(ServerConsts.REC_SERVICE_PORT)) {
                buf.append(":").append(ServerConsts.REC_SERVICE_PORT);
            }
            buf.append(path);
            ServerConsts.REC_SERVICE_URL = buf.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static HttpUrl.Builder buildProtectedRSUrl(String url) {
        StringBuilder result = new StringBuilder();
        try {
            String prefUrl = guessUrl(url);
            URL baseUrl = new URL(prefUrl);
            String path = baseUrl.getPath();
            result.append(baseUrl.getProtocol()).append("://");
            if ((!TextUtils.isEmpty(ServerConsts.REC_SERVICE_USER_NAME)) && (!TextUtils.isEmpty(ServerConsts.REC_SERVICE_PASSWORD))) {
                result.append(ServerConsts.REC_SERVICE_USER_NAME).append(":").append(ServerConsts.REC_SERVICE_PASSWORD).append("@");
            }
            result.append(baseUrl.getHost());
            int port = baseUrl.getPort();
            if (port > 0) {
                result.append(":").append(baseUrl.getPort());
            }
            result.append(path);
            return HttpUrl.parse(result.toString()).newBuilder();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Cleans up (if possible) user-entered web addresses
     */
    private static String guessUrl(String inUrl) throws NullPointerException {

        String retVal = StringUtils.EMPTY;
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
        if (inUrl.endsWith(".")) {
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

    private static class WebAddress {

        private String mScheme;
        private String mHost;
        private int mPort;
        private String mPath;
        private String mAuthInfo;

        public static final String GOOD_IRI_CHAR = "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";

        static final int MATCH_GROUP_SCHEME = 1;
        static final int MATCH_GROUP_AUTHORITY = 2;
        static final int MATCH_GROUP_HOST = 3;
        static final int MATCH_GROUP_PORT = 4;
        static final int MATCH_GROUP_PATH = 5;

        static final Pattern sAddressPattern = Pattern.compile(
                                                            /* scheme */"(?:(http|https|file)\\:\\/\\/)?" +
															/* authority */"(?:([-A-Za-z0-9$_.+!*'(),;?&=]+(?:\\:[-A-Za-z0-9$_.+!*'(),;?&=]+)?)@)?" +
															/* host */"([" + GOOD_IRI_CHAR + "%_-][" + GOOD_IRI_CHAR + "%_\\.-]*|\\[[0-9a-fA-F:\\.]+\\])?" +
															/* port */"(?:\\:([0-9]*))?" +
															/* path */"(\\/?[^#]*)?" +
															/* anchor */".*", Pattern.CASE_INSENSITIVE);

        /**
         * parses given uriString.
         */
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

        public void setHost(String host) {
            mHost = host;
        }

        public String getHost() {
            return mHost;
        }

    }
}