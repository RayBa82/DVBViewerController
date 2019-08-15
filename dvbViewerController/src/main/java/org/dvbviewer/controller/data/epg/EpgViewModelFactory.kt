package org.dvbviewer.controller.data.epg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EpgViewModelFactory(private val mRepo: EPGRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChannelEpgViewModel(mRepo) as T
    }

}