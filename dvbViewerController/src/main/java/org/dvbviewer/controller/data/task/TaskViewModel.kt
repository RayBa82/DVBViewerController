package org.dvbviewer.controller.data.task

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.DmsViewModel
import org.dvbviewer.controller.data.task.xml.TaskList

/**
 * Created by rbaun on 02.04.18.
 */
class TaskViewModel(application: Application) : DmsViewModel(application) {

    private val repository: TaskRepository = TaskRepository()

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
                async(CommonPool) {
                    mediaList = repository.taskList
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

    }


}