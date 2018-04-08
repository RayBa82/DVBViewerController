package org.dvbviewer.controller.data.media;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class MediaViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Application mApplication;
    private MediaRepository mRepo;

    public MediaViewModelFactory(Application application, MediaRepository repo) {
        mApplication = application;
        mRepo = repo;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MediaViewModel(mApplication, mRepo);
    }

}