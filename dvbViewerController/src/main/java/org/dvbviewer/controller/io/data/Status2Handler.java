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
public class Status2Handler extends DefaultHandler {

    Status status = null;
    Folder currentFolder = null;

    /**
     * Parses the.
     *
     * @param xml the xml
     * @return the status©
     * @throws SAXException
     * @author RayBa
     * @date 01.07.2012
     */
    public Status parse(String xml) throws SAXException {
        RootElement root = new RootElement("status");
        Element recordcount = root.getChild("reccount");
        Element streamclientcount = root.getChild("streamclientcount");
        Element rtspclientcount = root.getChild("rtspclientcount");
        Element unicastclientcount = root.getChild("unicastclientcount");
        Element lastuiaccess = root.getChild("lastuiaccess");
        Element nexttimer = root.getChild("nexttimer");
        Element nextrec = root.getChild("nextrec");
        Element standbyblock = root.getChild("standbyblock");
        Element tunercount = root.getChild("tunercount");
        Element streamtunercount = root.getChild("streamtunercount");
        Element rectunercount = root.getChild("rectunercount");
        Element recfiles = root.getChild("recfiles");
        Element epgudate = root.getChild("epgudate");
        Element recfolders = root.getChild("recfolders");
        Element folder = recfolders.getChild("folder");

        root.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                status = new Status();
            }
        });

        recordcount.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_current_recordings);
                item.setValue(body);
                status.getItems().add(item);
            }
        });

        streamclientcount.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_current_clients);
                item.setValue(body);
                status.getItems().add(item);
            }

        });

        epgudate.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_epg_update_running);
                item.setValue(body);
                status.getItems().add(item);
            }

        });

        rtspclientcount.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_current_rtsp_clients);
                item.setValue(body);
                status.getItems().add(item);
            }

        });

        unicastclientcount.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_current_unicast_clients);
                item.setValue(body);
                status.getItems().add(item);
            }
        });
        nexttimer.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_next_timer);
                item.setValue(body);
                status.getItems().add(item);
            }
        });
        nextrec.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_next_Rec);
                item.setValue(body);
                status.getItems().add(item);
            }
        });
        lastuiaccess.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_last_ui_access);
                item.setValue(body);
                status.getItems().add(item);
            }
        });
        standbyblock.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_standby_blocked);
                item.setValue(body);
                status.getItems().add(item);
            }
        });
        tunercount.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_tunercount);
                item.setValue(body);
                status.getItems().add(item);
            }
        });
        streamtunercount.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_stream_tunercount);
                item.setValue(body);
                status.getItems().add(item);
            }
        });
        rectunercount.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_record_tunercount);
                item.setValue(body);
                status.getItems().add(item);
            }
        });
        recfiles.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                StatusItem item = new StatusItem();
                item.setNameRessource(R.string.status_recfiles);
                item.setValue(body);
                status.getItems().add(item);
            }
        });

        recfolders.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                status.setFolders(new ArrayList<Folder>());
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
