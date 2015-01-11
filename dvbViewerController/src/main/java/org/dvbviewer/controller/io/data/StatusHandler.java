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

import java.util.ArrayList;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.Status;
import org.dvbviewer.controller.entities.Status.Folder;
import org.dvbviewer.controller.entities.Status.StatusItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

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
		Element recordcount = root.getChild("recordcount");
		Element clientcount = root.getChild("clientcount");
		Element epgudate = root.getChild("epgudate");
		Element epgbefore = root.getChild("epgbefore");
		Element epgafter = root.getChild("epgafter");
		Element timezone = root.getChild("timezone");
		Element defafterrecord = root.getChild("defafterrecord");
		Element recfolders = root.getChild("recfolders");
		Element folder = recfolders.getChild("folder");

		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				status = new Status();
				status.setItems(new ArrayList<Status.StatusItem>());
			}
		});

		recordcount.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setRecordCount(Integer.valueOf(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_current_recordings);
				item.setValue(body);
				status.getItems().add(item);
			}
		});

		clientcount.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setClientCount(Integer.valueOf(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_current_clients);
				item.setValue(body);
				status.getItems().add(item);
			}

		});

		epgudate.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setEpgUdate(Integer.valueOf(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_epg_update_running);
				item.setValue(body);
				status.getItems().add(item);
			}

		});

		epgbefore.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setEpgBefore(Integer.valueOf(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_epg_before);
				item.setValue(body);
				status.getItems().add(item);
			}

		});

		epgafter.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setEpgAfter(Integer.valueOf(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_epg_after);
				item.setValue(body);
				status.getItems().add(item);
			}
		});
		timezone.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setTimeZone(Integer.valueOf(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_timezone);
				item.setValue(body);
				status.getItems().add(item);
			}
		});
		defafterrecord.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				status.setDefAfterRecord(Integer.valueOf(body));
				StatusItem item = new StatusItem();
				item.setNameRessource(R.string.status_def_after_record);
				item.setValue(body);
				status.getItems().add(item);
			}
		});
		recfolders.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				status.setFolders(new ArrayList<Status.Folder>());
			}
		});
		folder.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				currentFolder = new Folder();
				currentFolder.setSize(Long.valueOf(attributes.getValue("size")));
				currentFolder.setFree(Long.valueOf(attributes.getValue("free")));
			}
		});
		folder.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				currentFolder.setPath(body);
			}
		});
		folder.setEndElementListener(new EndElementListener() {

			@Override
			public void end() {
				status.getFolders().add(currentFolder);
			}
		});
		Xml.parse(xml, root.getContentHandler());
		return status;

	}

}
