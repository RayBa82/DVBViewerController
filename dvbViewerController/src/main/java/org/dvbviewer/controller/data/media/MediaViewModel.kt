package org.dvbviewer.controller.data.media

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.DmsViewModel

/**
 * Created by rbaun on 02.04.18.
 */

class MediaViewModel internal constructor(application: Application, private val mRepository: MediaRepository) : DmsViewModel(application) {

    private var data: MutableLiveData<ApiResponse<List<MediaFile>>>? = null

    fun getMedias(dirid: Long): MutableLiveData<ApiResponse<List<MediaFile>>> {
        if (data == null) {
            data = MutableLiveData()
            fetchMedias(dirid)
        }
        return data as MutableLiveData<ApiResponse<List<MediaFile>>>
    }

    fun fetchMedias(dirid: Long) {
        if(data == null) {
            return
        }
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            var mediaList = listOf<MediaFile>()
            try {
                async(Dispatchers.Default) {
                    mediaList = mRepository.getMedias(dirid)
                }.await()
                data?.value = ApiResponse.success(mediaList)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting mediafiles", e)
                val message = getErrorMessage(e)
                data?.value = ApiResponse.error(message, null)
            }

        }
    }

    companion object {

        private val TAG = MediaViewModel::class.java.name
    }

}