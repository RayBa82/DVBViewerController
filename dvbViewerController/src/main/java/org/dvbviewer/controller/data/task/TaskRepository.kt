package org.dvbviewer.controller.data.task

import org.dvbviewer.controller.data.task.xml.TaskList
import org.dvbviewer.controller.io.api.DMSInterface

/**
 * Created by rbaun on 02.04.18.
 */

class TaskRepository(private val dmsInterface: DMSInterface) {

    val taskList: TaskList
        get() = dmsInterface.getTaskList(0).execute().body()!!

}