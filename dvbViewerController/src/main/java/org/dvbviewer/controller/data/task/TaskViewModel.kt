package org.dvbviewer.controller.data.task

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.DmsViewModel
import org.dvbviewer.controller.data.api.APIClient
import org.dvbviewer.controller.data.api.DMSInterface
import org.dvbviewer.controller.data.task.xml.TaskList
import org.dvbviewer.controller.data.version.VersionRepository
import java.text.MessageFormat

/**
 * Created by rbaun on 02.04.18.
 */
class TaskViewModel(application: Application) : DmsViewModel(application) {

    private val taskRepo: TaskRepository
    private val versionRepo: VersionRepository

    init {
        val dmsInterface = APIClient.client.create(DMSInterface::class.java)
        taskRepo = TaskRepository(dmsInterface)
        versionRepo = VersionRepository(application, dmsInterface)
    }

    private var data: MutableLiveData<ApiResponse<TaskList>>? = null

    val taskList: MutableLiveData<ApiResponse<TaskList>>
        get() {
            if (data == null) {
                data = MutableLiveData()
                fetchTaskList()
            }
            return data as MutableLiveData<ApiResponse<TaskList>>
        }


    private fun fetchTaskList() {
        if (data == null) {
            return
        }
        viewModelScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            var mediaList = TaskList()
            var isSupported = false
            var exception: java.lang.Exception? = null
            async(Dispatchers.Default) {
                try {
                    isSupported = versionRepo.isSupported(MIN_VERSION)
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking support", e)
                    exception = e
                }
            }.await()
            if (exception != null) {
                data?.value = ApiResponse.error(exception, mediaList)
                return@launch
            }

            if (!isSupported) {
                val res = getApplication<Application>().resources
                data?.value = ApiResponse.notSupported(MessageFormat.format(res.getString(R.string.version_unsupported_text), MIN_VERSION))
                return@launch
            }

            async(Dispatchers.Default) {
                try {
                    mediaList = taskRepo.taskList
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting tasks", e)
                    exception = e
                }
            }.await()

            if (exception != null) {
                data?.value = ApiResponse.error(exception, mediaList)
            } else {
                data?.value = ApiResponse.success(mediaList)
            }

        }
    }

    companion object {

        private val TAG = TaskViewModel::class.java.name
        private const val MIN_VERSION = "2.0.3.0"

    }


}