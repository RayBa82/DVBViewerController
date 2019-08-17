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
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.entities.Status
import org.dvbviewer.controller.data.entities.Status.StatusItem
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * The Class Status1Handler.
 *
 * @author RayBa
 * @date 01.07.2012
 */
class Status1Handler : DefaultHandler(), StatusHandler {

    private val status: Status = Status()

    /**
     * Parses the.
     *
     * @param stream the is
     * @return the status©
     * @author RayBa
     * @throws SAXException
     * @date 01.07.2012
     */
    @Throws(SAXException::class, IOException::class)
    override fun parse(stream: InputStream): Status {
        val root = RootElement("status")
        val epgbefore = root.getChild("epgbefore")
        val epgafter = root.getChild("epgafter")
        val timezone = root.getChild("timezone")
        val defafterrecord = root.getChild("defafterrecord")

        root.setStartElementListener {
            status.items = ArrayList()
        }


        epgbefore.setEndTextElementListener { body ->
            status.epgBefore = NumberUtils.toInt(body)
            val item = StatusItem()
            item.nameRessource = R.string.status_epg_before
            item.value = body
            status.items.add(item)
        }

        epgafter.setEndTextElementListener { body ->
            status.epgAfter = Integer.parseInt(body)
            val item = StatusItem()
            item.nameRessource = R.string.status_epg_after
            item.value = body
            status.items.add(item)
        }
        timezone.setEndTextElementListener { body ->
            status.timeZone = NumberUtils.toInt(body)
            val item = StatusItem()
            item.nameRessource = R.string.status_timezone
            item.value = body
            status.items.add(item)
        }
        defafterrecord.setEndTextElementListener { body ->
            status.defAfterRecord = NumberUtils.toInt(body)
            val item = StatusItem()
            item.nameRessource = R.string.status_def_after_record
            item.value = body
            status.items.add(item)
        }
        Xml.parse(stream, Xml.Encoding.UTF_8, root.contentHandler)
        return status

    }

}
