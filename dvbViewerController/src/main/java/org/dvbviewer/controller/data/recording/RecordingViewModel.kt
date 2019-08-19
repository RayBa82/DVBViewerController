package org.dvbviewer.controller.data.stream

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dvbviewer.controller.data.api.ApiResponse
import org.dvbviewer.controller.data.entities.Recording
import org.dvbviewer.controller.data.recording.RecordingRepository


class RecordingViewModel internal constructor(private val mRepository: RecordingRepository) : ViewModel() {

    private var data: MutableLiveData<ApiResponse<List<Recording>>> = MutableLiveData()

    fun getRecordingList(force: Boolean = false): MutableLiveData<ApiResponse<List<Recording>>> {
        if (data.value == null || force) {
            fetchRecordingList()
        }
        return data
    }

    fun fetchRecordingList() {
        viewModelScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            var apiResponse: ApiResponse<List<Recording>> = ApiResponse.error(null, null)
            async(Dispatchers.Default) {
                apiResponse = try {
                    ApiResponse.success(mRepository.getRecordingList())
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting recording list", e)
                    ApiResponse.error(e, null)
                }
            }.await()
            data.value = apiResponse


        }
    }

    companion object {

        private const val TAG = "RecordingViewModel"
    }

}