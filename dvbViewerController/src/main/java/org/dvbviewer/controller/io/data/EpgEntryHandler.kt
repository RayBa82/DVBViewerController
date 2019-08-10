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
package org.dvbviewer.controller.io.data

import android.sax.RootElement
import android.util.Xml
import org.apache.commons.lang3.math.NumberUtils
import org.dvbviewer.controller.entities.EpgEntry
import org.dvbviewer.controller.utils.DateUtils
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * The Class EpgEntryHandler.
 *
 * @author RayBa
 */
class EpgEntryHandler : DefaultHandler() {

    private val epgList = ArrayList<EpgEntry>()
    private lateinit var currentEPG: EpgEntry

    private val contentHandler: ContentHandler
        get() {
            val root = RootElement("epg")
            val programmeElement = root.getChild("programme")
            val eventId = programmeElement.getChild("eventid")
            val pdc = programmeElement.getChild("pdc")
            val titles = programmeElement.getChild("titles")
            val title = titles.getChild("title")
            val descriptions = programmeElement.getChild("descriptions")
            val description = descriptions.getChild("description")
            val events = programmeElement.getChild("events")
            val event = events.getChild("event")

            programmeElement.setStartElementListener { attributes ->
                currentEPG = EpgEntry()
                currentEPG.epgID = NumberUtils.toLong(attributes.getValue("channel"))
                currentEPG.start = DateUtils.stringToDate(attributes.getValue("start"), DateUtils.DATEFORMAT_RS_EPG)
                currentEPG.end = DateUtils.stringToDate(attributes.getValue("stop"), DateUtils.DATEFORMAT_RS_EPG)
            }

            programmeElement.setEndElementListener { epgList.add(currentEPG) }

            eventId.setEndTextElementListener { body -> currentEPG.eventId = body }
            pdc.setEndTextElementListener { body -> currentEPG.pdc = body }
            title.setEndTextElementListener { body -> currentEPG.title = body }
            event.setEndTextElementListener { body -> currentEPG.subTitle = body }

            description.setEndTextElementListener { body -> currentEPG.description = body }
            return root.contentHandler
        }

    @Throws(SAXException::class, IOException::class)
    fun parse(inputStream: InputStream): List<EpgEntry> {
        Xml.parse(inputStream, Xml.Encoding.UTF_8, contentHandler)
        return epgList
    }

}
