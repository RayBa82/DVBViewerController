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
	public static String    URL_SEND_COMMAND                = "/api/dvbcommand.html?target={0}&cmd=-x{1}";
	public static String	URL_SWITCH_COMMAND				= "/api/dvbcommand.html?target={0}&cmd=-c{1}";
	public static String	URL_DELETE_RECORDING			= "/rec_listnew.html?aktion=delete_rec&recid=";
	public static String	URL_FLASHSTREAM					= "/flashstream/stream.ts?";
	public static String	URL_TARGETS					    = "/api/dvbcommand.html";
	public static String 	URL_FFMPEGPREFS					= "/api/getconfigfile.html?file=config%5Cffmpegprefs.ini";

}