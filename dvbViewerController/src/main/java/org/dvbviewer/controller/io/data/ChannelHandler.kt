/*
 * Copyright � 2013 dvbviewer-controller Project
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
import org.dvbviewer.controller.entities.Channel
import org.dvbviewer.controller.entities.ChannelGroup
import org.dvbviewer.controller.entities.ChannelRoot
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * The Class ChannelHandler.
 *
 * @author RayBa
 */
class ChannelHandler : DefaultHandler() {

    private lateinit var rootElements: LinkedList<ChannelRoot>
    private lateinit var currentRoot: ChannelRoot
    private lateinit var currentGroup: ChannelGroup
    private lateinit var currentChannel: Channel
    private lateinit var favRootName: String
    private var favPosition: Int = 1

    @Throws(SAXException::class, IOException::class)
    fun parse(inputStream: InputStream): MutableList<ChannelRoot> {
        Xml.parse(inputStream, Xml.Encoding.UTF_8, getContentHandler())
        return rootElements
    }

    private fun getContentHandler(): ContentHandler {
        val channels = RootElement("channels")
        val rootElement = channels.getChild("root")
        val groupElement = rootElement.getChild("group")
        val channelElement = groupElement.getChild("channel")
        val subChanElement = channelElement.getChild("subchannel")
        val logoElement = channelElement.getChild("logo")

        channels.setStartElementListener { rootElements = LinkedList() }

        rootElement.setStartElementListener { attributes ->
            currentRoot = ChannelRoot()
            currentRoot.name = attributes.getValue("name")
            rootElements.add(currentRoot)
        }

        groupElement.setStartElementListener { attributes ->
            currentGroup = ChannelGroup()
            currentGroup.name = attributes.getValue("name")
            currentRoot.groups.add(currentGroup)
        }

        channelElement.setStartElementListener { attributes ->
            currentChannel = Channel()
            currentChannel.channelID = NumberUtils.toLong(attributes.getValue("ID"))
            currentChannel.position = favPosition
            currentChannel.name = attributes.getValue("name")
            currentChannel.epgID = NumberUtils.toLong(attributes.getValue("EPGID"))
            currentGroup.channels.add(currentChannel)
            favPosition++
        }

        logoElement.setEndTextElementListener { body -> currentChannel.logoUrl = body }

        subChanElement.setStartElementListener { attributes ->
            val c = Channel()
            c.channelID = NumberUtils.toLong(attributes.getValue("ID"))
            c.position = currentChannel.position
            c.name = attributes.getValue("name")
            c.epgID = currentChannel.epgID
            c.logoUrl = currentChannel.logoUrl
            c.setFlag(Channel.FLAG_ADDITIONAL_AUDIO)
            currentGroup.channels.add(c)
        }
        return channels.contentHandler
    }

}
