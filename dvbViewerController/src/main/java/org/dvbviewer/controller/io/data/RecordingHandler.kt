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
import org.dvbviewer.controller.entities.Recording
import org.dvbviewer.controller.utils.DateUtils
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * The Class RecordingHandler.
 *
 * @author RayBa
 */
class RecordingHandler : DefaultHandler() {

    private val recordingList = ArrayList<Recording>()
    private var currentRecording: Recording? = null

    private val contentHandler: ContentHandler
        get() {
            val root = RootElement("recordings")
            val recordingElement = root.getChild("recording")
            val chanElement = recordingElement.getChild("channel")
            val titleElement = recordingElement.getChild("title")
            val infoElement = recordingElement.getChild("info")
            val descElement = recordingElement.getChild("desc")
            val imageElement = recordingElement.getChild("image")

            recordingElement.setStartElementListener { attributes ->
                currentRecording = Recording()
                currentRecording?.id = NumberUtils.toLong(attributes.getValue("id"))
                val start = DateUtils.stringToDate(attributes.getValue("start"), DateUtils.DATEFORMAT_RS_EPG)
                val duration = DateUtils.stringToDate(attributes.getValue("duration"), DateUtils.TIMEFORMAT_RS_RECORDING)
                currentRecording?.start = start
                currentRecording?.end = DateUtils.addTime(start, duration)
            }

            recordingElement.setEndElementListener { currentRecording?.let { recordingList.add(it) } }

            chanElement.setEndTextElementListener { body -> currentRecording?.channel = body }

            titleElement.setEndTextElementListener { body -> currentRecording?.title = body }

            infoElement.setEndTextElementListener { body -> currentRecording?.subTitle = body }

            descElement.setEndTextElementListener { body -> currentRecording?.description = body }
            imageElement.setEndTextElementListener { body -> currentRecording?.thumbNail = body }
            return root.contentHandler
        }

    @Throws(SAXException::class, IOException::class)
    fun parse(inputStream: InputStream): MutableList<Recording> {
        Xml.parse(inputStream, Xml.Encoding.UTF_8, contentHandler)
        recordingList.sort()
        return recordingList
    }

}
