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
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.dvbviewer.controller.entities.VideoFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class VideoHandler extends DefaultHandler {

	List<VideoFile> mediaFiles = null;
	VideoFile currentFile = null;

	public List<VideoFile> parse(InputStream xml) throws SAXException, IOException {
		RootElement root = new RootElement("videofiles");
		Element dir = root.getChild("file");
		Element thumb = dir.getChild("thumb");
		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				mediaFiles = new LinkedList<>();
			}
		});

		dir.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				currentFile = new VideoFile();
				mediaFiles.add(currentFile);
				final String name = attributes.getValue("name");
				final String objid = attributes.getValue("objid");
				final String title = attributes.getValue("title");
				final String dur = attributes.getValue("dur");
				final String hres = attributes.getValue("hres");
				final String vres = attributes.getValue("vres");
				currentFile.setName(name);
				currentFile.setId(Long.valueOf(objid));
				currentFile.setTitle(title);
				currentFile.setDur(Integer.valueOf(dur));
				currentFile.setHres(Integer.valueOf(hres));
				currentFile.setVres(Integer.valueOf(vres));
			}
		});

		thumb.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				currentFile.setThumb(body);
			}

		});

		Xml.parse(xml, Xml.Encoding.UTF_8, root.getContentHandler());
		return mediaFiles;
	}

}
