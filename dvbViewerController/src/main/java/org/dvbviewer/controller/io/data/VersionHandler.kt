/*
 * Copyright © 2012 dvbviewer-controller Project
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
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream

/**
 * The Class VersionHandler.
 *
 * @author RayBa
 */
class VersionHandler : DefaultHandler() {

    private var result: String = ""

    private val contentHandler: ContentHandler
        get() {
            val root = RootElement("version")
            root.setEndTextElementListener { body -> result = body }
            return root.contentHandler
        }

    @Throws(SAXException::class, IOException::class)
    fun parse(inputStream: InputStream): String {
        Xml.parse(inputStream, Xml.Encoding.UTF_8, contentHandler)
        return result
    }

}