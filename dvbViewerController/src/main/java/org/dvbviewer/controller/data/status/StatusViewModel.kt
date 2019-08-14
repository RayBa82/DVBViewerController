package org.dvbviewer.controller.data.status

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.version.VersionRepository
import org.dvbviewer.controller.data.version.xml.Version
import org.dvbviewer.controller.entities.DVBViewerPreferences
import org.dvbviewer.controller.entities.Status

class StatusViewModel internal constructor(private val prefs: DVBViewerPreferences, private val repo: VersionRepository) : ViewModel() {

    private val data: MutableLiveData<ApiResponse<Status>> = MutableLiveData()

    fun getStatus(force: Boolean = false): MutableLiveData<ApiResponse<Status>> {
        if (data.value == null || force) {
            fetchStatus()
        }
        return data
    }

    fun fetchStatus() {
        viewModelScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            var apiResponse: ApiResponse<Status> = ApiResponse.error(null, null)
            async(Dispatchers.Default) {
                apiResponse = try {
                    val status = repo.getStatus()
                    val version = repo.getVersion()
                    addVersionItem(status, version)
                    ApiResponse.success(status)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting status", e)
                    ApiResponse.error(e, null)

                }
            }.await()
            data.value = apiResponse
        }
    }

    private fun addVersionItem(status: Status?, version: Version?) {
        if (status == null || version == null) {
            return
        }
        val versionItem = Status.StatusItem()
        versionItem.nameRessource = R.string.status_server_version
        versionItem.value = version.version
        status.items.add(0, versionItem)
        prefs.prefs.edit()
                .putInt(DVBViewerPreferences.KEY_RS_IVER, version.internalVersion)
                .apply()

    }

    companion object {

        private val TAG = StatusViewModel::class.java.name

    }

}