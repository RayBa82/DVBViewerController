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

	/** The Constant CONTENT_AUTHORITY. */
	public static final String	CONTENT_AUTHORITY	= "org.dvbviewer.controller.provider";

	/** The Constant BASE_CONTENT_URI. */
	private static final Uri	BASE_CONTENT_URI	= Uri.parse("content://" + CONTENT_AUTHORITY);

	/**
	 * The Class RootTbl.
	 *
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public static class RootTbl implements BaseColumns {

		/** The table name. */
		public static final String	TABLE_NAME	= "channel_root";

		/** The name. */
		public static final String	NAME		= "name";

		public static final Uri	CONTENT_URI	= BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

	}

	/**
	 * The Class GroupTbl.
	 *
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public static class GroupTbl implements BaseColumns {

		/** The table name. */
		public static final String	TABLE_NAME	= "channel_group";

		/** The root id. */
		public static final String	ROOT_ID		= "root_id";
		
		/** The name. */
		public static final String	NAME		= "name";

		/** The type. */
		public static final String	TYPE		= "type";

		/** The Constant CONTENT_URI. */
		public static final Uri	CONTENT_URI	= BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
	}

	/**
	 * The Class ChannelTbl.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class ChannelTbl implements BaseColumns {

		/** The table name. */
		public static final String	TABLE_NAME				= "channel";
		
		public static final String	CHANNEL_ID				= "channel_id";

		/** The name. */
		public static final String	NAME					= "name";

		/** The position. */
		public static final String	POSITION				= "position";

		/** The fav position. */
		public static final String	FAV_POSITION			= "fav_position";

		/** The epg id. */
		public static final String	EPG_ID					= "epg_id";

		/** The fav id. */
		public static final String	FAV_ID					= "fav_id";

		/** The flags. */
		public static final String	FLAGS					= "flags";

		/** The logo url. */
		public static final String	LOGO_URL				= "logo_url";

		/** The group id. */
		public static final String	GROUP_ID				= "group_id";

		/** The fav group id. */
		public static final String	FAV_GROUP_ID			= "fav_group_id";

		/** The now playing. */
		public static final String	NOW_PLAYING				= ".now";

		/** The now playing favs. */
		public static final String	NOW_PLAYING_FAVS		= ".nowFavs";

		/** The alias. */
		public static final String	ALIAS					= "chans";

		/** The as alias. */
		public static final String	AS_ALIAS				= TABLE_NAME + " as " + ALIAS;

		/** The Constant CONTENT_URI. */
		public static final Uri	CONTENT_URI				= BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

		/** The Constant CONTENT_URI_NOW. */
		public static final Uri	CONTENT_URI_NOW			= BASE_CONTENT_URI.buildUpon().appendPath(NOW_PLAYING).build();

	}

	/**
	 * The Class FavTbl.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class FavTbl extends ChannelTbl {

		/** The table name. */
		public static final String	TABLE_NAME	= "favs";

		/** The Constant CONTENT_URI. */
		public static final Uri	CONTENT_URI	= BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
	}

	/**
	 * The Class EpgTbl.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class EpgTbl implements BaseColumns {

		/** The table name. */
		public static final String	TABLE_NAME	= "epg";

		/** The epg id. */
		public static final String	EPG_ID		= "epg_id";

		/** The start. */
		public static final String	START		= "start";

		/** The end. */
		public static final String	END			= "end";

		/** The title. */
		public static final String	TITLE		= "title";

		/** The subtitle. */
		public static final String	SUBTITLE	= "subtitle";

		/** The desc. */
		public static final String	DESC		= "desc";

		public static final String	EVENT_ID	= "event_id";

		public static final String	PDC			= "pdc";

	}

	/**
	 * The Class NowTbl.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class NowTbl implements BaseColumns {

		/** The table name. */
		public static final String	TABLE_NAME	= "now";

		/** The epg id. */
		public static final String	EPG_ID		= EpgTbl.EPG_ID;

		/** The start. */
		public static final String	START		= EpgTbl.START;

		/** The end. */
		public static final String	END			= EpgTbl.END;

		/** The title. */
		public static final String	TITLE		= EpgTbl.TITLE;

		/** The subtitle. */
		public static final String	SUBTITLE	= EpgTbl.SUBTITLE;

		/** The desc. */
		public static final String	DESC		= EpgTbl.DESC;

		public static final String	EVENT_ID	= EpgTbl.EVENT_ID;

		public static final String	PDC		= EpgTbl.PDC;

		/** The alias. */
		public static final String	ALIAS		= "nowAlias";

		/** The as alias. */
		public static final String	AS_ALIAS	= TABLE_NAME + " as " + ALIAS;

	}

}
