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
import java.util.BitSet;
import java.util.List;

import org.dvbviewer.controller.entities.Channel.Fav;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

/**
 * Handler to parse the facourites from the DVBViewer Recording Service
 *
 * @author RayBa
 * @date 05.07.2012
 */
public class FavouriteHandler extends DefaultHandler {

	List<Fav>	favourites	= null;
	Fav			currentFav;

	/**
	 * Parses the xml String favourites.xml
	 *
	 * @param context the context
	 * @param xml the xml
	 * @return the list©
	 * @author RayBa
	 * @throws SAXException 
	 * @date 05.07.2012
	 */
	public List<Fav> parse(Context context, String xml) throws SAXException {
		RootElement root = new RootElement("settings");
		Element sectionElement = root.getChild("section");
		Element entryElement = sectionElement.getChild("entry");

		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				favourites = new ArrayList<Fav>();
			}
		});

		entryElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				String[] channelInfos = body.split("\\|");
				if (channelInfos.length > 1) {
					currentFav = new Fav();
					long id = Long.valueOf(channelInfos[0]);
					currentFav.id = id;
					currentFav.name = channelInfos[1];
					favourites.add(currentFav);
				}
			}
		});

		Xml.parse(xml, root.getContentHandler());
		for (int i = 0; i < favourites.size(); i++) {
			Fav fav = favourites.get(i);
			fav.position = i + 1;
		}
		return favourites;
	}

	/**
	 * Convert.
	 *
	 * @param value the value
	 * @return the bit set©
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public static BitSet convert(long value) {
		BitSet bits = new BitSet();
		int index = 0;
		while (value != 0L) {
			if (value % 2L != 0) {
				bits.set(index);
			}
			++index;
			value = value >>> 1;
		}
		return bits;
	}

	/**
	 * Convert.
	 *
	 * @param bits the bits
	 * @return the int©
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public static int convert(BitSet bits) {
		int value = 0;
		for (int i = 0; i < bits.length(); ++i) {
			value += bits.get(i) ? (1 << i) : 0;
		}
		return value;
	}

}
