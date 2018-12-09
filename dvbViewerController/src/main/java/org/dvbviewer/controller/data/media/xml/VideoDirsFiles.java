package org.dvbviewer.controller.data.media.xml;

import com.tickaroo.tikxml.annotation.Element;
import com.tickaroo.tikxml.annotation.Xml;

import java.util.List;

/**
 * Created by rbaun on 02.04.18.
 */

@Xml(name = "videodirsfiles")
public class VideoDirsFiles {

    @Element
    private List<Dir> dirs;

    @Element
    private List<File> files;

    public List<Dir> getDirs() {
        return dirs;
    }

    public void setDirs(List<Dir> dirs) {
        this.dirs = dirs;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}
