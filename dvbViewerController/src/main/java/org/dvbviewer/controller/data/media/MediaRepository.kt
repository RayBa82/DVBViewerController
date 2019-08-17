package org.dvbviewer.controller.data.media

import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.entities.NaturalOrderComparator
import org.dvbviewer.controller.io.api.DMSInterface
import java.util.*
import kotlin.math.max

/**
 * Created by rbaun on 02.04.18.
 */
class MediaRepository(private val dmsInterface: DMSInterface) {
    private val comparator = NaturalOrderComparator()

    fun getMedias(dirid: Long): List<MediaFile> {
        val videoDirs = dmsInterface.getMediaDir(dirid).execute().body()
        val mediaFiles = ArrayList<MediaFile>()
        if (CollectionUtils.isNotEmpty(videoDirs!!.dirs)) {
            val dirs = ArrayList<MediaFile>()
            videoDirs.dirs?.forEach { dir ->
                val f = MediaFile()
                f.dirId = dir.dirid
                val dirArr = StringUtils.split(dir.path, "\\")
                val index = max(0, dirArr.size - 1)
                f.name = dirArr[index]
                dirs.add(f)
            }
            Collections.sort(dirs, comparator)
            mediaFiles.addAll(dirs)
        }
        if (CollectionUtils.isNotEmpty(videoDirs.files)) {
            val files = ArrayList<MediaFile>()
            videoDirs.files?.forEach { file ->
                val f = MediaFile()
                f.dirId = -1L
                f.id = file.objid
                f.name = file.name
                f.thumb = file.thumb
                files.add(f)
            }
            Collections.sort(files, comparator)
            mediaFiles.addAll(files)
        }
        return mediaFiles
    }

}