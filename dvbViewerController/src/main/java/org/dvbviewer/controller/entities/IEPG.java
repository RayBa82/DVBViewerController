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

import java.util.Date;

import android.os.Parcelable;

/**
 * The Interface IEPG.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public interface IEPG extends Parcelable {

	/**
	 * Gets the id.
	 *
	 * @return the id
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public long getId();

	/**
	 * Sets the id.
	 *
	 * @param id the id to set
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setId(long id);

	/**
	 * Gets the channel.
	 *
	 * @return the channel
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public String getChannel();

	/**
	 * Sets the channel.
	 *
	 * @param channel the channel to set
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setChannel(String channel);
	
	/**
	 * Gets the channel logo.
	 *
	 * @return the channel logo
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public String getChannelLogo();
	
	/**
	 * Sets the channel logo.
	 *
	 * @param channelLogo the new channel logo
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setChannelLogo(String channelLogo);

	/**
	 * Gets the epg id.
	 *
	 * @return the epgID
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public long getEpgID();

	/**
	 * Sets the epg id.
	 *
	 * @param epgID the epgID to set
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setEpgID(long epgID);

	/**
	 * Gets the start.
	 *
	 * @return the start
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Date getStart();

	/**
	 * Sets the start.
	 *
	 * @param start the start to set
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setStart(Date start);

	/**
	 * Gets the end.
	 *
	 * @return the end
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Date getEnd();

	/**
	 * Sets the end.
	 *
	 * @param end the end to set
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setEnd(Date end);

	/**
	 * Gets the title.
	 *
	 * @return the title
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public String getTitle();

	/**
	 * Sets the title.
	 *
	 * @param title the title to set
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setTitle(String title);

	/**
	 * Gets the subtitle.
	 *
	 * @return the subtitle
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public String getSubTitle();

	/**
	 * Sets the subtitle.
	 *
	 * @param subTitle the subtitle to set
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setSubTitle(String subTitle);

	/**
	 * Gets the description.
	 *
	 * @return the description
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public String getDescription();

	/**
	 * Sets the description.
	 *
	 * @param description the description to set
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setDescription(String description);

}