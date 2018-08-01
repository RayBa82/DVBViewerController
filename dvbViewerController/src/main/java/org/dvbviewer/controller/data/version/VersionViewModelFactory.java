package org.dvbviewer.controller.data.version;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class VersionViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Application mApplication;
    private VersionRepository mRepo;

    public VersionViewModelFactory(Application application, VersionRepository repo) {
        mApplication = application;
        mRepo = repo;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new VersionViewModel(mApplication, mRepo);
    }

}