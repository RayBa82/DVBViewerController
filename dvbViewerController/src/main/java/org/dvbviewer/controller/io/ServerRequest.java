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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * The Class ServerRequest.
 * 
 * @author RayBa
 * @date 06.04.2012
 */
public class ServerRequest {




	/**
	 * Gets the rS string.
	 *
	 * @param request the request
	 * @return the rS string
	 * @throws AuthenticationException the authentication exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the URI syntax exception
	 * @author RayBa
	 * @date 13.04.2012
	 */
	public static String getRSString(String request) throws Exception {
		return HTTPUtil.getString(ServerConsts.REC_SERVICE_URL + request, ServerConsts.REC_SERVICE_USER_NAME, ServerConsts.REC_SERVICE_PASSWORD);
	}




	/**
	 * Executes a Recording Service Get request, with no return value.
	 * 
	 * For example used to send Timer to the Recording Service.
	 *
	 * @param request the request
	 * @throws URISyntaxException the URI syntax exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws AuthenticationException the authentication exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @author RayBa
	 * @date 13.04.2012
	 */
	public static void executeRSGet(String request) throws Exception {
		Log.d(ServerRequest.class.getSimpleName(), "request: " + request);
		HTTPUtil.executeGet(ServerConsts.REC_SERVICE_URL + request, ServerConsts.REC_SERVICE_USER_NAME, ServerConsts.REC_SERVICE_PASSWORD);
	}

	public static InputStream getInputStream(String request) throws Exception {
		return HTTPUtil.getInputStream(request, ServerConsts.REC_SERVICE_USER_NAME, ServerConsts.REC_SERVICE_PASSWORD);
	}

	public static byte[] getRSBytes(String request) throws Exception{
		return HTTPUtil.getByteArrray(ServerConsts.REC_SERVICE_URL + request, ServerConsts.REC_SERVICE_USER_NAME, ServerConsts.REC_SERVICE_PASSWORD);
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
                ServerRequest.executeRSGet(request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
