package org.dvbviewer.controller.data.version

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.DmsViewModel

/**
 * Created by rbaun on 02.04.18.
 */

class VersionViewModel internal constructor(application: Application, private val vRepo: VersionRepository) : DmsViewModel(application) {

    private var supported: MutableLiveData<ApiResponse<Boolean>>? = null

    fun isSupported(minVersion: Int): MutableLiveData<ApiResponse<Boolean>> {
        if (supported == null) {
            supported = MutableLiveData()
            fetchSupported(minVersion)
        }
        return supported as MutableLiveData<ApiResponse<Boolean>>
    }

    fun fetchSupported(minVersion: Int) {
        launch(UI) {
            var apiResponse = ApiResponse.error("", false)
            try {
                async(CommonPool) {
                    apiResponse = ApiResponse.success(vRepo.isSupported(minVersion))
                }.await()
            } catch (e: Exception) {
                Log.e(TAG , "Error getting version", e)
                val message = getErrorMessage(e)
                apiResponse = ApiResponse.error(message, false)
            }
            supported?.value = apiResponse
        }
    }

    companion object {

        private val TAG = VersionViewModel::class.java.name

    }

}