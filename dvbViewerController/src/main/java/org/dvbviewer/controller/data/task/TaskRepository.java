package org.dvbviewer.controller.data.task;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import org.dvbviewer.controller.data.task.xml.TaskList;
import org.dvbviewer.controller.io.api.APIClient;
import org.dvbviewer.controller.io.api.DMSInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rbaun on 02.04.18.
 */

public class TaskRepository {

    private static final String TAG = TaskRepository.class.getName();

    DMSInterface dmsInterface;

    MutableLiveData<TaskList> tasks;

    public TaskRepository() {
        dmsInterface = APIClient.getClient().create(DMSInterface.class);
    }


    public MutableLiveData<TaskList> getTaskList() {
        if (tasks == null) {
            tasks = new MutableLiveData<>();
            loadTaskList();
        }
        return tasks;
    }

    private void loadTaskList() {
        dmsInterface.getTaskList(0).enqueue(new Callback<TaskList>() {
            @Override
            public void onResponse(Call<TaskList> call, Response<TaskList> response) {
                TaskList taskList = response.body();
                tasks.setValue(taskList);

            }

            @Override
            public void onFailure(Call<TaskList> call, Throwable t) {
                Log.e(TAG, "Error getting tasks", t);
                tasks.setValue(null);
            }
        });
    }

}
