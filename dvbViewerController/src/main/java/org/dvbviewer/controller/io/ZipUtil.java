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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HeaderElement;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpException;
import ch.boye.httpclientandroidlib.HttpRequest;
import ch.boye.httpclientandroidlib.HttpRequestInterceptor;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpResponseInterceptor;
import ch.boye.httpclientandroidlib.entity.HttpEntityWrapper;
import ch.boye.httpclientandroidlib.protocol.HttpContext;

/**
 * Util Class to support GZip compression.
 *
 * @author RayBa
 * @date 02.03.2014
 */
public class ZipUtil {

	/**
	 * Gets the gzip request interceptor.
	 *
	 * @return the gzip request interceptor
	 * @author RayBa
	 * @date 02.03.2014
	 */
	public static GZipRequestInterceptor getGzipRequestInterceptor() {
		return new GZipRequestInterceptor();
	}

	/**
	 * Gets the g zip response interceptor.
	 *
	 * @return the g zip response interceptor
	 * @author RayBa
	 * @date 02.03.2014
	 */
	public static GZipResponseInterceptor getGZipResponseInterceptor() {
		return new GZipResponseInterceptor();
	}

	/**
	 * The Class GZipResponseInterceptor.
	 *
	 * @author RayBa
	 * @date 02.03.2014
	 */
	public static class GZipResponseInterceptor implements HttpResponseInterceptor {

		/* (non-Javadoc)
		 * @see ch.boye.httpclientandroidlib.HttpResponseInterceptor#process(ch.boye.httpclientandroidlib.HttpResponse, ch.boye.httpclientandroidlib.protocol.HttpContext)
		 */
		@Override
		public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
			HttpEntity entity = response.getEntity();
			Header ceheader = entity.getContentEncoding();
			if (ceheader != null) {
				HeaderElement[] codecs = ceheader.getElements();
				for (int i = 0; i < codecs.length; i++) {
					if (codecs[i].getName().equalsIgnoreCase("gzip")) {
						response.setEntity(new GzipDecompressingEntity(response.getEntity()));
						return;
					}
				}
			}
		}

	}

	/**
	 * The Class GZipRequestInterceptor.
	 *
	 * @author RayBa
	 * @date 02.03.2014
	 */
	public static class GZipRequestInterceptor implements HttpRequestInterceptor {

		/* (non-Javadoc)
		 * @see ch.boye.httpclientandroidlib.HttpRequestInterceptor#process(ch.boye.httpclientandroidlib.HttpRequest, ch.boye.httpclientandroidlib.protocol.HttpContext)
		 */
		public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
			if (!request.containsHeader("Accept-Encoding")) {
				request.addHeader("Accept-Encoding", "gzip");
			}
		}

	}

	/**
	 * The Class GzipDecompressingEntity.
	 *
	 * @author RayBa
	 * @date 02.03.2014
	 */
	public static class GzipDecompressingEntity extends HttpEntityWrapper {

		/**
		 * Instantiates a new gzip decompressing entity.
		 *
		 * @param entity the entity
		 * @author RayBa
		 * @date 02.03.2014
		 */
		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		/* (non-Javadoc)
		 * @see ch.boye.httpclientandroidlib.entity.HttpEntityWrapper#getContent()
		 */
		@Override
		public InputStream getContent() throws IOException, IllegalStateException {

			// the wrapped entity's getContent() decides about repeatability
			InputStream wrappedin = wrappedEntity.getContent();

			return new GZIPInputStream(wrappedin);
		}

		/* (non-Javadoc)
		 * @see ch.boye.httpclientandroidlib.entity.HttpEntityWrapper#getContentLength()
		 */
		@Override
		public long getContentLength() {
			// length of ungzipped content is not known
			return -1;
		}

	}

}
