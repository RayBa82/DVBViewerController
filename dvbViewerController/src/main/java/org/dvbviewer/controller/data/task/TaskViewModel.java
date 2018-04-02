package org.dvbviewer.controller.data.task;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import org.dvbviewer.controller.data.task.xml.TaskList;

/**
 * Created by rbaun on 02.04.18.
 */

public class TaskViewModel extends ViewModel {

    TaskRepository repository;

    public TaskViewModel() {
        repository = new TaskRepository();
    }

    public MutableLiveData<TaskList> getTaskList() {
        return repository.getTaskList();
    }

}