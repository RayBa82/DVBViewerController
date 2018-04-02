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
package org.dvbviewer.controller.io.data;

import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.dvbviewer.controller.entities.MediaFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MediaHandler extends DefaultHandler {

	List<MediaFile> mediaFiles = null;

	/**
	 * Parses the.
	 *
	 * @param xml the xml
	 * @return the list©
	 * @author RayBa
	 * @throws SAXException 
	 * @date 05.07.2012
	 */
	public List<MediaFile> parse(InputStream xml) throws SAXException, IOException {
		RootElement root = new RootElement("videodirs");
		Element dir = root.getChild("dir");
		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				mediaFiles = new LinkedList<>();
			}
		});

		dir.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				final String path = attributes.getValue("path");
				final String[] pathArray = path.replace("\\", "/").split("/");
				List<MediaFile> currentFiles = mediaFiles;
				MediaFile file = null;
				for (String folder : Arrays.copyOfRange(pathArray, 1, pathArray.length)){
					file = getMediaFileFromList(folder, currentFiles);
				}
				final String dirId = attributes.getValue("dirid");
				file.setDirId(Long.valueOf(dirId));
			}
		});

		Xml.parse(xml, Xml.Encoding.UTF_8, root.getContentHandler());
		return mediaFiles;
	}

	private MediaFile getMediaFileFromList(final String name, final List<MediaFile> medias){
		for(MediaFile media : medias) {
			if (media.getName().equals(name)) {
				return media;
			}
		}
		final MediaFile file = new MediaFile();
		file.setName(name);
		medias.add(file);
		return file;
	}

}
