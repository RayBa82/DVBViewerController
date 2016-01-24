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

import android.os.Parcelable;

import java.util.Date;

/**
 * Interface for EPG entries.
 *
 * @author RayBa
 */
public interface IEPG extends Parcelable {

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	long getId();

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 */
	void setId(long id);

	/**
	 * Gets the channel.
	 *
	 * @return the channel
	 */
	String getChannel();

	/**
	 * Sets the channel.
	 *
	 * @param channel the channel to set
	 */
	void setChannel(String channel);
	
	/**
	 * Gets the channel logo.
	 *
	 * @return the channel logo
	 */
	String getChannelLogo();
	
	/**
	 * Sets the channel logo.
	 *
	 * @param channelLogo the new channel logo
	 */
	void setChannelLogo(String channelLogo);

	/**
	 * Gets the epg id.
	 *
	 * @return the epgID
	 */
	long getEpgID();

	/**
	 * Sets the epg id.
	 *
	 * @param epgID the epgID to set
	 */
	void setEpgID(long epgID);

	/**
	 * Gets the start.
	 *
	 * @return the start
	 */
	Date getStart();

	/**
	 * Sets the start.
	 *
	 * @param start the start to set
	 */
	void setStart(Date start);

	/**
	 * Gets the end.
	 *
	 * @return the end
	 */
	Date getEnd();

	/**
	 * Sets the end.
	 *
	 * @param end the end to set
	 */
	void setEnd(Date end);

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	String getTitle();

	/**
	 * Sets the title.
	 *
	 * @param title the title to set
	 */
	void setTitle(String title);

	/**
	 * Gets the subtitle.
	 *
	 * @return the subtitle
	 */
	String getSubTitle();

	/**
	 * Sets the subtitle.
	 *
	 * @param subTitle the subtitle to set
	 */
	void setSubTitle(String subTitle);

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	String getDescription();

	/**
	 * Sets the description.
	 *
	 * @param description the description to set
	 */
	void setDescription(String description);

}