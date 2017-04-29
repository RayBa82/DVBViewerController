package org.dvbviewer.controller.io.data;

import org.apache.commons.lang3.StringUtils;
import org.dvbviewer.controller.entities.FfMpegPrefs;
import org.dvbviewer.controller.entities.Preset;
import org.dvbviewer.controller.ui.fragments.StreamConfig;
import org.dvbviewer.controller.utils.INIParser;

import java.util.Iterator;

/**
 * Created by karlw on 18.12.2015.
 */
public class FFMPEGPrefsHandler {

    public FfMpegPrefs parse(String ffmpegprefs) throws Exception {
        FfMpegPrefs ffPrefs = new FfMpegPrefs();
        INIParser iniParser = new INIParser(ffmpegprefs);
        ffPrefs.setVersion(iniParser.getString("Version", "Version"));
        Iterator<String> sectionIterator = iniParser.getSections();
        while (sectionIterator.hasNext()) {
            String sectionName = sectionIterator.next();
            if (isPreset(iniParser, sectionName)) {
                Preset preset = new Preset();
                preset.setTitle(sectionName);
                final String mimeType = iniParser.getString(sectionName, "MimeType");
                if (StringUtils.isEmpty(mimeType)) {
                    preset.setMimeType(StreamConfig.M3U8_MIME_TYPE);
                } else {
                    preset.setMimeType(mimeType);
                }
                preset.setExtension(iniParser.getString(sectionName, "Ext"));
                ffPrefs.getPresets().add(preset);
            }
        }
        return ffPrefs;
    }

    private boolean isPreset(INIParser iniParser, String sectionName) {
        Iterator<String> keysIterator = iniParser.getKeys(sectionName);
        boolean isPreset = false;
        while(keysIterator.hasNext()){
            String keyName = keysIterator.next();
            if ("Cmd".equals(keyName)){
                isPreset = true;
            }
        }
        return isPreset;
    }
}
