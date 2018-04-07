package org.dvbviewer.controller.data.media;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dvbviewer.controller.data.media.xml.Dir;
import org.dvbviewer.controller.data.media.xml.File;
import org.dvbviewer.controller.data.media.xml.VideoDirsFiles;
import org.dvbviewer.controller.entities.NaturalOrderComparator;
import org.dvbviewer.controller.io.api.APIClient;
import org.dvbviewer.controller.io.api.DMSInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rbaun on 02.04.18.
 */

public class MediaRepository {

    private static final String TAG = MediaRepository.class.getName();

    DMSInterface dmsInterface;

    List<MediaFile> medias;

    NaturalOrderComparator comparator = new NaturalOrderComparator();

    public MediaRepository() {
        dmsInterface = APIClient.getClient().create(DMSInterface.class);
    }

    public List<MediaFile> getMedias(long dirid) throws IOException {
            final VideoDirsFiles videoDirs = dmsInterface.getMediaDir(dirid).execute().body();
            final List<MediaFile> mediaFiles = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(videoDirs.getDirs())) {
                final List<MediaFile> dirs = new ArrayList<>();
                for (Dir dir : videoDirs.getDirs()) {
                    MediaFile f = new MediaFile();
                    f.setDirId(dir.getDirid());
                    String[] dirArr = StringUtils.split(dir.getPath(), "\\");
                    int index = Math.max(0, dirArr.length-1);
                    f.setName(dirArr[index]);
                    dirs.add(f);
                }
                Collections.sort(dirs, comparator);
                mediaFiles.addAll(dirs);
            }
            if (CollectionUtils.isNotEmpty(videoDirs.getFiles())) {
                final List<MediaFile> files = new ArrayList<>();
                for (File file : videoDirs.getFiles()) {
                    MediaFile f = new MediaFile();
                    f.setDirId(-1l);
                    f.setId(file.getObjid());
                    f.setName(file.getName());
                    f.setThumb(file.getThumb());
                    files.add(f);
                }
                Collections.sort(files, comparator);
                mediaFiles.addAll(files);
            }
            return mediaFiles;
    }


}
