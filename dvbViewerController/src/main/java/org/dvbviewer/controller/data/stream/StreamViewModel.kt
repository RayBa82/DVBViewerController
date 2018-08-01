package org.dvbviewer.controller.data.stream

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.DmsViewModel
import org.dvbviewer.controller.entities.FFMpegPresetList

/**
 * Created by rbaun on 02.04.18.
 */

class StreamViewModel internal constructor(application: Application, private val mRepository: StreamRepository) : DmsViewModel(application) {

    private var data: MutableLiveData<ApiResponse<FFMpegPresetList>>? = null

    fun getFFMpegPresets(): MutableLiveData<ApiResponse<FFMpegPresetList>> {
        if (data == null) {
            data = MutableLiveData()
            fetchFFMpegPresets()
        }
        return data as MutableLiveData<ApiResponse<FFMpegPresetList>>
    }

    fun fetchFFMpegPresets() {
        if (data == null) {
            return
        }
        launch(UI) {
            var prefs = FFMpegPresetList()
            try {
                async(CommonPool) {
                    prefs = mRepository.getFFMpegPresets(getApplication())
                }.await()
                data?.value = ApiResponse.success(prefs)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting mediafiles", e)
                val message = getErrorMessage(e)
                data?.value = ApiResponse.error(message, null)
            }

        }
    }

    companion object {

        private val TAG = StreamViewModel::class.java.name
    }

}