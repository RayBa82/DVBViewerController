package org.dvbviewer.controller.data.version;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import org.dvbviewer.controller.data.ApiResponse;
import org.dvbviewer.controller.data.DmsViewModel;

/**
 * Created by rbaun on 02.04.18.
 */

public class VersionViewModel extends DmsViewModel {

    private static final String TAG = VersionViewModel.class.getName();

    private VersionRepository vRepo;
    private MutableLiveData<ApiResponse<Boolean>> supported;

    VersionViewModel(Application application, VersionRepository repository) {
        super(application);
        this.vRepo = repository;
    }

    public MutableLiveData<ApiResponse<Boolean>> isSupported(final int minVersion) {
        if(supported == null) {
            supported = new MutableLiveData<>();
            fetchSupported(minVersion);
        }
        return supported;
    }

    public void fetchSupported(final int minVersion) {
        new AsyncTask<Void,Void,ApiResponse<Boolean>>() {

            @Override
            protected ApiResponse<Boolean> doInBackground(Void... voids) {
                try {
                    return ApiResponse.success(vRepo.isSupported(minVersion));
                } catch (Exception e) {
                    Log.e(TAG, "Error loading Data", e);
                    final String message = getErrorMessage(e);
                    return ApiResponse.error(message, null);
                }
            }

            @Override
            protected void onPostExecute(ApiResponse<Boolean> value) {
                supported.setValue(value);
            }
        }.execute();
    }

}