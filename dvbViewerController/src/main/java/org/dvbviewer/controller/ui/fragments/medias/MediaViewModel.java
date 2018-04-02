package org.dvbviewer.controller.ui.fragments.medias;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import org.dvbviewer.controller.data.repository.MediaRepository;
import org.dvbviewer.controller.entities.MediaFile;

import java.util.List;

/**
 * Created by rbaun on 02.04.18.
 */

public class MediaViewModel extends ViewModel {

    MediaRepository repository;

    public MediaViewModel(){
        repository = new MediaRepository();
    }

    public MutableLiveData<List<MediaFile>> getMedias(long dirid) {
        return repository.getMedias(dirid);
    }

}
