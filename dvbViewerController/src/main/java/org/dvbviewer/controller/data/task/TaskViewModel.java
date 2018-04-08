package org.dvbviewer.controller.data.task;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import org.dvbviewer.controller.data.ApiResponse;
import org.dvbviewer.controller.data.DmsViewModel;
import org.dvbviewer.controller.data.task.xml.TaskList;

/**
 * Created by rbaun on 02.04.18.
 */

public class TaskViewModel extends DmsViewModel {

    private static final String TAG = TaskViewModel.class.getName();

    private final TaskRepository repository;

    private MutableLiveData<ApiResponse<TaskList>> data;


    public TaskViewModel(Application application) {
        super(application);
        repository = new TaskRepository();
    }

    public MutableLiveData<ApiResponse<TaskList>> getTaskList() {
        if (data == null) {
            data = new MutableLiveData<>();
            fetchTaskList();
        }
        return data;
    }

    public void fetchTaskList() {
        new AsyncTask<Void,Void,ApiResponse<TaskList>>() {

            @Override
            protected ApiResponse<TaskList> doInBackground(Void... voids) {
                try {
                    return ApiResponse.success(repository.getTaskList());
                } catch (Exception e) {
                    Log.e(TAG, "Error loading Data", e);
                    final String message = getErrorMessage(e);
                    return ApiResponse.error(message, null);
                }
            }

            @Override
            protected void onPostExecute(ApiResponse<TaskList> value) {
                data.setValue(value);
            }
        }.execute();
    }


}