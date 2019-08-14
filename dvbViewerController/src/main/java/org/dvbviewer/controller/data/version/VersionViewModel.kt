package org.dvbviewer.controller.data.version

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
        viewModelScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            var apiResponse = ApiResponse.error(null, false)

                async(Dispatchers.Default) {
                    apiResponse = try {
                        ApiResponse.success(vRepo.isSupported(minVersion))
                    } catch (e: Exception) {
                        Log.e(TAG , "Error getting version", e)
                        ApiResponse.error(e, false)
                    }
                }.await()

            data?.value = apiResponse
        }
    }

    companion object {

        private val TAG = VersionViewModel::class.java.name

    }

}