package org.dvbviewer.controller.data.task;

import org.dvbviewer.controller.data.task.xml.TaskList;
import org.dvbviewer.controller.io.api.APIClient;
import org.dvbviewer.controller.io.api.DMSInterface;

import java.io.IOException;

/**
 * Created by rbaun on 02.04.18.
 */

public class TaskRepository {

    private final DMSInterface dmsInterface;

    public TaskRepository() {
        dmsInterface = APIClient.getClient().create(DMSInterface.class);
    }

    public TaskList getTaskList() throws IOException {
        return dmsInterface.getTaskList(0).execute().body();
    }

}