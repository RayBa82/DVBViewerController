package org.dvbviewer.controller.data.media;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

/**
 * Created by rbaun on 02.04.18.
 */

public class MediaViewModel extends ViewModel {

    MediaRepository repository;

    private MutableLiveData<List<MediaFile>> data;

    public MediaViewModel() {
        repository = new MediaRepository();
    }

    public MutableLiveData<List<MediaFile>> getMedias(final long dirid) {
        if(data == null) {
            data = new MutableLiveData<>();
            fetchMedias(dirid);
        }
        return data;
    }

    public void fetchMedias(final long dirid) {
        new AsyncTask<Void,Void,List<MediaFile>>() {

            @Override
            protected List<MediaFile> doInBackground(Void... voids) {
                try {
                    return repository.getMedias(dirid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<MediaFile> mediaFiles) {
                data.setValue(mediaFiles);
            }
        }.execute();
    }

}