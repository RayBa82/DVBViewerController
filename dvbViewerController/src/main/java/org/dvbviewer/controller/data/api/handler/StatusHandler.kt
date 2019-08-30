package org.dvbviewer.controller.data.api.handler

import org.dvbviewer.controller.data.entities.Status
import org.xml.sax.SAXException

import java.io.IOException
import java.io.InputStream

/**
 * Created by rayba on 24.12.16.
 */
interface StatusHandler {
    @Throws(SAXException::class, IOException::class)
    fun parse(stream: InputStream): Status
}
