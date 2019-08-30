package org.dvbviewer.controller.data.remote

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.data.api.ApiResponse
import org.dvbviewer.controller.data.entities.DVBTarget
import org.dvbviewer.controller.data.entities.DVBViewerPreferences


class RemoteViewModel internal constructor(private val mRepository: RemoteRepository, private val prefs: DVBViewerPreferences) : ViewModel() {

    private var data: MutableLiveData<ApiResponse<List<DVBTarget>>> = MutableLiveData()
    private val gson = Gson()
    private val type = object : TypeToken<List<DVBTarget>>() {}.type

    fun getTargets(force: Boolean = false): MutableLiveData<ApiResponse<List<DVBTarget>>> {
        if (data.value == null || force) {
            fetchTargets()
        }
        return data
    }

    private fun fetchTargets() {
        viewModelScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            var apiResponse: ApiResponse<List<DVBTarget>> = ApiResponse.error(null, null)
            async(Dispatchers.Default) {
                apiResponse = try {
                    var targets = mRepository.getTargets()
                    if (CollectionUtils.isNotEmpty(targets)) {
                        prefs.prefs.edit()
                                .putString(DVBViewerPreferences.KEY_RS_CLIENTS, gson.toJson(targets))
                                .apply()
                    } else {
                        val prefValue = prefs.prefs.getString(DVBViewerPreferences.KEY_RS_CLIENTS, "")
                        if (StringUtils.isNotBlank(prefValue)) {
                            try {
                                targets = gson.fromJson<List<DVBTarget>>(prefValue, type)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error reading Targets vom prefs", e)
                            }
                        }
                    }
                    ApiResponse.success(targets)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting recording list", e)
                    ApiResponse.error(e, null)
                }
            }.await()
            data.value = apiResponse
        }
    }

    companion object {

        private const val TAG = "RemoteViewModel"
    }

}