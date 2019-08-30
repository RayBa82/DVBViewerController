package org.dvbviewer.controller.data.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dvbviewer.controller.data.entities.DVBViewerPreferences

class RemoteViewModelFactory(private val mRepo: RemoteRepository, private val prefs: DVBViewerPreferences) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RemoteViewModel(mRepo, prefs) as T
    }

}