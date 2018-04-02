package org.dvbviewer.controller.data.repository;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dvbviewer.controller.entities.MediaFile;
import org.dvbviewer.controller.entities.NaturalOrderComparator;
import org.dvbviewer.controller.entities.xml.media.Dir;
import org.dvbviewer.controller.entities.xml.media.File;
import org.dvbviewer.controller.entities.xml.media.VideoDirsFiles;
import org.dvbviewer.controller.io.api.APIClient;
import org.dvbviewer.controller.io.api.DMSInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rbaun on 02.04.18.
 */

public class MediaRepository {

    private static final String TAG = MediaRepository.class.getName();

    DMSInterface dmsInterface;

    MutableLiveData<List<MediaFile>> medias;

    NaturalOrderComparator comparator = new NaturalOrderComparator();

    public MediaRepository() {
        dmsInterface = APIClient.getClient().create(DMSInterface.class);
    }

    public MutableLiveData<List<MediaFile>> getMedias(long dirid) {
        if (medias == null) {
            medias = new MutableLiveData<>();
            loadMedias(dirid);
        }
        return medias;
    }

    private void loadMedias(long dirid) {
        dmsInterface.getMediaDir(dirid).enqueue(new Callback<VideoDirsFiles>() {
            @Override
            public void onResponse(Call<VideoDirsFiles> call, Response<VideoDirsFiles> response) {
                final List<MediaFile> mediaFiles = new ArrayList<>();
                VideoDirsFiles videoDirs = response.body();
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

                medias.setValue(mediaFiles);

            }

            @Override
            public void onFailure(Call<VideoDirsFiles> call, Throwable t) {
                Log.e(TAG, "Error getting Medias", t);
                medias.setValue(null);
            }
        });
    }

}
