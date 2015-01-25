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
package org.dvbviewer.controller.io.imageloader;

import android.content.Context;

import com.nostra13.universalimageloader.core.assist.FlushedInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.dvbviewer.controller.io.ServerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Imagedownloader which supports https connections and
 * downloading of protected Images through Basic Authentication.
 *
 * @author RayBa
 * @date 02.03.2014
 */
public class AuthImageDownloader extends BaseImageDownloader {
	
	/** The Constant TAG. */
	public static final String	TAG				= AuthImageDownloader.class.getName();

	/**
	 * Instantiates a new auth image downloader.
	 *
	 * @param context the context
	 * @author RayBa
	 * @date 02.03.2014
	 */
	public AuthImageDownloader(Context context) {
		super(context);
	}

	/* (non-Javadoc)
	 * @see com.nostra13.universalimageloader.core.download.BaseImageDownloader#getStreamFromNetwork(java.lang.String, java.lang.Object)
	 */
	@Override
	protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
		FlushedInputStream result = null;
		try {
			result = new FlushedInputStream(ServerRequest.getInputStream(imageUri));
		} catch (URISyntaxException e) {
			throw new IOException("Invalid Uri "+imageUri);
		}
		return result;
	}


}