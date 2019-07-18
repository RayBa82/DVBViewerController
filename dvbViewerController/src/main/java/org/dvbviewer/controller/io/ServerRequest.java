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

import android.content.Context;
import android.util.Log;

import org.dvbviewer.controller.utils.ServerConsts;

import java.io.InputStream;

import okhttp3.Callback;

/**
 * The Class ServerRequest.
 * 
 */
public class ServerRequest {




	/**
	 * Gets a string result from the backend
	 *
	 * @param request the request
	 * @return the string response string
	 * @throws Exception the exception
	 */
	public static String getRSString(String request) throws Exception {
		return HTTPUtil.getString(request, ServerConsts.REC_SERVICE_USER_NAME, ServerConsts.REC_SERVICE_PASSWORD);
	}




	/**
	 * Executes a server GET request, with no return value.
	 * 
	 * @param request the request
	 * @throws Exception the exception
	 */
	public static void executeRSGet(String request) throws Exception {
		HTTPUtil.executeGet(request, ServerConsts.REC_SERVICE_USER_NAME, ServerConsts.REC_SERVICE_PASSWORD);
	}

	/**
	 * Executes an async server GET request
	 *
	 * For example used to send Timer to the Recording Service.
	 *
	 * @param request the Backend request
	 * @param callback the Callback to execute
	 * @throws Exception the exception
	 */
	public static void executeAsync(String request, Callback callback) {
		HTTPUtil.getAsyncResponse(request, ServerConsts.REC_SERVICE_USER_NAME, ServerConsts.REC_SERVICE_PASSWORD, callback);
	}

	public static InputStream getInputStream(String request) throws Exception {
		return HTTPUtil.getInputStream(request, ServerConsts.REC_SERVICE_USER_NAME, ServerConsts.REC_SERVICE_PASSWORD);
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
		Context	context;
		String	request;
        public DVBViewerCommand(Context context, String request) {
			this.context = context;
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
				Log.e("DVBViewerCommand", "error executing request " + request, e);
			}
		}

	}

}
