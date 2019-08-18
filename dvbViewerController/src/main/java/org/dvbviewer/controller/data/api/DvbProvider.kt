/*
 * Copyright © 2013 dvbviewer-controller Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dvbviewer.controller.data.api

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import org.dvbviewer.controller.data.DbHelper
import org.dvbviewer.controller.data.ProviderConsts
import org.dvbviewer.controller.data.ProviderConsts.*
import java.util.*

/**
 * The Class DvbProvider.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class DvbProvider : ContentProvider() {

    lateinit var dbHelper: DbHelper


    /* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
    override fun onCreate(): Boolean {
        dbHelper = DbHelper(context)
        return true
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val qb: SQLiteQueryBuilder
        var groupBy: String? = null
        val projectionMap: HashMap<String, String>
        when (sUriMatcher.match(uri)) {
            CHANNELS -> {
                qb = SQLiteQueryBuilder()
                qb.tables = ChannelTbl.TABLE_NAME
            }
            FAVOURITES -> {
                qb = SQLiteQueryBuilder()
                qb.tables = ChannelTbl.TABLE_NAME
            }
            NOW_PLAYING_CHANNELS -> {
                qb = SQLiteQueryBuilder()
                qb.tables = ChannelTbl.AS_ALIAS + " LEFT JOIN " + NowTbl.AS_ALIAS + " on (" + ChannelTbl.ALIAS + "." + ChannelTbl.EPG_ID + " = " + NowTbl.ALIAS + "." + NowTbl.EPG_ID + " AND " + NowTbl.ALIAS + "." + NowTbl.START + " < " + Date().time + " AND " + NowTbl.ALIAS + "." + NowTbl.END + " > " + Date().time + ")"
                projectionMap = HashMap()
                projectionMap[ChannelTbl._ID] = ChannelTbl.ALIAS + "." + ChannelTbl._ID + " as " + ChannelTbl._ID
                projectionMap[ChannelTbl.CHANNEL_ID] = ChannelTbl.ALIAS + "." + ChannelTbl.CHANNEL_ID + " as " + ChannelTbl.CHANNEL_ID
                projectionMap[ChannelTbl.POSITION] = ChannelTbl.ALIAS + "." + ChannelTbl.POSITION + " as " + ChannelTbl.POSITION
                projectionMap[ChannelTbl.LOGO_URL] = ChannelTbl.ALIAS + "." + ChannelTbl.LOGO_URL + " as " + ChannelTbl.LOGO_URL
                projectionMap[ChannelTbl.FAV_POSITION] = ChannelTbl.ALIAS + "." + ChannelTbl.FAV_POSITION + " as " + ChannelTbl.FAV_POSITION
                projectionMap[ChannelTbl.NAME] = ChannelTbl.ALIAS + "." + ChannelTbl.NAME + " as " + ChannelTbl.NAME
                projectionMap[ChannelTbl.EPG_ID] = ChannelTbl.ALIAS + "." + ChannelTbl.EPG_ID + " as " + ChannelTbl.EPG_ID
                projectionMap[NowTbl.TITLE] = NowTbl.ALIAS + "." + NowTbl.TITLE + " as " + NowTbl.TITLE
                projectionMap[NowTbl.START] = NowTbl.ALIAS + "." + NowTbl.START + " as " + NowTbl.START
                projectionMap[NowTbl.PDC] = NowTbl.ALIAS + "." + NowTbl.PDC + " as " + NowTbl.PDC
                projectionMap[NowTbl.EVENT_ID] = NowTbl.ALIAS + "." + NowTbl.EVENT_ID + " as " + NowTbl.EVENT_ID
                projectionMap[NowTbl.END] = NowTbl.ALIAS + "." + NowTbl.END + " as " + NowTbl.END
                groupBy = ChannelTbl.ALIAS + "." + ChannelTbl._ID
                qb.projectionMap = projectionMap
            }
            EPG -> {
                qb = SQLiteQueryBuilder()
                qb.tables = EpgTbl.TABLE_NAME
            }
            GROUPS -> {
                qb = SQLiteQueryBuilder()
                qb.tables = GroupTbl.TABLE_NAME
            }
            MEDIAS -> {
                qb = SQLiteQueryBuilder()
                qb.tables = ProviderConsts.MediaTbl.TABLE_NAME
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        val db = dbHelper.readableDatabase
        val c = qb.query(db, null, selection, selectionArgs, groupBy, null, sortOrder)
        if (context != null) {
            c.setNotificationUri(context!!.contentResolver, uri)
        }
        return c
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
    override fun getType(uri: Uri): String? {
        // TODO Auto-generated method stub
        return null
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#bulkInsert(android.net.Uri, android.content.ContentValues[])
	 */
    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        // TODO Auto-generated method stub
        return super.bulkInsert(uri, values)
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // TODO Auto-generated method stub
        return null
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // TODO Auto-generated method stub
        return 0
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        // TODO Auto-generated method stub
        return 0
    }

    companion object {

        private val CHANNELS = 100

        private val FAVOURITES = 101

        private val NOW_PLAYING_CHANNELS = 102

        private val NOW_PLAYING_FAVS = 103

        private val EPG = 200

        private val GROUPS = 300

        private val MEDIAS = 400

        private val sUriMatcher = buildUriMatcher()

        /**
         * Build and return a [android.content.UriMatcher] that catches all [android.net.Uri]
         * variations supported by this [android.content.ContentProvider].
         *
         * @return the uri matcher�
         * @author RayBa
         * @date 07.04.2013
         */
        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            val authority = ProviderConsts.CONTENT_AUTHORITY
            matcher.addURI(authority, ChannelTbl.TABLE_NAME, CHANNELS)
            matcher.addURI(authority, ChannelTbl.NOW_PLAYING, NOW_PLAYING_CHANNELS)
            matcher.addURI(authority, ChannelTbl.NOW_PLAYING_FAVS, NOW_PLAYING_FAVS)
            matcher.addURI(authority, FavTbl.TABLE_NAME, FAVOURITES)
            matcher.addURI(authority, EpgTbl.TABLE_NAME, EPG)
            matcher.addURI(authority, GroupTbl.TABLE_NAME, GROUPS)
            matcher.addURI(authority, ProviderConsts.MediaTbl.TABLE_NAME, MEDIAS)
            return matcher
        }
    }

}
