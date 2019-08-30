package org.dvbviewer.controller.data.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.data.version.VersionRepository

class StatusViewModelFactory(private val prefs: DVBViewerPreferences, private val mRepo: VersionRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatusViewModel(prefs, mRepo) as T
    }

}