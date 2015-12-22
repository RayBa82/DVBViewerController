package org.dvbviewer.controller.io.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by karlw on 18.12.2015.
 */
public class FFMPEGPrefsHandler {

    public List<String> parse(String ffmpegprefs) {

        List<String> prefs = new ArrayList<String>();
        Matcher m = Pattern.compile("[\\s\\t]*\\[([^\\[\\]]*)\\][\\s\\t]*\\r?\\n[\\s\\t]*Cmd=.*")
                .matcher(ffmpegprefs);
        while (m.find()) {
            prefs.add(m.group(1));
        }

        return prefs;
    }
}
