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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.dvbviewer.controller.data.DbConsts.ChannelTbl;
import org.dvbviewer.controller.data.DbConsts.EpgTbl;
import org.dvbviewer.controller.data.DbConsts.GroupTbl;
import org.dvbviewer.controller.data.DbConsts.NowTbl;
import org.dvbviewer.controller.data.DbConsts.RootTbl;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.entities.EpgEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class DbHelper.
 * 
 * @author RayBa
 */
public class DbHelper extends SQLiteOpenHelper {

	private static final String	DATABASE_NAME		= "dvbviewercontroller.db";

	private static final int	DATABASE_VERSION	= 1;

	private Context				mContext;

	/**
	 * Instantiates a new dB helper.
	 * 
	 * @param context
	 *            the context
	 */
	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		createChannelTable(db);
		db.execSQL("CREATE TABLE " + EpgTbl.TABLE_NAME + "(" + EpgTbl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + EpgTbl.EPG_ID + " INTEGER," + EpgTbl.START + " INTEGER, " + EpgTbl.END + " INTEGER," + EpgTbl.TITLE + " TEXT," + EpgTbl.SUBTITLE + " TEXT," + EpgTbl.DESC + " TEXT);");
		db.execSQL("CREATE TABLE " + NowTbl.TABLE_NAME + "(" + NowTbl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + NowTbl.EPG_ID + " INTEGER," + NowTbl.START + " INTEGER, " + NowTbl.END + " INTEGER," + NowTbl.TITLE + " TEXT," + NowTbl.SUBTITLE + " TEXT," + NowTbl.DESC + " TEXT);");
		db.execSQL("CREATE TABLE " + RootTbl.TABLE_NAME + "(" + RootTbl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + RootTbl.NAME + " TEXT);");
		db.execSQL("CREATE TABLE " + GroupTbl.TABLE_NAME + "(" + GroupTbl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + GroupTbl.ROOT_ID + " INTEGER," + GroupTbl.NAME + " TEXT," + GroupTbl.TYPE + " INTEGER);");
	}

	public void createChannelTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + ChannelTbl.TABLE_NAME + "(" + ChannelTbl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + ChannelTbl.CHANNEL_ID + " INTEGER," + ChannelTbl.GROUP_ID + " INTEGER," + ChannelTbl.FAV_GROUP_ID + " INTEGER," + ChannelTbl.NAME + " TEXT," + ChannelTbl.POSITION + " INTEGER, " + ChannelTbl.FAV_POSITION + " INTEGER, " + ChannelTbl.FAV_ID + " INTEGER," + ChannelTbl.EPG_ID + " INTEGER," + ChannelTbl.LOGO_URL + " TEXT," + ChannelTbl.FLAGS + " INTEGER);");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(this.getClass().getSimpleName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
		switch (newVersion) {
		case 2:
			db.execSQL("ALTER TABLE " + ChannelTbl.TABLE_NAME + " ADD COLUMN " + ChannelTbl.LOGO_URL + " TEXT");
			break;
		case 3:
			List<Channel> result = new ArrayList<>();
			Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME, null);
			while (c.moveToNext()) {
				Channel channel = new Channel();
				channel.setChannelID(c.getLong(c.getColumnIndex(ChannelTbl._ID)));
				channel.setEpgID(c.getLong(c.getColumnIndex(ChannelTbl.EPG_ID)));
				channel.setName(c.getString(c.getColumnIndex(ChannelTbl.NAME)));
				channel.setPosition(c.getInt(c.getColumnIndex(ChannelTbl.POSITION)));
				channel.setFavPosition(c.getInt(c.getColumnIndex(ChannelTbl.FAV_POSITION)));
				channel.setFlags(c.getInt(c.getColumnIndex(ChannelTbl.FLAGS)));
				channel.setLogoUrl(c.getString(c.getColumnIndex(ChannelTbl.LOGO_URL)));
				result.add(channel);
			}
			c.close();
			db.execSQL("DROP TABLE " + ChannelTbl.TABLE_NAME);
			createChannelTable(db);
			db.beginTransaction();
			db.execSQL("DELETE FROM " + ChannelTbl.TABLE_NAME);
			try {
				for (Channel channel : result) {
					db.insert(ChannelTbl.TABLE_NAME, null, channel.toContentValues());
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			break;
		case 4:
			db.execSQL("ALTER TABLE " + ChannelTbl.TABLE_NAME + " ADD COLUMN " + ChannelTbl.GROUP_ID + " INTEGER");
			db.execSQL("ALTER TABLE " + ChannelTbl.TABLE_NAME + " ADD COLUMN " + ChannelTbl.FAV_GROUP_ID + " INTEGER");
			db.execSQL("CREATE TABLE IF NOT EXISTS " + RootTbl.TABLE_NAME + "(" + RootTbl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + RootTbl.NAME + " TEXT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS " + GroupTbl.TABLE_NAME + "(" + GroupTbl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + GroupTbl.ROOT_ID + " INTEGER," + GroupTbl.NAME + " TEXT," + GroupTbl.TYPE + " INTEGER);");
			break;

		default:
			// db.execSQL("DROP TABLE IF EXISTS " + ChannelTbl.TABLE_NAME);
			// db.execSQL("DROP TABLE IF EXISTS " + EpgTbl.TABLE_NAME);
			break;
		}
	}

	/**
	 * Save epg entries.
	 * 
	 * @param rootElements
	 *            the rootElements
	 */
	public List<ChannelRoot> saveChannelRoots(List<ChannelRoot> rootElements) {
		if (rootElements == null || rootElements.size() <= 0) {
			return rootElements;
		}
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.execSQL("DELETE FROM " + RootTbl.TABLE_NAME);
			db.execSQL("DELETE FROM " + GroupTbl.TABLE_NAME);
			db.execSQL("DELETE FROM " + ChannelTbl.TABLE_NAME);
			db.beginTransaction();

			for (ChannelRoot channelRoot : rootElements) {
				long rootId = db.insert(RootTbl.TABLE_NAME, null, channelRoot.toContentValues());
				for (ChannelGroup group : channelRoot.getGroups()) {
					group.setRootId(rootId);
					long groupId = db.insert(GroupTbl.TABLE_NAME, null, group.toContentValues());
					for (Channel chan : group.getChannels()) {
						chan.setGroupId(groupId);
						long id = db.insert(ChannelTbl.TABLE_NAME, null, chan.toContentValues());
						chan.setId(id);
					}
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
		mContext.getContentResolver().notifyChange(GroupTbl.CONTENT_URI, null);
		return rootElements;
	}

	/**
	 * Save now playing.
	 *
	 * @param epgEntries the epg entries
	 */
	public void saveNowPlaying(List<EpgEntry> epgEntries) {
		if (epgEntries == null || epgEntries.size() <= 0) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		db.execSQL("DELETE FROM " + NowTbl.TABLE_NAME);
		try {
			for (EpgEntry epgEntrie : epgEntries) {
				db.insert(NowTbl.TABLE_NAME, null, epgEntrie.toContentValues());
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
		mContext.getContentResolver().notifyChange(ChannelTbl.CONTENT_URI_NOW, null);
	}



    /**
	 * Save favs.
	 *
	 * @param groups the groups
	 */
	public void saveFavGroups(List<ChannelGroup> groups) {
		if (groups == null || groups.size() <= 0) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			for (ChannelGroup channelGroup : groups) {
				long groupId = db.insert(GroupTbl.TABLE_NAME, null, channelGroup.toContentValues());
				updateChannels(db, groupId, channelGroup.getChannels());
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	private void updateChannels(SQLiteDatabase db, long groupId, List<Channel> channels) {
		for (Channel channel : channels) {
			channel.setFavGroupId(groupId);
			db.update(ChannelTbl.TABLE_NAME, channel.toContentValues(), ChannelTbl._ID+"="+channel.getId(), null);
		}
	}
}
