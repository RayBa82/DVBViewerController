package org.dvbviewer.controller.data.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dvbviewer.controller.data.entities.DVBViewerPreferences

class ChannelGroupViewModelFactory(private val prefs: DVBViewerPreferences, private val mRepo: ChannelRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelGroupViewModel(prefs, mRepo) as T
    }

}