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
package org.dvbviewer.controller.utils;

/**
 * The Class ActionID.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class ActionID {



	/** The Constant CMD_MOVE_RIGHT. */
	public static final String CMD_MOVE_RIGHT 		= 		"2100";

	/** The Constant CMD_MOVE_LEFT. */
	public static final String CMD_MOVE_LEFT 		= 		"2000";

	/** The Constant CMD_MOVE_UP. */
	public static final String CMD_MOVE_UP 			= 		"78";

	/** The Constant CMD_MOVE_DOWN. */
	public static final String CMD_MOVE_DOWN 		= 		"79";

	/** The Constant CMD_SELECT_ITEM. */
	public static final String CMD_SELECT_ITEM 		= 		"73";

	/** The Constant CMD_SHOW_VIDEO. */
	public static final String CMD_SHOW_VIDEO		= 		"8204";

	/** The Constant CMD_PREVIOUS_MENU. */
	public static final String CMD_PREVIOUS_MENU	= 		"84";

	/** The Constant CMD_SHOW_OSD. */
	public static final String CMD_SHOW_OSD			= 		"111";

	/** The Constant CMD_STEP_FORWARD. */
	public static final String CMD_STEP_FORWARD		= 		"103";

	/** The Constant CMD_STEP_BACK. */
	public static final String CMD_STEP_BACK		= 		"102";

	/** The Constant CMD_STOP. */
	public static final String CMD_STOP		  		= 		"114";

	/** The Constant CMD_PAUSE. */
	public static final String CMD_PAUSE	  		= 		"0";

	/** The Constant CMD_RED. */
	public static final String CMD_RED	  			= 		Integer.valueOf(0x0000004A).toString();

	/** The Constant CMD_GREEN. */
	public static final String CMD_GREEN	  		= 		"75";

	/** The Constant CMD_YELLOW. */
	public static final String CMD_YELLOW			= 		"76";

	/** The Constant CMD_BLUE. */
	public static final String CMD_BLUE				= 		Integer.valueOf(0x0000004D).toString();

	/** The Constant CMD_SHOW_TELETEXT. */
	public static final String CMD_SHOW_TELETEXT	= 		Integer.valueOf(0x00000065).toString();

	/** The Constant CMD_REMOTE_0. */
	public static final String CMD_REMOTE_0 		= 		"40";

	/** The Constant CMD_REMOTE_1. */
	public static final String CMD_REMOTE_1 		= 		"41";

	/** The Constant CMD_REMOTE_2. */
	public static final String CMD_REMOTE_2 		= 		"42";

	/** The Constant CMD_REMOTE_3. */
	public static final String CMD_REMOTE_3 		=	 	"43";

	/** The Constant CMD_REMOTE_4. */
	public static final String CMD_REMOTE_4 		= 		"44";

	/** The Constant CMD_REMOTE_5. */
	public static final String CMD_REMOTE_5 		= 		"45";

	/** The Constant CMD_REMOTE_6. */
	public static final String CMD_REMOTE_6 		= 		"46";

	/** The Constant CMD_REMOTE_7. */
	public static final String CMD_REMOTE_7 		= 		"47";

	/** The Constant CMD_REMOTE_8. */
	public static final String CMD_REMOTE_8 		= 		"48";

	/** The Constant CMD_REMOTE_9. */
	public static final String CMD_REMOTE_9 		= 		"49";

    /** The Constant CMD_REMOTE_0. */
    public static final String CMD_FAV_0 		    = 		"";

    /** The Constant CMD_REMOTE_1. */
    public static final String CMD_FAV_1 		= 		"11";

    /** The Constant CMD_REMOTE_2. */
    public static final String CMD_FAV_2 		= 		"12";

    /** The Constant CMD_REMOTE_3. */
    public static final String CMD_FAV_3 		=	 	"13";

    /** The Constant CMD_REMOTE_4. */
    public static final String CMD_FAV_4 		= 		"14";

    /** The Constant CMD_REMOTE_5. */
    public static final String CMD_FAV_5 		= 		"15";

    /** The Constant CMD_REMOTE_6. */
    public static final String CMD_FAV_6 		= 		"16";

    /** The Constant CMD_REMOTE_7. */
    public static final String CMD_FAV_7 		= 		"17";

    /** The Constant CMD_REMOTE_8. */
    public static final String CMD_FAV_8 		= 		"18";

    /** The Constant CMD_REMOTE_9. */
    public static final String CMD_FAV_9 		= 		"19";

	/** The Constant CMD_EXIT. */
	public static final String CMD_EXIT  			= 		Integer.valueOf(0x00003006).toString();

	/** The Constant CMD_SHOW_RECORDINGS. */
	public static final String CMD_SHOW_RECORDINGS  = 		Integer.valueOf(0x00002004).toString();

//	CMD_PAGE_UP: 0x00000052; 
//	CMD_PAGE_DOWN: 0x00000053; 
//	CMD_FORWARD: 0x00003010 
//	CMD_REWIND: 0x00003011 
//	CMD_RECORD: 0x00000022 
//	CMD_SELECT: 0x00000049 
//	CMD_RED: 0x0000004A 
//	CMD_BLUE: 0x0000004D 
//	CMD_PLAY: 0x0000005C 
//	CMD_PREV_SONG: 0x00000070; 
//	CMD_NEXT_SONG: 0x00000071; 
//	CMD_SHOW_CLOCK: 0x000007DA; 
//	CMD_SWITCH_BACKGROUND: 0x00002002; 
//	
//	CMD_REBOOT: 0x00003007; 
//	CMD_SHUTDOWN: 0x00003008; 
//	CMD_STANDBY: 0x00003024; 
//	CMD_HIBERNATE: 0x00003023; 
//	CMD_POWEROFF: 0x00003025; 
//	CMD_BACKGROUND_TOGGLE: 0x00003009; 
//	CMD_EJECTCD: 0x0000300B; 
//	CMD_SPEED_UP: 0x0000305E; 
//	CMD_SPEED_DOWN: 0x0000305F; 
//	CMD_SHOW_PLAYLIST: 0x00003060; 
//	CMD_SHOW_HTPC: 0x0000083E; 
//	CMD_SHOW_TIMER: 0x00002003; 
//	CMD_SHOW_RECORDINGS: 0x00002004; 
//	CMD_SHOW_NOW: 0x00002005; 
//	CMD_SHOW_EPG: 0x00002006; 
//	CMD_SHOW_CHANNELS: 0x00002007; 
//	CMD_SHOW_FAVOURITES: 0x00002008; 
//	CMD_SHOW_TIMELINE: 0x00002009; 
//	CMD_SHOW_PICTURES: 0x0000200A; 
//	CMD_SHOW_MUSIC: 0x0000200B; 
//	CMD_SHOW_NEWS: 0x0000200D; 
//	CMD_SHOW_WEATHER: 0x0000200E 
//	CMD_SHOW_MINIEPG: 0x0000200F 
//	CMD_SHOW_MUSIC_PLAYLIST: 0x00002010 
//	CMD_SHOW_VIDEO_PLAYLIST: 0x00002011 
//	CMD_DISABLE_AUDIO: 0x00004001 
//	CMD_DISABLE_AUDIOVIDEO: 0x00004002 
//	CMD_DISABLE_VIDEO: 0x00004003 
//	CMD_ENABLE_AUDIOVIDEO: 0x00004004 
//	CMD_SHOW_VERSION: 0x00004000 
//	CMD_MINIMIZE: 0x00003FFE 
//	CMD_STOP_VIDEO: 0x00003FFF 
//	CMD_ZOOMLEVEL_STANDARD: 0x00004005 
//	CMD_ZOOMLEVEL_0: 0x00004006 
//	CMD_ZOOMLEVEL_1: 0x00004007 
//	CMD_ZOOMLEVEL_2: 0x00004008 
//	CMD_ZOOMLEVEL_3: 0x00004009 
//	CMD_ZOOMLEVEL_TOGGLE: 0x0000400A 
//	CMD_PREVIEW_TOGGLE: 0x0000400B 
//	CMD_RESTORE_DEFAULTCOLORS: 0x0000400C 
//	CMD_SHOW_HELP: 0x00002015 
//	CMD_ADD_BOOKMARK: 0x00003012 
//	CMD_SHOW_COMPUTER: 0x00002012 
//	CMD_TOGGLE_MOSAICPREVIEW: 0x00002013 
//	CMD_SHOW_ALARMS: 0x00002014 
//	CMD_VIDEOWINDOW_SHOW: 0x00000335 
//	CMD_VIDEOWINDOW_HIDE: 0x00002016 
//	CMD_OSD_GOHOME: 0x00002017 
//	CMD_OSD_SHOWHOME: 0x00002018; 

}
