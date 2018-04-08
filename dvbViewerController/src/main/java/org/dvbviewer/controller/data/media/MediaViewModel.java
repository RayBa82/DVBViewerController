package org.dvbviewer.controller.data.media;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import org.dvbviewer.controller.data.ApiResponse;
import org.dvbviewer.controller.data.DmsViewModel;

import java.io.IOException;
import java.util.List;

/**
 * Created by rbaun on 02.04.18.
 */

public class MediaViewModel extends DmsViewModel {

    private static final String TAG = MediaViewModel.class.getName();

    private final MediaRepository mRepository;
    private MutableLiveData<ApiResponse<List<MediaFile>>> data;

    MediaViewModel(Application application, MediaRepository repository) {
        super(application);
        mRepository = repository;
    }

    public MutableLiveData<ApiResponse<List<MediaFile>>> getMedias(final long dirid) {
        if(data == null) {
            data = new MutableLiveData<>();
            fetchMedias(dirid);
        }
        return data;
    }

    public void fetchMedias(final long dirid) {
        new AsyncTask<Void,Void,ApiResponse<List<MediaFile>>>() {

            @Override
            protected ApiResponse<List<MediaFile>> doInBackground(Void... voids) {
                try {
                    return ApiResponse.success(mRepository.getMedias(dirid));
                } catch (IOException e) {
                    Log.e(TAG, "Error loading Data", e);
                    final String message = getErrorMessage(e);
                    return ApiResponse.error(message, null);
                }
            }

            @Override
            protected void onPostExecute(ApiResponse<List<MediaFile>> response) {
                data.setValue(response);
            }
        }.execute();
    }

}