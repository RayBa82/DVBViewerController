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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * The Class Recording.
 * 
 * @author RayBa
 * @date 09.04.2012
 */
public class Recording implements IEPG, Comparable<Recording> {

	private long				id;

	private String				title;

	private String				channel;
	
	private String				channelLogo;

	private String				subTitle;

	private String				description;

	private Date				start;

	private Date				end;

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            the new title
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the channel.
	 * 
	 * @return the channel
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Sets the channel.
	 * 
	 * @param channel
	 *            the new channel
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#getSubTitle()
	 */
	@Override
	public String getSubTitle() {
		return subTitle;
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#setSubTitle(java.lang.String)
	 */
	@Override
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description
	 *            the new description
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the start.
	 * 
	 * @return the start
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * Sets the date.
	 * 
	 * @param date
	 *            the date to set
	 * @author RayBa
	 * @date 09.04.2012
	 */
	public void setStart(Date date) {
		this.start = date;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Recording comparator) {
		return 0 - this.start.compareTo(comparator.getStart());
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#getEpgID()
	 */
	@Override
	public long getEpgID() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#setEpgID(long)
	 */
	@Override
	public void setEpgID(long epgID) {

	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#getEnd()
	 */
	@Override
	public Date getEnd() {
		return end;
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#setEnd(java.util.Date)
	 */
	@Override
	public void setEnd(Date end) {
		this.end = end;
	}
	
	/* (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Read from parcel.
	 *
	 * @param src the src
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private void readFromParcel(Parcel src) {
		this.id = src.readLong();
		this.channel = src.readString();
		long start = src.readLong();
		this.start = (start == -1 ? null : new Date(start));
		long end = src.readLong();
		this.end = (end == -1 ? null : new Date(end));
		this.title = src.readString();
		this.subTitle = src.readString();
		this.description = src.readString();
		this.channelLogo = src.readString();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeString(this.channel);
		dest.writeLong(this.start == null ? -1 : this.start.getTime());
		dest.writeLong(this.end == null ? -1 : this.end.getTime());
		dest.writeString(this.title);
		dest.writeString(this.subTitle);
		dest.writeString(this.description);
		dest.writeString(this.channelLogo);
	}

	/**
	 * Instantiates a new recording.
	 *
	 * @param src the src
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Recording(Parcel src) {
		readFromParcel(src);
	}
	
	/**
	 * Instantiates a new recording.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Recording() {
		
	}

	/** The Constant CREATOR. */
	public static final Parcelable.Creator<Recording>	CREATOR	= new Parcelable.Creator<Recording>() {
																public Recording createFromParcel(Parcel src) {

																	return new Recording(src);
																}

																public Recording[] newArray(int size) {
																	return new Recording[size];
																}
															};

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#getChannelLogo()
	 */
	public String getChannelLogo() {
		return channelLogo;
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#setChannelLogo(java.lang.String)
	 */
	public void setChannelLogo(String channelLogo) {
		this.channelLogo = channelLogo;
	}

}
