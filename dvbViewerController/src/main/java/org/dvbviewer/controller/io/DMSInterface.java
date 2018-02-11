package org.dvbviewer.controller.io;

import org.dvbviewer.controller.entities.TaskList;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface DMSInterface {

    String API = "api/";

    String TASK_API                 = API + "tasks.html";
    String REC_DEL_API              = API + "recdelete.html";
    String TIMER_ADD_API            = API + "timeradd.html";
    String TIMER_EDIT_API           = API + "timeredit.html";

    @GET(TASK_API)
    Call<TaskList> getTaskList(@Query("all") int all);

    @GET(TASK_API)
    Call<ResponseBody> executeTask(@Query("action") String action);

    @GET(REC_DEL_API)
    Call<ResponseBody> deleteRecording(@Query("recid") long id, @Query("delfile") int delete);

    @GET(TIMER_ADD_API)
    Call<ResponseBody> addTimer(@QueryMap Map<String, String> params);

    @GET(TIMER_EDIT_API)
    Call<ResponseBody> editTimer(@QueryMap Map<String, String> params);

}