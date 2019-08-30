package org.dvbviewer.controller.data.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecordingViewModelFactory(private val mRepo: RecordingRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecordingViewModel(mRepo) as T
    }

}