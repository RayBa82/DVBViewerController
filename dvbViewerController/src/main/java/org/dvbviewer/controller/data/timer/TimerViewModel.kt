package org.dvbviewer.controller.data.timer

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dvbviewer.controller.data.api.ApiResponse
import org.dvbviewer.controller.data.entities.Timer


class TimerViewModel internal constructor(private val mRepository: TimerRepository) : ViewModel() {

    private val data: MutableLiveData<ApiResponse<List<Timer>>> = MutableLiveData()

    fun getTimerList(force: Boolean = false): MutableLiveData<ApiResponse<List<Timer>>> {
        if (data.value == null || force) {
            fetchTimerList()
        }
        return data
    }

    private fun fetchTimerList() {
        viewModelScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            var apiResponse: ApiResponse<List<Timer>> = ApiResponse.error(null, null)
            async(Dispatchers.Default) {
                apiResponse = try {
                    ApiResponse.success(mRepository.getTimer())
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting timer list", e)
                    ApiResponse.error(e, null)
                }
            }.await()
            data.value = apiResponse


        }
    }

    companion object {

        private const val TAG = "TimerViewModel"
    }

}