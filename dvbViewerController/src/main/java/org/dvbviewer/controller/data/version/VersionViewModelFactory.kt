package org.dvbviewer.controller.data.version

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class VersionViewModelFactory(private val mApplication: Application, private val mRepo: VersionRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VersionViewModel(mApplication, mRepo) as T
    }

}