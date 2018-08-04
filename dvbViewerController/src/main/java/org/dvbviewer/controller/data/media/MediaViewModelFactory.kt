package org.dvbviewer.controller.data.media

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class MediaViewModelFactory(private val mApplication: Application, private val mRepo: MediaRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MediaViewModel(mApplication, mRepo) as T
    }

}