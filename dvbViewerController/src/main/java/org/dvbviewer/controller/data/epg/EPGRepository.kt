package org.dvbviewer.controller.data.epg

import org.dvbviewer.controller.entities.EpgEntry
import org.dvbviewer.controller.io.api.DMSInterface
import org.dvbviewer.controller.utils.DateUtils
import java.util.*

class EPGRepository(private val dmsInterface: DMSInterface) {

    fun getChannelEPG(channelId: Long, start: Date, end: Date): List<EpgEntry>? {
        val startFloat = DateUtils.getFloatDate(start)
        val endFloat = DateUtils.getFloatDate(end)
        return dmsInterface.getChannelProgramm(channelId.toString(), startFloat, endFloat).execute().body()
    }




}
