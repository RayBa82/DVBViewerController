package org.dvbviewer.controller.utils;

/**
 * The Class ServerConsts.
 * 
 * @author RayBa
 * @date 13.04.2012
 */
public class ServerConsts {

	public static String	REC_SERVICE_PROTOCOL			= "http://";
	public static String	REC_SERVICE_HOST				= "";
	public static String	REC_SERVICE_PORT				= "8089";
	public static String	REC_SERVICE_URL					= REC_SERVICE_PROTOCOL;
	public static String	REC_SERVICE_LIVE_STREAM_PORT	= "7522";
	public static String	REC_SERVICE_MEDIA_STREAM_PORT	= "8090";
	public static String	REC_SERVICE_USER_NAME			= "";
	public static String	REC_SERVICE_MAC_ADDRESS			= "";
	public static int		REC_SERVICE_WOL_PORT			= 9;
	public static String	REC_SERVICE_PASSWORD			= "";
	public static String	DVBVIEWER_PROTOCOL				= "http://";
	public static String	DVBVIEWER_HOST					= "";
	public static String	DVBVIEWER_PORT					= "80";
	public static String	DVBVIEWER_URL					= DVBVIEWER_PROTOCOL;
	public static String	DVBVIEWER_USER_NAME				= "";
	public static String	DVBVIEWER_PASSWORD				= "";
	public static String	DVBVIEWER_MAC_ADDRESS			= "";
	public static String	HTTP							= "http";
	public static String	URL_CHANNELS					= "/api/getchannelsxml.html?logo=1&subchannels=1";
	public static String	URL_CHANNELS_OLD				= "/api/getchannelsdat.html";
	public static String	URL_FAVS						= "/api/getfavourites.html";
	public static String	URL_VERSION						= "/api/version.html";
	public static String	URL_STATUS						= "/api/status.html";
	public static String	URL_CHANNEL_EPG					= "/api/epg.html?lvl=2&channel=";
	public static String	URL_EPG							= "/api/epg.html?lvl=2";
	public static String	URL_TIMER_CREATE				= "/api/timeradd.html?";
	public static String	URL_TIMER_EDIT					= "/api/timeredit.html?";
	public static String	URL_TIMER_DELETE				= "/api/timerdelete.html?id=";
	public static String	URL_EXECUTE_TASK				= "/tasks.html?aktion=tasks&task=";
	public static String	URL_SEND_COMMAND				= "/fb.html?command=";
	public static String	URL_SWITCH_COMMAND				= "/fb.html?swcommand=";
	public static String	URL_DELETE_RECORDING			= "/rec_listnew.html?aktion=delete_rec&recid=";
	public static String	URL_FLASHSTREAM					= "/flashstream/stream.flv?";

}