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
		Element optionElement = timerElement.getChild("Options");
		Element descElement = timerElement.getChild("Descr");
		Element chanElement = timerElement.getChild("Channel");
		Element idElement = timerElement.getChild("ID");
		Element executableElement = timerElement.getChild("Executeable");
		Element recordStatElement = timerElement.getChild("Recordstat");

		root.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				timerList = new ArrayList<>();
			}
		});

		timerElement.setStartElementListener(new StartElementListener() {
			public void start(Attributes attributes) {
				currentTimer = new Timer();
				final Date startDay = DateUtils.stringToDate(attributes.getValue("Date"), DateUtils.DATEFORMAT_RS_TIMER);
				final Date startTime = DateUtils.stringToDate(attributes.getValue("Start"), DateUtils.TIMEFORMAT_RS_TIMER);
				int duration = NumberUtils.toInt(attributes.getValue("Dur"));
				currentTimer.setStart(DateUtils.addTime(startDay, startTime));
				currentTimer.setEnd(DateUtils.addMinutes(currentTimer.getStart(), duration));
				final String pre = attributes.getValue("PreEPG");
				currentTimer.setPre(NumberUtils.toInt(pre));
				final String post = attributes.getValue("PostEPG");
				currentTimer.setPost(NumberUtils.toInt(post));
				final String timerAction = attributes.getValue("ShutDown");
				currentTimer.setTimerAction(timerAction != null ? NumberUtils.toInt(timerAction) : 0);
				long disabled = NumberUtils.toLong(attributes.getValue("Enabled"));
				if (disabled == 0l) {
					currentTimer.setFlag(Timer.FLAG_DISABLED);
				} else {
					currentTimer.unsetFlag(Timer.FLAG_DISABLED);
				}
				currentTimer.setEventId(attributes.getValue("EPGEventID"));
				currentTimer.setPdc(attributes.getValue("PDC"));
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

		optionElement.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes attributes) {
				final String adjustPAT = attributes.getValue("AdjustPAT");
				currentTimer.setAdjustPAT(adjustPAT != null ? NumberUtils.toInt(adjustPAT) : -1);
				final String allAudio = attributes.getValue("AllAudio");
				currentTimer.setAllAudio(allAudio != null ? NumberUtils.toInt(allAudio) : -1);
				final String dvbSubs = attributes.getValue("DVBSubs");
				currentTimer.setDvbSubs(dvbSubs != null ? NumberUtils.toInt(dvbSubs) : -1);
				final String teletext = attributes.getValue("Teletext");
				currentTimer.setTeletext(teletext != null ? NumberUtils.toInt(teletext) : -1);
				final String eitepg = attributes.getValue("EITEPG");
				currentTimer.setEitEPG(eitepg != null ? NumberUtils.toInt(eitepg) : -1);
				final String monitorPDC = attributes.getValue("MonitorPDC");
				currentTimer.setMonitorPDC(monitorPDC != null ? NumberUtils.toInt(monitorPDC) : -1);
				final String split = attributes.getValue("RunningStatusSplit");
				currentTimer.setRunningStatusSplit(split != null ? NumberUtils.toInt(split) : -1);
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
