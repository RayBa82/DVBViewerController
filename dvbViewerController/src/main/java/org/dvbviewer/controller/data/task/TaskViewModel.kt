package org.dvbviewer.controller.data.task

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.DmsViewModel
import org.dvbviewer.controller.data.task.xml.TaskList
import org.dvbviewer.controller.data.version.VersionRepository
import org.dvbviewer.controller.io.api.APIClient
import org.dvbviewer.controller.io.api.DMSInterface
import java.text.MessageFormat

/**
 * Created by rbaun on 02.04.18.
 */
class TaskViewModel(application: Application) : DmsViewModel(application) {

    private val taskRepo: TaskRepository
    private val versionRepo: VersionRepository

    init {
        val dmsInterface = APIClient.getClient().create(DMSInterface::class.java)
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
        if(data == null) {
            return
        }
        launch(UI) {
            var mediaList = TaskList()
            try {
                var isSupported = false
                async(CommonPool) {
                    isSupported = versionRepo.isSupported(MIN_VERSION)
                }.await()

                if(!isSupported) {
                    val res = getApplication<Application>().resources
                    data?.value = ApiResponse.notSupported(MessageFormat.format(res.getString(R.string.version_unsupported_text), MIN_VERSION))
                    return@launch
                }

                async(CommonPool) {
                    mediaList = taskRepo.taskList
                }.await()

                data?.value = ApiResponse.success(mediaList)

            } catch (e: Exception) {
                Log.e(TAG, "Error getting tasks", e)
                val message = getErrorMessage(e)
                data?.value = ApiResponse.error(message, null)
            }

        }
    }

    companion object {

        private val TAG = TaskViewModel::class.java.name
        private const val MIN_VERSION = "2.0.3.0"

    }


}