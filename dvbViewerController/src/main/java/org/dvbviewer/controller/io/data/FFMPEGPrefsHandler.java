package org.dvbviewer.controller.io.data;

import org.dvbviewer.controller.entities.FfMpegPrefs;
import org.dvbviewer.controller.entities.Preset;
import org.dvbviewer.controller.utils.INIParser;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by karlw on 18.12.2015.
 */
public class FFMPEGPrefsHandler {

    public FfMpegPrefs parse(String ffmpegprefs) {
        FfMpegPrefs ffPrefs = new FfMpegPrefs();
        try {
            INIParser iniParser = new INIParser(ffmpegprefs);
            ffPrefs.setVersion(iniParser.getString("Version", "Version"));
            Iterator<String> sectionIterator = iniParser.getSections();
            while(sectionIterator.hasNext()){
                String sectionName = sectionIterator.next();
                if (isPreset(iniParser, sectionName)){
                    Preset preset = new Preset();
                    preset.setTitle(sectionName);
                    preset.setMimeType(iniParser.getString(sectionName, "MimeType"));
                    preset.setExtension(iniParser.getString(sectionName, "Ext"));
                    ffPrefs.getPresets().add(preset);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
