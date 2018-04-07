package org.dvbviewer.controller.data.version;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;

/**
 * Created by rbaun on 02.04.18.
 */

public class VersionViewModel extends AndroidViewModel {

    VersionRepository repository;

    public VersionViewModel(Application application) {
        super(application);
        repository = new VersionRepository(application.getBaseContext());
    }

    public MutableLiveData<String> getVersion() {
        return repository.getVersion();
    }

}