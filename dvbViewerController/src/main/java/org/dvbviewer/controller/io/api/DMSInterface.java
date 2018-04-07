package org.dvbviewer.controller.io.api;

import org.dvbviewer.controller.data.media.xml.VideoDirsFiles;
import org.dvbviewer.controller.data.task.xml.TaskList;
import org.dvbviewer.controller.data.version.xml.Version;

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

    String MEDIA_DIRS               = API + "mediafiles.html?content=3&recursive=0&thumbs=1";

    String VERSION               = API + "version.html";

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

    @GET(MEDIA_DIRS)
    Call<VideoDirsFiles> getMediaDir(@Query("dirid") long id);

    @GET(VERSION)
    Call<Version> getVersion();

}