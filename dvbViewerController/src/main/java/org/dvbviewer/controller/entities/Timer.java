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
package org.dvbviewer.controller.entities;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * The Class Timer.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class Timer implements Comparable<Timer> {

	public static final int		FLAG_EXECUTABLE			= 1 << 0;					// 0x01
	public static final int		FLAG_RECORDING			= 1 << 1;					// 0x02
	public static final int		FLAG_DISABLED			= 1 << 2;					// 0x02

	private long				id = -1;
	private long				channelId;
	private String				channelName;
	private String				title;
	private Date				start;
	private Date				end;
	private int					timerAction;
	private int					pre;
	private int					post;
	private String				eventId;
	private String				pdc;
	private int					adjustPAT           = -1;
	private int					allAudio            = -1;
	private int					dvbSubs             = -1;
	private int					teletext            = -1;
	private int					eitEPG              = -1;
	private int					monitorPDC          = -1;
	private int                 runningStatusSplit  = -1;
	private int					flags               = FLAG_EXECUTABLE;

	/**
	 * Gets the title.
	 *
	 * @return the title
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 *
	 * @param description the new title
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setTitle(String description) {
		this.title = description;
	}

	/**
	 * Gets the start.
	 *
	 * @return the start
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * Sets the start.
	 *
	 * @param start the new start
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setStart(Date start) {
		this.start = start;
	}

	/**
	 * Gets the end.
	 *
	 * @return the end
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Date getEnd() {
		return end;
	}

	/**
	 * Sets the end.
	 *
	 * @param end the new end
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setEnd(Date end) {
		this.end = end;
	}

	/**
	 * Gets the channel id.
	 *
	 * @return the channel id
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public long getChannelId() {
		return channelId;
	}

	/**
	 * Sets the channel id.
	 *
	 * @param channelId the new channel id
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets the timer action.
	 *
	 * @return the timer action
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public int getTimerAction() {
		return timerAction;
	}

	/**
	 * Sets the timer action.
	 *
	 * @param timerAction the new timer action
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setTimerAction(int timerAction) {
		this.timerAction = timerAction;
	}

	/**
	 * Gets the channel name.
	 *
	 * @return the channel name
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public String getChannelName() {
		return channelName;
	}

	/**
	 * Sets the channel name.
	 *
	 * @param channelName the new channel name
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public int getPost() {
		return post;
	}

	public void setPost(int post) {
		this.post = post;
	}

	public int getPre() {
		return pre;
	}

	public void setPre(int pre) {
		this.pre = pre;
	}

	public String getPdc() {
		return pdc;
	}

	public void setPdc(String pdc) {
		this.pdc = pdc;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public int getAdjustPAT() {
		return adjustPAT;
	}

	public void setAdjustPAT(int adjustPAT) {
		this.adjustPAT = adjustPAT;
	}

	public int getAllAudio() {
		return allAudio;
	}

	public void setAllAudio(int allAudio) {
		this.allAudio = allAudio;
	}

	public int getDvbSubs() {
		return dvbSubs;
	}

	public void setDvbSubs(int dvbSubs) {
		this.dvbSubs = dvbSubs;
	}

	public int getTeletext() {
		return teletext;
	}

	public void setTeletext(int teletext) {
		this.teletext = teletext;
	}

	public int getEitEPG() {
		return eitEPG;
	}

	public void setEitEPG(int eitEPG) {
		this.eitEPG = eitEPG;
	}

	public int getMonitorPDC() {
		return monitorPDC;
	}

	public void setMonitorPDC(int monitorPDC) {
		this.monitorPDC = monitorPDC;
	}

	public int getRunningStatusSplit() {
		return runningStatusSplit;
	}

	public void setRunningStatusSplit(int runningStatusSplit) {
		this.runningStatusSplit = runningStatusSplit;
	}


	/**
	 * Gets the flags.
	 *
	 * @return the flags
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public int getFlags() {
		return flags;
	}
	
	/**
	 * Sets the flag.
	 *
	 * @param flag the new flag
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setFlag(int flag){
		this.flags |= flag;
	}
	
	/**
	 * Unset flag.
	 *
	 * @param flag the flag
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void unsetFlag(int flag){
		this.flags &= ~flag;
	}
	
	/**
	 * Checks if is flag set.
	 *
	 * @param flag the flag
	 * @return true, if is flag set
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public boolean isFlagSet(int flag){
		return (flag & flags) != 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(@NonNull Timer comparator) {
		return this.start.compareTo(comparator.getStart());
	}


}
