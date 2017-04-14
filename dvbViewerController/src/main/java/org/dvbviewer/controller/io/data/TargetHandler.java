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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler to parse the Remote Targets from the DVBViewer Recording Service (Since 1.30.1.0)
 *
 * @author RayBa
 * @date 11.01.2015
 */
public class TargetHandler extends DefaultHandler {

	List<String>	targets	= null;

	/**
	 * Parses the xml String targets
	 *
	 * @param xml the xml
	 * @return the list©
	 * @author RayBa
	 * @throws org.xml.sax.SAXException
	 * @date 11.01.2015
	 */
	public List<String> parse(InputStream xml) throws SAXException, IOException {
		RootElement root = new RootElement("targets");
		Element targetElement = root.getChild("target");

		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
                targets = new ArrayList<String>();
			}
		});

        targetElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
                targets.add(body);
			}
		});

		Xml.parse(xml, Xml.Encoding.UTF_8, root.getContentHandler());
		return targets;
	}

}
