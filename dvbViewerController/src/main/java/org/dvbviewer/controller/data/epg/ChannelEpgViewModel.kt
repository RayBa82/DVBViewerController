package org.dvbviewer.controller.data.epg

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dvbviewer.controller.entities.EpgEntry
import java.util.*

class ChannelEpgViewModel(private val repository: EPGRepository) : ViewModel() {


    private val data: MutableLiveData<List<EpgEntry>> = MutableLiveData()

    fun getChannelEPG(channel: Long, start: Date, end: Date, force: Boolean = false): MutableLiveData<List<EpgEntry>> {
        if (data.value == null || force) {
            fetchChannelEPG(channel, start, end)
        }
        return data
    }


    private fun fetchChannelEPG(channel: Long, start: Date, end: Date) {
        viewModelScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            val groupList: MutableList<EpgEntry> = mutableListOf()
            async(Dispatchers.IO) {
                try {
                    repository.getChannelEPG(channel, start, end)?.let { groupList.addAll(it) }
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "Error getting EPG Entry", e)
                }

            }.await()

            data.value = groupList

        }
    }

}