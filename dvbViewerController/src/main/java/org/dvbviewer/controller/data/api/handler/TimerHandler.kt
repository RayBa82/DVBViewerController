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
package org.dvbviewer.controller.data.api.handler

import android.sax.RootElement
import android.util.Xml
import org.apache.commons.lang3.math.NumberUtils
import org.dvbviewer.controller.data.entities.Timer
import org.dvbviewer.controller.utils.DateUtils
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * The Class TimerHandler.
 *
 * @author RayBa
 * @date 05.07.2012
 */
class TimerHandler : DefaultHandler() {

    private lateinit var timerList: LinkedList<Timer>
    private var currentTimer: Timer? = null

    /**
     * Parses the.
     *
     * @param xml the xml
     * @return the list©
     * @author RayBa
     * @throws SAXException
     * @date 05.07.2012
     */
    @Throws(SAXException::class, IOException::class)
    fun parse(xml: InputStream): List<Timer> {
        val root = RootElement("Timers")
        val timerElement = root.getChild("Timer")
        val optionElement = timerElement.getChild("Options")
        val descElement = timerElement.getChild("Descr")
        val chanElement = timerElement.getChild("Channel")
        val idElement = timerElement.getChild("ID")
        val executableElement = timerElement.getChild("Executeable")
        val recordStatElement = timerElement.getChild("Recordstat")

        root.setStartElementListener {
            timerList = LinkedList()
        }

        timerElement.setStartElementListener { attributes ->
            currentTimer = Timer()
            val startDay = DateUtils.stringToDate(attributes.getValue("Date"), DateUtils.DATEFORMAT_RS_TIMER)
            val startTime = DateUtils.stringToDate(attributes.getValue("Start"), DateUtils.TIMEFORMAT_RS_TIMER)
            val duration = NumberUtils.toInt(attributes.getValue("Dur"))
            currentTimer?.start = DateUtils.addTime(startDay, startTime)
            currentTimer?.end = DateUtils.addMinutes(currentTimer!!.start, duration)
            val pre = attributes.getValue("PreEPG")
            currentTimer?.pre = NumberUtils.toInt(pre)
            val post = attributes.getValue("PostEPG")
            currentTimer?.post = NumberUtils.toInt(post)
            val timerAction = attributes.getValue("ShutDown")
            currentTimer?.timerAction = if (timerAction != null) NumberUtils.toInt(timerAction) else 0
            val disabled = NumberUtils.toLong(attributes.getValue("Enabled"))
            if (disabled == 0L) {
                currentTimer?.setFlag(Timer.FLAG_DISABLED)
            } else {
                currentTimer?.unsetFlag(Timer.FLAG_DISABLED)
            }
            currentTimer?.eventId = attributes.getValue("EPGEventID")
            currentTimer?.pdc = attributes.getValue("PDC")
        }

        timerElement.setEndElementListener { currentTimer?.let { timerList.add(it) } }

        descElement.setEndTextElementListener { body -> currentTimer?.title = body }

        optionElement.setStartElementListener { attributes ->
            val adjustPAT = attributes.getValue("AdjustPAT")
            currentTimer?.adjustPAT = if (adjustPAT != null) NumberUtils.toInt(adjustPAT) else -1
            val allAudio = attributes.getValue("AllAudio")
            currentTimer?.allAudio = if (allAudio != null) NumberUtils.toInt(allAudio) else -1
            val dvbSubs = attributes.getValue("DVBSubs")
            currentTimer?.dvbSubs = if (dvbSubs != null) NumberUtils.toInt(dvbSubs) else -1
            val teletext = attributes.getValue("Teletext")
            currentTimer?.teletext = if (teletext != null) NumberUtils.toInt(teletext) else -1
            val eitepg = attributes.getValue("EITEPG")
            currentTimer?.eitEPG = if (eitepg != null) NumberUtils.toInt(eitepg) else -1
            val monitorPDC = attributes.getValue("MonitorPDC")
            currentTimer?.monitorPDC = if (monitorPDC != null) NumberUtils.toInt(monitorPDC) else -1
            val split = attributes.getValue("RunningStatusSplit")
            currentTimer?.runningStatusSplit = if (split != null) NumberUtils.toInt(split) else -1
        }

        chanElement.setStartElementListener { attributes ->
            val id = attributes.getValue("ID")
            val channelInfos = id.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            currentTimer?.channelId = NumberUtils.toLong(channelInfos[0].trim { it <= ' ' })
            currentTimer?.channelName = channelInfos[1].trim { it <= ' ' }
        }
        idElement.setEndTextElementListener { body -> currentTimer?.id = NumberUtils.toLong(body) }

        recordStatElement.setStartElementListener { currentTimer?.setFlag(Timer.FLAG_RECORDING) }
        executableElement.setEndTextElementListener { body ->
            val executable = NumberUtils.toLong(body)
            if (executable == 0L) {
                currentTimer?.setFlag(Timer.FLAG_EXECUTABLE)
            } else {
                currentTimer?.unsetFlag(Timer.FLAG_EXECUTABLE)
            }
        }
        Xml.parse(xml, Xml.Encoding.UTF_8, root.contentHandler)
        timerList.sort()
        return timerList
    }

}
