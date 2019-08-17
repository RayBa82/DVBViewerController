package org.dvbviewer.controller.data.api.handler


import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.data.entities.FFMpegPresetList
import org.dvbviewer.controller.data.entities.Preset
import org.dvbviewer.controller.utils.INIParser
import org.dvbviewer.controller.utils.StreamUtils

class FFMPEGPrefsHandler {

    @Throws(Exception::class)
    fun parse(ffmpegprefs: String?): FFMpegPresetList {
        val ffPrefs = FFMpegPresetList()
        if (StringUtils.isBlank(ffmpegprefs)) {
            return ffPrefs
        }
        val iniParser = INIParser(ffmpegprefs)
        ffPrefs.version = iniParser.getString("Version", "Version")
        val sectionIterator = iniParser.sections
        while (sectionIterator.hasNext()) {
            val sectionName = sectionIterator.next()
            if (isPreset(iniParser, sectionName)) {
                val preset = Preset()
                preset.title = sectionName
                val mimeType = iniParser.getString(sectionName, "MimeType")
                if (StringUtils.isEmpty(mimeType)) {
                    preset.mimeType = StreamUtils.M3U8_MIME_TYPE
                } else {
                    preset.mimeType = mimeType
                }
                preset.extension = iniParser.getString(sectionName, "Ext")
                ffPrefs.presets.add(preset)
            }
        }
        return ffPrefs
    }

    private fun isPreset(iniParser: INIParser, sectionName: String): Boolean {
        val keysIterator = iniParser.getKeys(sectionName)
        var isPreset = false
        while (keysIterator!!.hasNext()) {
            val keyName = keysIterator.next()
            if ("Cmd" == keyName) {
                isPreset = true
            }
        }
        return isPreset
    }
}
