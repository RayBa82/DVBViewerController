package org.dvbviewer.controller.data.media.xml

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * Created by rbaun on 02.04.18.
 */

@Xml(name = "videodirsfiles")
class VideoDirsFiles {

    @Element
    var dirs: List<Dir>? = null

    @Element
    var files: List<File>? = null
}
