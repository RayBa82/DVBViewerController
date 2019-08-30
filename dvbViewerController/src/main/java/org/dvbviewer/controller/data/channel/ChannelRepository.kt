package org.dvbviewer.controller.data.channel

import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.data.DbHelper
import org.dvbviewer.controller.data.ProviderConsts
import org.dvbviewer.controller.data.api.DMSInterface
import org.dvbviewer.controller.data.entities.ChannelGroup
import org.dvbviewer.controller.data.entities.ChannelRoot
import org.dvbviewer.controller.utils.DateUtils
import java.util.*

class ChannelRepository(private val dmsInterface: DMSInterface, private val dbHelper: DbHelper) {

    fun syncChannels() {
        val allChans: LinkedList<ChannelRoot> = LinkedList()
        val chans = dmsInterface.getChannelList().execute().body()
        chans?.let { allChans.addAll(it) }
        val favs = dmsInterface.getFavList().execute().body()
        favs?.forEach {
            val toReplace = it.name
            it.groups.forEach { group ->
                group.name = group.name.replace(toReplace, StringUtils.EMPTY)
                group.type = ChannelGroup.TYPE_FAV
            }
        }
        favs?.let { allChans.addAll(it) }
        dbHelper.saveChannelRoots(allChans)
    }

    fun saveEpg() {
        val nowFloat = DateUtils.getFloatDate(Date())
        val epgList = dmsInterface.getProgramm(nowFloat, nowFloat).execute().body()
        dbHelper.saveNowPlaying(epgList)
    }

    fun getGroups(fav: Boolean): MutableList<ChannelGroup> {
        val groups: MutableList<ChannelGroup> = mutableListOf()
        val db = dbHelper.readableDatabase
        val selection = if (fav) ProviderConsts.GroupTbl.TYPE + " = " + ChannelGroup.TYPE_FAV else ProviderConsts.GroupTbl.TYPE + " = " + ChannelGroup.TYPE_CHAN
        val orderBy = ProviderConsts.GroupTbl._ID
        val cursor = db.query(ProviderConsts.GroupTbl.TABLE_NAME, null, selection, null, null, null, orderBy)
        while (cursor.moveToNext()) {
            val group = ChannelGroup()
            group.id = cursor.getLong(cursor.getColumnIndex(ProviderConsts.GroupTbl._ID))
            group.name = cursor.getString(cursor.getColumnIndex(ProviderConsts.GroupTbl.NAME))
            groups.add(group)
        }
        cursor.close()
        db.close()
        return groups
    }

}
