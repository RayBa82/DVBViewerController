package org.dvbviewer.controller.data.media.xml;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rbaun on 02.04.18.
 */

@Root(name = "videodirsfiles", strict = false)
public class VideoDirsFiles {

    @ElementList(inline = true, required = false)
    private List<Dir> dirs;

    @ElementList(inline = true, required = false)
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
