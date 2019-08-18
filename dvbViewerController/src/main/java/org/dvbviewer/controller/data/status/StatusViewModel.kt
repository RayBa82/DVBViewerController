package org.dvbviewer.controller.data.status

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.api.ApiResponse
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.data.entities.Status
import org.dvbviewer.controller.data.version.VersionRepository
import org.dvbviewer.controller.data.version.xml.Version

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
                    val status = repo.getStatus2()
                    val version = repo.getVersion()
                    val editor = prefs.prefs.edit()
                    addVersionItem(status, version, editor)
                    val status1 = repo.getStatus()
                    saveDefaultTimerValues(status1, editor)
                    editor.apply()
                    ApiResponse.success(status)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting status", e)
                    ApiResponse.error(e, null)

                }
            }.await()
            data.value = apiResponse
        }
    }

    private fun addVersionItem(status: Status?, version: Version?, editor: SharedPreferences.Editor) {
        if (status == null || version == null) {
            return
        }
        val versionItem = Status.StatusItem()
        versionItem.nameRessource = R.string.status_server_version
        versionItem.value = version.version
        status.items.add(0, versionItem)
        editor.putInt(DVBViewerPreferences.KEY_RS_IVER, version.internalVersion)

    }

    private fun saveDefaultTimerValues(status: Status?, editor: SharedPreferences.Editor) {
        if (status == null) {
            return
        }
        editor.putInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, status.epgBefore)
        editor.putInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, status.epgAfter)
        editor.putInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, status.defAfterRecord)

    }

    companion object {

        private val TAG = StatusViewModel::class.java.name

    }

}