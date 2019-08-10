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
import org.dvbviewer.controller.entities.Status
import org.dvbviewer.controller.entities.Status.Folder
import org.dvbviewer.controller.entities.Status.StatusItem
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
class Status2Handler : DefaultHandler(), StatusHandler {

    private var status: Status? = null
    private var currentFolder: Folder? = null

    /**
     * Parses the.
     *
     * @param stream the is
     * @return the status©
     * @throws SAXException
     * @author RayBa
     * @date 01.07.2012
     */
    @Throws(SAXException::class, IOException::class)
    override fun parse(stream: InputStream): Status? {
        val root = RootElement("status")
        val recordcount = root.getChild("reccount")
        val streamclientcount = root.getChild("streamclientcount")
        val rtspclientcount = root.getChild("rtspclientcount")
        val unicastclientcount = root.getChild("unicastclientcount")
        val lastuiaccess = root.getChild("lastuiaccess")
        val nexttimer = root.getChild("nexttimer")
        val nextrec = root.getChild("nextrec")
        val standbyblock = root.getChild("standbyblock")
        val tunercount = root.getChild("tunercount")
        val streamtunercount = root.getChild("streamtunercount")
        val rectunercount = root.getChild("rectunercount")
        val recfiles = root.getChild("recfiles")
        val epgudate = root.getChild("epgudate")
        val recfolders = root.getChild("recfolders")
        val folder = recfolders.getChild("folder")

        root.setStartElementListener { status = Status() }

        recordcount.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_current_recordings
            item.value = body
            status!!.items.add(item)
        }

        streamclientcount.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_current_clients
            item.value = body
            status!!.items.add(item)
        }

        epgudate.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_epg_update_running
            item.value = body
            status!!.items.add(item)
        }

        rtspclientcount.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_current_rtsp_clients
            item.value = body
            status!!.items.add(item)
        }

        unicastclientcount.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_current_unicast_clients
            item.value = body
            status!!.items.add(item)
        }
        nexttimer.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_next_timer
            item.value = body
            status!!.items.add(item)
        }
        nextrec.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_next_Rec
            item.value = body
            status!!.items.add(item)
        }
        lastuiaccess.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_last_ui_access
            item.value = body
            status!!.items.add(item)
        }
        standbyblock.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_standby_blocked
            item.value = body
            status!!.items.add(item)
        }
        tunercount.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_tunercount
            item.value = body
            status!!.items.add(item)
        }
        streamtunercount.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_stream_tunercount
            item.value = body
            status!!.items.add(item)
        }
        rectunercount.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_record_tunercount
            item.value = body
            status!!.items.add(item)
        }
        recfiles.setEndTextElementListener { body ->
            val item = StatusItem()
            item.nameRessource = R.string.status_recfiles
            item.value = body
            status!!.items.add(item)
        }

        recfolders.setStartElementListener { status!!.folders = ArrayList() }
        folder.setStartElementListener { attributes ->
            currentFolder = Folder()
            currentFolder!!.size = NumberUtils.toLong(attributes.getValue("size"))
            currentFolder!!.free = NumberUtils.toLong(attributes.getValue("free"))
        }
        folder.setEndTextElementListener { body -> currentFolder!!.path = body }
        folder.setEndElementListener { status!!.folders.add(currentFolder) }
        Xml.parse(stream, Xml.Encoding.UTF_8, root.contentHandler)
        return status

    }

}
