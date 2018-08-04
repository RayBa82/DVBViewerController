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

    private var data: MutableLiveData<ApiResponse<Boolean>>? = null

    fun isSupported(minVersion: String): MutableLiveData<ApiResponse<Boolean>> {
        if (data == null) {
            data = MutableLiveData()
            fetchSupported(minVersion)
        }
        return data as MutableLiveData<ApiResponse<Boolean>>
    }

    fun fetchSupported(minVersion: String) {
        if(data == null) {
            return
        }
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
            data?.value = apiResponse
        }
    }

    companion object {

        private val TAG = VersionViewModel::class.java.name

    }

}