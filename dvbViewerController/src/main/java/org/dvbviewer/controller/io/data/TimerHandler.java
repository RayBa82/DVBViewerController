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

import org.apache.commons.lang3.math.NumberUtils;
import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.utils.DateUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class TimerHandler.
 *
 * @author RayBa
 * @date 05.07.2012
 */
public class TimerHandler extends DefaultHandler {

	List<Timer>	timerList		= null;
	Timer		currentTimer	= null;

	/**
	 * Parses the.
	 *
	 * @param xml the xml
	 * @return the list©
	 * @author RayBa
	 * @throws SAXException 
	 * @date 05.07.2012
	 */
	public List<Timer> parse(InputStream xml) throws SAXException, IOException {
		RootElement root = new RootElement("Timers");
		Element timerElement = root.getChild("Timer");
		Element descElement = timerElement.getChild("Descr");
		Element chanElement = timerElement.getChild("Channel");
		Element idElement = timerElement.getChild("ID");
		Element executableElement = timerElement.getChild("Executeable");
		Element recordStatElement = timerElement.getChild("Recordstat");

		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				timerList = new ArrayList<Timer>();
			}
		});

		timerElement.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				currentTimer = new Timer();
				Date startDay = DateUtils.stringToDate(attributes.getValue("Date"), DateUtils.DATEFORMAT_RS_TIMER);
				Date startTime = DateUtils.stringToDate(attributes.getValue("Start"), DateUtils.TIMEFORMAT_RS_TIMER);
				int duration = NumberUtils.toInt(attributes.getValue("Dur"));
				currentTimer.setStart(DateUtils.addTime(startDay, startTime));
				currentTimer.setEnd(DateUtils.addMinutes(currentTimer.getStart(), duration));
				String timerAction = attributes.getValue("ShutDown");
				currentTimer.setTimerAction(timerAction != null ? NumberUtils.toInt(timerAction) : 0);
				long disabled = NumberUtils.toLong(attributes.getValue("Enabled"));
				if (disabled == 0l) {
					currentTimer.setFlag(Timer.FLAG_DISABLED);
				} else {
					currentTimer.unsetFlag(Timer.FLAG_DISABLED);
				}
			}
		});

		timerElement.setEndElementListener(new EndElementListener() {

			@Override
			public void end() {
				timerList.add(currentTimer);
			}
		});

		descElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				currentTimer.setTitle(body);
			}
		});

		chanElement.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				String id = attributes.getValue("ID");
				String[] channelInfos = id.split("\\|");
				currentTimer.setChannelId(NumberUtils.toLong(channelInfos[0].trim()));
				currentTimer.setChannelName(channelInfos[1].trim());
			}

		});
		idElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				currentTimer.setId(NumberUtils.toLong(body));
			}
		});

		recordStatElement.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				currentTimer.setFlag(Timer.FLAG_RECORDING);
			}

		});
		executableElement.setEndTextElementListener(new EndTextElementListener() {

			@Override
			public void end(String body) {
				long executable = NumberUtils.toLong(body);
				if (executable == 0l) {
					currentTimer.setFlag(Timer.FLAG_EXECUTABLE);
				} else {
					currentTimer.unsetFlag(Timer.FLAG_EXECUTABLE);
				}
			}
		});
		Xml.parse(xml, Xml.Encoding.UTF_8, root.getContentHandler());
		return timerList;
	}

}
