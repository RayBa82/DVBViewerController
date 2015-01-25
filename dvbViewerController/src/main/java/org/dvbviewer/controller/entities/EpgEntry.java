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

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.dvbviewer.controller.data.DbConsts.EpgTbl;

import java.util.Date;

/**
 * The Class EpgEntry.
 * 
 * @author RayBa
 * @date 20.06.2010
 * @project
 * 
 *          DVBViewerRecService
 * @package
 * 
 *          de.raiba.dvb.entities
 * @description
 * @cvs-header $Header$
 * @version $Revision$
 * @lastChanging $Date$
 * @changedescription
 */
public class EpgEntry implements Parcelable, IEPG {

	private long				id;

	private long				epgID;

	private Date				start;

	private Date				end;

	private String				channel;

	private String				title;

	private String				subtitle;

	private String				description;
	
	private String				channelLogo;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#getId()
	 */
	@Override
	public long getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#setId(java.lang.Long)
	 */
	@Override
	public void setId(long id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#getEpgID()
	 */
	@Override
	public long getEpgID() {
		return epgID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#setEpgID(long)
	 */
	@Override
	public void setEpgID(long epgID) {
		this.epgID = epgID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#getStart()
	 */
	@Override
	public Date getStart() {
		return start;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#setStart(java.util.Date)
	 */
	@Override
	public void setStart(Date start) {
		this.start = start;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#getEnd()
	 */
	@Override
	public Date getEnd() {
		return end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#setEnd(java.util.Date)
	 */
	@Override
	public void setEnd(Date end) {
		this.end = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#getSubtitle()
	 */
	@Override
	public String getSubTitle() {
		return subtitle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#setSubtitle(java.lang.String)
	 */
	@Override
	public void setSubTitle(String subtitle) {
		this.subtitle = subtitle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dvbviewer.controller.entities.IEPG#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dvbviewer.controller.entities.IEPG#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
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
	 * To content values.
	 *
	 * @return the content values©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public ContentValues toContentValues() {
		ContentValues result = new ContentValues();
		if (this.id != 0l) {
			result.put(EpgTbl._ID, id);
		}
		if (this.epgID != 0l) {
			result.put(EpgTbl.EPG_ID, this.epgID);
		}
		if (this.start != null) {
			result.put(EpgTbl.START, this.start.getTime());
		}
		if (this.end != null) {
			result.put(EpgTbl.END, this.end.getTime());
		}
		if (!TextUtils.isEmpty(this.title)) {
			result.put(EpgTbl.TITLE, this.title);
		}
		if (!TextUtils.isEmpty(this.subtitle)) {
			result.put(EpgTbl.SUBTITLE, this.subtitle);
		}
		if (!TextUtils.isEmpty(this.description)) {
			result.put(EpgTbl.DESC, this.description);
		}
		return result;
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
		this.epgID = src.readLong();
		long start = src.readLong();
		this.start = (start == -1 ? null : new Date(start));
		long end = src.readLong();
		this.end = (end == -1 ? null : new Date(end));
		this.channel = src.readString();
		this.title = src.readString();
		this.subtitle = src.readString();
		this.description = src.readString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeLong(this.epgID);
		dest.writeLong(this.start == null ? -1 : this.start.getTime());
		dest.writeLong(this.end == null ? -1 : this.end.getTime());
		dest.writeString(this.channel);
		dest.writeString(this.title);
		dest.writeString(this.subtitle);
		dest.writeString(this.description);
	}

	/**
	 * Instantiates a new epg entry.
	 *
	 * @param src the src
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private EpgEntry(Parcel src) {
		readFromParcel(src);
	}

	/**
	 * Instantiates a new epg entry.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public EpgEntry() {
	}

	/** The Constant CREATOR. */
	public static final Parcelable.Creator<EpgEntry>	CREATOR	= new Parcelable.Creator<EpgEntry>() {
																	public EpgEntry createFromParcel(Parcel src) {

																		return new EpgEntry(src);
																	}

																	public EpgEntry[] newArray(int size) {
																		return new EpgEntry[size];
																	}
																};

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#getChannel()
	 */
	@Override
	public String getChannel() {
		return channel;
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#setChannel(java.lang.String)
	 */
	@Override
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#getChannelLogo()
	 */
	@Override
	public String getChannelLogo() {
		return channelLogo;
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.entities.IEPG#setChannelLogo(java.lang.String)
	 */
	@Override
	public void setChannelLogo(String channelLogo) {
		this.channelLogo = channelLogo;
	}

}
