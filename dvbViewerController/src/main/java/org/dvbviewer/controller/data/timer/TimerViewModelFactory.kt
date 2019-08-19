package org.dvbviewer.controller.data.stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dvbviewer.controller.data.timer.TimerRepository

class TimerViewModelFactory(private val mRepo: TimerRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimerViewModel(mRepo) as T
    }

}