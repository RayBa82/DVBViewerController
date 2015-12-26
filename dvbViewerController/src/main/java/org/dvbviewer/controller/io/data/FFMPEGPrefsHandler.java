package org.dvbviewer.controller.io.data;

import android.text.TextUtils;

import org.dvbviewer.controller.entities.FfMpegPrefs;
import org.dvbviewer.controller.entities.Preset;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by karlw on 18.12.2015.
 */
public class FFMPEGPrefsHandler {

    public FfMpegPrefs parse(String ffmpegprefs) {
        FfMpegPrefs ffPrefs = new FfMpegPrefs();
        try {
            Ini ini = new Ini(new StringReader(ffmpegprefs));
            ffPrefs.setVersion(ini.get("Version").get("Version"));
            for (String sectionName : ini.keySet()){
                Section section = ini.get(sectionName);
                final String cmd = section.get("Cmd");
                if (!TextUtils.isEmpty(cmd)){
                    Preset preset = new Preset();
                    preset.setTitle(sectionName);
                    preset.setMimeType(section.get("MimeType"));
                    preset.setExtension(section.get("Ext"));
                    ffPrefs.getPresets().add(preset);
                }
                for (String optionKey: section.keySet()) {
                    System.out.println("\t"+optionKey+"="+section.get(optionKey));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ffPrefs;
    }
}
