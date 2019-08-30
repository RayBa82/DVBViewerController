package org.dvbviewer.controller.data.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TimerViewModelFactory(private val mRepo: TimerRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimerViewModel(mRepo) as T
    }

}