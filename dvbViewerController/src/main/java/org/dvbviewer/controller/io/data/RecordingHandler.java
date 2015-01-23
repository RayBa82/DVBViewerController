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
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.dvbviewer.controller.entities.Recording;
import org.dvbviewer.controller.utils.DateUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class RecordingHandler.
 *
 * @author RayBa
 * @date 05.07.2012
 */
public class RecordingHandler extends DefaultHandler {

	List<Recording>	recordingList		= null;
	Recording		currentRecording	= null;

	/**
	 * Parses the.
	 *
	 * @param xml the xml
	 * @return the list©
	 * @author RayBa
	 * @throws SAXException 
	 * @date 05.07.2012
	 */
	public List<Recording> parse(String xml) throws SAXException {
		RootElement root = new RootElement("recordings");
		Element recordingElement = root.getChild("recording");
		Element chanElement = recordingElement.getChild("channel");
		Element titleElement = recordingElement.getChild("title");
		Element infoElement = recordingElement.getChild("info");
		Element descElement = recordingElement.getChild("desc");

		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				recordingList = new ArrayList<Recording>();
			}
		});

		recordingElement.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				currentRecording = new Recording();
				currentRecording.setId(Long.valueOf(attributes.getValue("id")));
				Date start = DateUtils.stringToDate(attributes.getValue("start"), DateUtils.DATEFORMAT_RS_EPG);
				Date duration = DateUtils.stringToDate(attributes.getValue("duration"), DateUtils.TIMEFORMAT_RS_RECORDING);
				currentRecording.setStart(start);
				currentRecording.setEnd(DateUtils.addTime(start, duration));
			}
		});

		recordingElement.setEndElementListener(new EndElementListener() {

			@Override
			public void end() {
				recordingList.add(currentRecording);
			}
		});

		chanElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				currentRecording.setChannel(body);
			}

		});

		titleElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				currentRecording.setTitle(body);
			}

		});

		infoElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				currentRecording.setSubTitle(body);
			}

		});

		descElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				currentRecording.setDescription(body);
			}
		});

		Xml.parse(xml, root.getContentHandler());
		return recordingList;
	}

}
