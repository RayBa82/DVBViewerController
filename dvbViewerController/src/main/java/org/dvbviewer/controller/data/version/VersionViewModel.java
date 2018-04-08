package org.dvbviewer.controller.data.version;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * Created by rbaun on 02.04.18.
 */

public class VersionViewModel extends AndroidViewModel {

    private static final String TAG = VersionViewModel.class.getName();

    VersionRepository repository;

    MutableLiveData<Boolean> supported;

    public VersionViewModel(Application application) {
        super(application);
        repository = new VersionRepository(application.getBaseContext());
    }

    public MutableLiveData<Boolean> isSupported(final int minVersion) {
        if(supported == null) {
            supported = new MutableLiveData<>();
            fetchSupported(minVersion);
        }
        return supported;
    }

    public void fetchSupported(final int minVersion) {
        new AsyncTask<Void,Void,Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return repository.isSupported(minVersion);
                } catch (IOException e) {
                    Log.e(TAG, "error loading Version", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean value) {
                supported.setValue(value);
            }
        }.execute();
    }

}