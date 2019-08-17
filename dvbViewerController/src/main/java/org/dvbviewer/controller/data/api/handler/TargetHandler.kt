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
import org.dvbviewer.controller.data.entities.DVBTarget
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Handler to parse the Remote Targets from the DVBViewer Recording Service (Since 1.30.1.0)
 *
 * @author RayBa
 * @date 11.01.2015
 */
class TargetHandler : DefaultHandler() {

    private val targets = ArrayList<DVBTarget>()

    /**
     * Parses the xml String targets
     *
     * @param xml the xml
     * @return the list©
     * @author RayBa
     * @date 11.01.2015
     */
    @Throws(SAXException::class, IOException::class)
    fun parse(xml: InputStream): MutableList<DVBTarget> {
        val root = RootElement("targets")
        val targetElement = root.getChild("target")

        targetElement.setEndTextElementListener { body ->
            val target = DVBTarget()
            target.name = body
            targets.add(target)
        }

        Xml.parse(xml, Xml.Encoding.UTF_8, root.contentHandler)
        return targets
    }

}
