package org.dvbviewer.controller.data.stream

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class StreamViewModelFactory(private val mApplication: Application, private val mRepo: StreamRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StreamViewModel(mApplication, mRepo) as T
    }

}