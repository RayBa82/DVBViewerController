/*
 * Copyright © 2012 dvbviewer-controller Project
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

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

/**
 * The Class VersionHandler.
 *
 * @author RayBa
 * @date 01.07.2012
 */
public class VersionHandler extends DefaultHandler {

	String	result	= null;

	/**
	 * Parses the.
	 *
	 * @param xml the xml
	 * @return the version©
	 * @author RayBa
	 * @throws SAXException 
	 * @date 01.07.2012
	 */
	public String parse(String xml) throws SAXException {
		RootElement root = new RootElement("version");
		root.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				result = body;
			}
		});

		Xml.parse(xml, root.getContentHandler());
		return result;
	}

}
