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

import org.apache.commons.lang3.math.NumberUtils;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.Status;
import org.dvbviewer.controller.entities.Status.Folder;
import org.dvbviewer.controller.entities.Status.StatusItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * The Class StatusHandler.
 *
 * @author RayBa
 * @date 01.07.2012
 */
public class StatusHandler extends DefaultHandler {

	Status	status			= null;
	Folder	currentFolder	= null;

	/**
	 * Parses the.
	 *
	 * @param xml the xml
	 * @return the status©
	 * @author RayBa
	 * @throws SAXException 
	 * @date 01.07.2012
	 */
	public Status parse(String xml) throws SAXException {
		RootElement root = new RootElement("status");
		Element epgbefore = root.getChild("epgbefore");
		Element epgafter = root.getChild("epgafter");
		Element timezone = root.getChild("timezone");
		Element defafterrecord = root.getChild("defafterrecord");

		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				status = new Status();
				status.setItems(new ArrayList<Status.StatusItem>());
			}
		});


		epgbefore.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setEpgBefore(NumberUtils.toInt(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_epg_before);
				item.setValue(body);
				status.getItems().add(item);
			}

		});

		epgafter.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setEpgAfter(Integer.parseInt(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_epg_after);
				item.setValue(body);
				status.getItems().add(item);
			}
		});
		timezone.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setTimeZone(NumberUtils.toInt(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_timezone);
				item.setValue(body);
				status.getItems().add(item);
			}
		});
		defafterrecord.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setDefAfterRecord(NumberUtils.toInt(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_def_after_record);
				item.setValue(body);
				status.getItems().add(item);
			}
		});
		Xml.parse(xml, root.getContentHandler());
		return status;

	}

}
