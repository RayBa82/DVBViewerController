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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The Class DbConsts.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class DbConsts {

	public static final String	CONTENT_AUTHORITY	= "org.dvbviewer.controller.provider";
	
	private static final Uri	BASE_CONTENT_URI	= Uri.parse("content://" + CONTENT_AUTHORITY);

	/**
	 * The Class ChannelTbl.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class ChannelTbl implements BaseColumns {

		public static String	TABLE_NAME				= "channel";

		public static String	CHANNEL_ID				= "channel_id";
		
		public static String	NAME					= "name";
		
		public static String	POSITION				= "position";
		
		public static String	FAV_POSITION			= "fav_position";

		public static String	EPG_ID					= "epg_id";
		
		public static String	FAV_ID					= "fav_id";
		
		public static String	FLAGS					= "flags";
		
		public static String	LOGO_URL				= "logo_url";

		public static String	NOW_PLAYING				= ".now";
		
		public static String	NOW_PLAYING_FAVS		= ".nowFavs";
		
		public static String	ALIAS					= "chans";
		
		public static String	AS_ALIAS				= TABLE_NAME + " as " + ALIAS;

		public static final Uri	CONTENT_URI				= BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
		
		public static final Uri	CONTENT_URI_NOW			= BASE_CONTENT_URI.buildUpon().appendPath(NOW_PLAYING).build();
		
		public static final Uri	CONTENT_URI_NOW_FAVS	= BASE_CONTENT_URI.buildUpon().appendPath(NOW_PLAYING_FAVS).build();
	}

	/**
	 * The Class FavTbl.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class FavTbl extends ChannelTbl {

		public static String	TABLE_NAME	= "favs";

		public static final Uri	CONTENT_URI	= BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
	}

	/**
	 * The Class EpgTbl.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class EpgTbl implements BaseColumns {

		public static String	TABLE_NAME	= "epg";
		
		public static String	EPG_ID		= "epg_id";
		
		public static String	START		= "start";
		
		public static String	END			= "end";
		
		public static String	TITLE		= "title";

		public static String	SUBTITLE	= "subtitle";
		
		public static String	DESC		= "desc";

		public static String	ALIAS		= "epgalias";
		
		public static String	AS_ALIAS	= TABLE_NAME + " as " + ALIAS;

		public static final Uri	CONTENT_URI	= BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

	}
	
	/**
	 * The Class NowTbl.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class NowTbl implements BaseColumns {
		
		public static String	TABLE_NAME	= "now";
		
		public static String	EPG_ID		= EpgTbl.EPG_ID;
		
		public static String	START		= EpgTbl.START;
		
		public static String	END			= EpgTbl.END;
		
		public static String	TITLE		= EpgTbl.TITLE;
		
		public static String	SUBTITLE	= EpgTbl.SUBTITLE;
		
		public static String	DESC		= EpgTbl.DESC;
		
		public static String	ALIAS		= "nowAlias";
		
		public static String	AS_ALIAS	= TABLE_NAME + " as " + ALIAS;
		
		public static final Uri	CONTENT_URI	= BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
		
	}

	/**
	 * The Class SqlSynatx.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class SqlSynatx {

		public static String	SMALLER_OR_EQUALS	= " <= ";
		
		public static String	BIGGER_OR_EQUALS	= " >= ";
		
		public static String	EQUALS				= " = ";
		
		public static String	AND					= " AND ";
		
		public static String	BETWEEN				= " BETWEEN ";
		
		public static String	WHERE				= " WHERE ";
		
		public static String	ORDER_BY			= " ORDER BY ";

	}

}
