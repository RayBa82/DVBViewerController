package org.dvbviewer.controller.data.stream;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class StreamViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Application mApplication;
    private StreamRepository mRepo;

    public StreamViewModelFactory(Application application, StreamRepository repo) {
        mApplication = application;
        mRepo = repo;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new StreamViewModel(mApplication, mRepo);
    }

}