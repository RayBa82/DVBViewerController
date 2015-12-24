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
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import org.dvbviewer.controller.data.DbConsts.ChannelTbl;
import org.dvbviewer.controller.data.DbConsts.EpgTbl;
import org.dvbviewer.controller.data.DbConsts.GroupTbl;
import org.dvbviewer.controller.data.DbConsts.NowTbl;
import org.dvbviewer.controller.data.DbConsts.RootTbl;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.Channel.Fav;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.entities.EpgEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class DbHelper.
 * 
 * @author RayBa
 * @date 26.04.2012
 */
public class DbHelper extends SQLiteOpenHelper {

	private static final String	DATABASE_NAME		= "dvbviewercontroller.db";

	private static final int	DATABASE_VERSION	= 1;

	CursorFactory				mCursorFactory;

	public static String		SMALLER_OR_EQUALS	= " <= ";

	public static String		BIGGER_OR_EQUALS	= " >= ";

	public static String		EQUALS				= " = ";

	public static String		NOT_EQUALS			= " != ";

	public static String		AND					= " AND ";

	public static String		BETWEEN				= " BETWEEN ";

	public static String		WHERE				= " WHERE ";

	public static String		ORDER_BY			= " ORDER BY ";

	public static String		BIT_AND				= " & ";

	private Context				mContext;

	/**
	 * Instantiates a new dB helper.
	 * 
	 * @param context
	 *            the context
	 * @author RayBa
	 * @date 26.04.2012
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
			List<Channel> result = new ArrayList<Channel>();
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
	 * Gets the channellist.
	 * 
	 * @return the channellist
	 * 
	 * @author RayBa
	 * @date 20.06.2010
	 * @description Gets the channellist.
	 */
	public List<Channel> loadChannellist() {
		SQLiteDatabase db = getWritableDatabase();
		List<Channel> result = new ArrayList<Channel>();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + " ORDER BY " + ChannelTbl.POSITION, null);
		while (c.moveToNext()) {
			Channel channel = new Channel();
			channel.setId(c.getLong(c.getColumnIndex(ChannelTbl._ID)));
			channel.setEpgID(c.getLong(c.getColumnIndex(ChannelTbl.EPG_ID)));
			String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
			name = name.replaceAll("\\([^\\(]*\\)", "").trim();
			channel.setName(name);
			channel.setPosition(c.getInt(c.getColumnIndex(ChannelTbl.POSITION)));
			result.add(channel);
		}
		c.close();
		db.close();
		if (result.size() <= 0) {
			result = null;
		}
		return result;
	}

	/**
	 * Gets the channellist.
	 * 
	 * @return the channellist
	 * 
	 * @author RayBa
	 * @date 20.06.2010
	 * @description Gets the channellist.
	 */
	public List<Channel> loadPendingUpdateChannellist() {
		SQLiteDatabase db = getWritableDatabase();
		List<Channel> result = new ArrayList<Channel>();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + WHERE + ChannelTbl.FLAGS + BIT_AND + Channel.FLAG_PENDING_UPDATE + NOT_EQUALS + "0" + ORDER_BY + ChannelTbl.POSITION, null);
		while (c.moveToNext()) {
			Channel channel = new Channel();
			channel.setId(c.getLong(c.getColumnIndex(ChannelTbl._ID)));
			channel.setEpgID(c.getLong(c.getColumnIndex(ChannelTbl.EPG_ID)));
			String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
			name = name.replaceAll("\\([^\\(]*\\)", "").trim();
			channel.setName(name);
			channel.setPosition(c.getInt(c.getColumnIndex(ChannelTbl.POSITION)));
			result.add(channel);
		}
		c.close();
		db.close();
		if (result.size() <= 0) {
			result = null;
		}
		return result;
	}

	/**
	 * Gets the channel cursor.
	 *
	 * @return the channel cursor
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Cursor getChannelCursor() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + " ORDER BY " + ChannelTbl.POSITION, null);
		return c;
	}

	/**
	 * Gets the fav cursor.
	 *
	 * @return the fav cursor
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Cursor getFavCursor() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + " WHERE " + ChannelTbl.FLAGS + " & " + Channel.FLAG_FAV + "!= 0 ORDER BY " + ChannelTbl.FAV_POSITION, null);
		return c;
	}

	/**
	 * Gets the channellist.
	 *
	 * @param epgId the epg id
	 * @param start the start
	 * @param stop the stop
	 * @return the channellist
	 * @author RayBa
	 * @date 20.06.2010
	 * @description Gets the channellist.
	 */
	public List<EpgEntry> loadChannelEPG(long epgId, long start, long stop) {
		SQLiteDatabase db = getWritableDatabase();
		List<EpgEntry> result = new ArrayList<EpgEntry>();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(EpgTbl.TABLE_NAME);
		Cursor c = qb.query(db, null, EpgTbl.EPG_ID + EQUALS + epgId + AND + EpgTbl.END + BETWEEN + start + AND + stop, null, null, null, EpgTbl.END);
		while (c.moveToNext()) {
			EpgEntry epg = new EpgEntry();
			epg.setId(c.getLong(c.getColumnIndex(EpgTbl._ID)));
			epg.setEpgID(c.getLong(c.getColumnIndex(EpgTbl.EPG_ID)));
			epg.setStart(new Date((c.getLong(c.getColumnIndex(EpgTbl.START)))));
			epg.setEnd(new Date((c.getLong(c.getColumnIndex(EpgTbl.END)))));
			epg.setTitle(c.getString(c.getColumnIndex(EpgTbl.TITLE)));
			epg.setSubTitle(c.getString(c.getColumnIndex(EpgTbl.SUBTITLE)));
			epg.setDescription(c.getString(c.getColumnIndex(EpgTbl.DESC)));
			result.add(epg);
		}
		c.close();
		db.close();
		if (result.size() <= 0) {
			result = null;
		}
		return result;
	}

	/**
	 * Gets the channellist.
	 * 
	 * @return the channellist
	 * 
	 * @author RayBa
	 * @date 20.06.2010
	 * @description Gets the channellist.
	 */
	public List<Channel> loadFavourites() {
		SQLiteDatabase db = getWritableDatabase();
		List<Channel> result = new ArrayList<Channel>();
		Cursor c = db.rawQuery("SELECT * FROM " + ChannelTbl.TABLE_NAME + " WHERE " + ChannelTbl.FLAGS + " & " + Channel.FLAG_FAV + "!= 0 ORDER BY " + ChannelTbl.FAV_POSITION, null);
		while (c.moveToNext()) {
			Channel channel = new Channel();
			channel.setId(c.getLong(c.getColumnIndex(ChannelTbl._ID)));
			channel.setEpgID(c.getLong(c.getColumnIndex(ChannelTbl.EPG_ID)));
			String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
			name = name.replaceAll("\\([^\\(]*\\)", "").trim();
			channel.setName(name);
			channel.setPosition(c.getInt(c.getColumnIndex(ChannelTbl.FAV_POSITION)));
			result.add(channel);
		}
		c.close();
		db.close();
		if (result.size() <= 0) {
			result = null;
		}
		return result;
	}

	/**
	 * Save epg entries.
	 * 
	 * @param rootElements
	 *            the rootElements
	 * @author RayBa
	 * @date 26.04.2012
	 */
	public void saveChannelRoots(List<ChannelRoot> rootElements) {
		if (rootElements == null || rootElements.size() <= 0) {
			return;
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
						db.insert(ChannelTbl.TABLE_NAME, null, chan.toContentValues());
					}
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
		mContext.getContentResolver().notifyChange(GroupTbl.CONTENT_URI, null);
	}

	public void saveChannels(List<Channel> channels) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		db.execSQL("DELETE FROM " + ChannelTbl.TABLE_NAME);
		try {
			for (Channel channel : channels) {
				db.insert(ChannelTbl.TABLE_NAME, null, channel.toContentValues());
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * Save epg.
	 *
	 * @param epgEntries the epg entries
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void saveEPG(List<EpgEntry> epgEntries) {
		if (epgEntries == null || epgEntries.size() <= 0) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		// db.execSQL("DELETE FROM " + ChannelTbl.TABLE_NAME);
		try {
			for (EpgEntry epgEntrie : epgEntries) {
				db.insert(EpgTbl.TABLE_NAME, null, epgEntrie.toContentValues());
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * Save now playing.
	 *
	 * @param epgEntries the epg entries
	 * @author RayBa
	 * @date 07.04.2013
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
	 * @param favs the favs
	 * @author RayBa
	 * @date 26.04.2012
	 */
	public void saveFavs(List<Fav> favs) {
		if (favs == null || favs.size() <= 0) {
			return;
		}
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
            for (Fav fav : favs) {
                saveFav(db, fav);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

    /**
     *
     * @param db
     * @param fav
     * @author RayBa
     * @date 23.01.2015
     */
    private void saveFav(SQLiteDatabase db, Fav fav) {
        String subSelect = "Select "+ ChannelTbl.CHANNEL_ID+" from " + ChannelTbl.TABLE_NAME + " where " + ChannelTbl.CHANNEL_ID + " = '" + fav.id + "' LIMIT 1";
        String select = "update " + ChannelTbl.TABLE_NAME + " set " + ChannelTbl.FAV_POSITION + " = " + fav.position + ", " + ChannelTbl.FLAGS + " = " + ChannelTbl.FLAGS + " | " + Channel.FLAG_FAV + " where "+ ChannelTbl.CHANNEL_ID+" in ("+subSelect+");";
        db.execSQL(select);
    }

    /**
	 * Save favs.
	 *
	 * @param groups the groups
	 * @author RayBa
	 * @date 26.04.2012
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
				for (Fav fav : channelGroup.getFavs()) {
                    saveFav(db,fav);
                }
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
	}
}
