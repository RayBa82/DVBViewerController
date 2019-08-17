package org.dvbviewer.controller.data.recording

import okhttp3.ResponseBody
import org.dvbviewer.controller.data.api.DMSInterface
import org.dvbviewer.controller.entities.Recording
import retrofit2.Call

class RecordingRepository(private val dmsInterface: DMSInterface) {

    fun getRecordingList(): List<Recording>? {
        return dmsInterface.getRecordings().execute().body()
    }

    fun deleteRecording(recordings: List<Recording>): Call<ResponseBody> {
        return dmsInterface.deleteRecording(recordings.map { it.id }.joinToString(","))
    }

}
