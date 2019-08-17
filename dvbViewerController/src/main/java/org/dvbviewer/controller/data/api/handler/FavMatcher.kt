package org.dvbviewer.controller.data.api.handler

import org.dvbviewer.controller.data.entities.Channel
import org.dvbviewer.controller.data.entities.ChannelGroup
import org.dvbviewer.controller.data.entities.ChannelRoot
import java.util.*

class FavMatcher {

    fun matchFavs(channelRoots: List<ChannelRoot>, favList: List<ChannelGroup>?): List<ChannelGroup> {
        val result = ArrayList<ChannelGroup>()
        if (favList != null) {
            for (favGroup in favList) {
                val tmp = getCurrentFavGroup(result, favGroup)
                for (fav in favGroup.favs) {
                    val chan = getMatchedChannel(channelRoots, fav.id)
                    if (chan != null) {
                        chan.favPosition = fav.position
                        chan.setFlag(Channel.FLAG_FAV)
                        tmp.channels.add(chan)
                    }
                }
            }
        }
        return result
    }

    private fun getMatchedChannel(channelRoots: List<ChannelRoot>?, favId: Long): Channel? {
        if (channelRoots != null) {
            for (roots in channelRoots) {
                for (group in roots.groups) {
                    for (chan in group.channels) {
                        if (chan.channelID == favId) {
                            return chan
                        }
                    }
                }
            }
        }
        return null
    }

    private fun getCurrentFavGroup(result: MutableList<ChannelGroup>, group: ChannelGroup): ChannelGroup {
        val groupIndex = result.indexOf(group)
        val tmp: ChannelGroup
        if (groupIndex >= 0) {
            tmp = result[groupIndex]
        } else {
            tmp = ChannelGroup()
            tmp.name = group.name
            tmp.type = ChannelGroup.TYPE_FAV
            result.add(tmp)
        }
        return tmp
    }

}
