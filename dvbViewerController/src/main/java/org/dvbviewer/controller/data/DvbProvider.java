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
package org.dvbviewer.controller.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.dvbviewer.controller.data.DbConsts.ChannelTbl;
import org.dvbviewer.controller.data.DbConsts.EpgTbl;
import org.dvbviewer.controller.data.DbConsts.FavTbl;
import org.dvbviewer.controller.data.DbConsts.GroupTbl;
import org.dvbviewer.controller.data.DbConsts.NowTbl;

import java.util.Date;
import java.util.HashMap;

/**
 * The Class DvbProvider.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class DvbProvider extends ContentProvider {

	private static DbHelper			dbHelper;

	private static final int		CHANNELS				= 100;
	
	private static final int		FAVOURITES				= 101;
	
	private static final int		NOW_PLAYING_CHANNELS	= 102;
	
	private static final int		NOW_PLAYING_FAVS		= 103;

	private static final int		EPG						= 200;
	
	private static final int		GROUPS					= 300;

	private static final int		MEDIAS					= 400;

	private static final UriMatcher	sUriMatcher				= buildUriMatcher();

	/**
	 * Build and return a {@link android.content.UriMatcher} that catches all {@link android.net.Uri}
	 * variations supported by this {@link android.content.ContentProvider}.
	 *
	 * @return the uri matcher�
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = DbConsts.CONTENT_AUTHORITY;
		matcher.addURI(authority, ChannelTbl.TABLE_NAME, CHANNELS);
		matcher.addURI(authority, ChannelTbl.NOW_PLAYING, NOW_PLAYING_CHANNELS);
		matcher.addURI(authority, ChannelTbl.NOW_PLAYING_FAVS, NOW_PLAYING_FAVS);
		matcher.addURI(authority, FavTbl.TABLE_NAME, FAVOURITES);
		matcher.addURI(authority, EpgTbl.TABLE_NAME, EPG);
		matcher.addURI(authority, GroupTbl.TABLE_NAME, GROUPS);
		matcher.addURI(authority, DbConsts.MediaTbl.TABLE_NAME, MEDIAS);
		return matcher;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		dbHelper = new DbHelper(getContext());
		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb;
		String groupBy = null;
		HashMap<String, String> projectionMap;
		switch (sUriMatcher.match(uri)) {
			case CHANNELS:
				qb = new SQLiteQueryBuilder();
				qb.setTables(ChannelTbl.TABLE_NAME);
				break;
			case FAVOURITES:
				qb = new SQLiteQueryBuilder();
				qb.setTables(ChannelTbl.TABLE_NAME);
				break;
			case NOW_PLAYING_CHANNELS:
				qb = new SQLiteQueryBuilder();
				qb.setTables(ChannelTbl.AS_ALIAS + " LEFT JOIN " + NowTbl.AS_ALIAS + " on (" + ChannelTbl.ALIAS + "." + ChannelTbl.EPG_ID + " = " + NowTbl.ALIAS + "." + NowTbl.EPG_ID + " AND " + NowTbl.ALIAS + "." + NowTbl.START + " < " + new Date().getTime() + " AND " + NowTbl.ALIAS + "." + NowTbl.END + " > " + new Date().getTime() + ")");
				projectionMap = new HashMap<>();
				projectionMap.put(ChannelTbl._ID, ChannelTbl.ALIAS + "." + ChannelTbl._ID + " as " + ChannelTbl._ID);
				projectionMap.put(ChannelTbl.CHANNEL_ID, ChannelTbl.ALIAS + "." + ChannelTbl.CHANNEL_ID + " as " + ChannelTbl.CHANNEL_ID);
				projectionMap.put(ChannelTbl.POSITION, ChannelTbl.ALIAS + "." + ChannelTbl.POSITION + " as " + ChannelTbl.POSITION);
				projectionMap.put(ChannelTbl.LOGO_URL, ChannelTbl.ALIAS + "." + ChannelTbl.LOGO_URL + " as " + ChannelTbl.LOGO_URL);
				projectionMap.put(ChannelTbl.FAV_POSITION, ChannelTbl.ALIAS + "." + ChannelTbl.FAV_POSITION + " as " + ChannelTbl.FAV_POSITION);
				projectionMap.put(ChannelTbl.NAME, ChannelTbl.ALIAS + "." + ChannelTbl.NAME + " as " + ChannelTbl.NAME);
				projectionMap.put(ChannelTbl.EPG_ID, ChannelTbl.ALIAS + "." + ChannelTbl.EPG_ID + " as " + ChannelTbl.EPG_ID);
				projectionMap.put(NowTbl.TITLE, NowTbl.ALIAS + "." + NowTbl.TITLE + " as " + NowTbl.TITLE);
				projectionMap.put(NowTbl.START, NowTbl.ALIAS + "." + NowTbl.START + " as " + NowTbl.START);
				projectionMap.put(NowTbl.PDC, NowTbl.ALIAS + "." + NowTbl.PDC + " as " + NowTbl.PDC);
				projectionMap.put(NowTbl.EVENT_ID, NowTbl.ALIAS + "." + NowTbl.EVENT_ID + " as " + NowTbl.EVENT_ID);
				projectionMap.put(NowTbl.END, NowTbl.ALIAS + "." + NowTbl.END + " as " + NowTbl.END);
				groupBy = ChannelTbl.ALIAS + "." + ChannelTbl._ID;
				qb.setProjectionMap(projectionMap);
				break;
			case EPG:
				qb = new SQLiteQueryBuilder();
				qb.setTables(EpgTbl.TABLE_NAME);
				break;
			case GROUPS:
				qb = new SQLiteQueryBuilder();
				qb.setTables(GroupTbl.TABLE_NAME);
				break;
			case MEDIAS:
				qb = new SQLiteQueryBuilder();
				qb.setTables(DbConsts.MediaTbl.TABLE_NAME);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = qb.query(db, null, selection, selectionArgs, groupBy, null, sortOrder);
		if (getContext() != null){
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return c;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(@NonNull Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#bulkInsert(android.net.Uri, android.content.ContentValues[])
	 */
	@Override
	public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
		// TODO Auto-generated method stub
		return super.bulkInsert(uri, values);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
