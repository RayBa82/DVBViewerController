package org.dvbviewer.controller.io.api;

import org.dvbviewer.controller.data.media.xml.VideoDirsFiles;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by rbaun on 02.04.18.
 */

public interface DMSMediaInterface {

    String MEDIA_DIRS                 = DMSInterface.API + "mediafiles.html?content=3&recursive=0";

    @GET(MEDIA_DIRS)
    Call<VideoDirsFiles> getMediaDir(@Query("dirid") long id);

}
