package org.dvbviewer.controller.data.channel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import org.dvbviewer.controller.data.entities.ChannelGroup
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.utils.Config
import java.util.*

class ChannelGroupViewModel(private val prefs: DVBViewerPreferences, private val channelRepository: ChannelRepository) : ViewModel() {


    private val data: MutableLiveData<List<ChannelGroup>> = MutableLiveData()

    fun getGroupList(fav: Boolean, force: Boolean = false): MutableLiveData<List<ChannelGroup>> {
        if (data.value == null || force) {
            fetchChannelGroups(fav)
        }
        return data
    }


    private fun fetchChannelGroups(fav: Boolean) {
        viewModelScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            val groupList: MutableList<ChannelGroup> = mutableListOf()
            try {
                async(Dispatchers.Main) {
                    if (Config.CHANNELS_SYNCED) {
                        groupList.addAll(channelRepository.getGroups(fav))
                        fetchEpg()
                    } else {
                        syncChannels(fav)
                    }
                }.await()

                data.value = groupList
            } catch (e: Exception) {
                data.value = Collections.emptyList()
            }

        }
    }

    fun fetchEpg() {
        viewModelScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
            withContext(Dispatchers.IO) {
                try {
                    channelRepository.saveEpg()
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "Error saving EPG", e)
                }
            }
        }
    }

    fun syncChannels(fav: Boolean) {
        viewModelScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
            withContext(Dispatchers.IO) {
                try {
                    channelRepository.syncChannels()
                    Config.CHANNELS_SYNCED = true
                    prefs.prefs
                            .edit()
                            .putBoolean(DVBViewerPreferences.KEY_CHANNELS_SYNCED, true)
                            .apply()
                    getGroupList(fav, true)
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "Error syncing channels", e)
                }
            }

        }
    }

}