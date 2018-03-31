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
	public static String	REC_SERVICE_USER_NAME			= "";
	public static String	REC_SERVICE_MAC_ADDRESS			= "";
	public static int		REC_SERVICE_WOL_PORT			= 9;
	public static String	REC_SERVICE_PASSWORD			= "";

	public static final String	URL_CHANNELS					= "/api/getchannelsxml.html?logo=1";
	public static final String	URL_FAVS						= "/api/getchannelsxml.html?logo=1&favonly=1";
	public static final String	URL_VERSION						= "/api/version.html";
	public static final String	URL_STATUS						= "/api/status.html";
	public static final String	URL_STATUS2						= "/api/status2.html";
	public static final String	URL_EPG							= "/api/epg.html";
	public static final String	URL_TIMER_CREATE				= "/api/timeradd.html";
	public static final String	URL_TIMER_EDIT					= "/api/timeredit.html";
	public static final String	URL_TIMER_LIST					= "/api/timerlist.html?utf8=2";
	public static final String	URL_MEDIA_LIST					= "/api/mediafiles.html?thumbs=1";
	public static final String	URL_TIMER_DELETE				= "/api/timerdelete.html?id=";
	public static final String  URL_SEND_COMMAND                = "/api/dvbcommand.html?target={0}&cmd=-x{1}";
	public static final String	URL_SWITCH_COMMAND				= "/api/dvbcommand.html?target={0}&cmd=-c{1}";
	public static final String	URL_FLASHSTREAM					= "flashstream/stream";
	public static final String	URL_M3U8						= "master.m3u8";
	public static final String	URL_TARGETS					    = "/api/dvbcommand.html";
	public static final String 	URL_FFMPEGPREFS					= "/api/getconfigfile.html?file=config%5Cffmpegprefs.ini";
	public static final String 	URL_IPHONE_FFMPEGPREFS			= "/api/getconfigfile.html?file=config%5Ciphoneprefs.ini";
	public static final String 	URL_RECORIDNGS 					= "/api/recordings.html?utf8=1&images=1";
	public static final String 	THUMBNAILS_VIDEO_URL 			= "/thumbnails/video/";

}