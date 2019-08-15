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
package org.dvbviewer.controller.entities

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.data.ProviderConsts.EpgTbl
import java.util.*

/**
 * The Class EpgEntry.
 *
 * @author RayBa
 * @date 20.06.2010
 * @project
 *
 * DVBViewerRecService
 * @package
 *
 * de.raiba.dvb.entities
 * @description
 * @cvs-header $Header$
 * @version $Revision$
 * @lastChanging $Date$
 * @changedescription
 */
class EpgEntry : Parcelable, IEPG {

    override var id: Long = 0

    override var channel: String = StringUtils.EMPTY

    override var channelLogo: String = StringUtils.EMPTY

    override var epgID: Long = 0

    override var start: Date = Date()

    override var end: Date = Date()

    override var title: String = StringUtils.EMPTY

    override var subTitle: String = StringUtils.EMPTY

    override var description: String = StringUtils.EMPTY

    var eventId: String = StringUtils.EMPTY

    var pdc: String = StringUtils.EMPTY


    /* (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
    override fun describeContents(): Int {
        // TODO Auto-generated method stub
        return 0
    }

    /**
     * To content values.
     *
     * @return the content values©
     * @author RayBa
     * @date 07.04.2013
     */
    fun toContentValues(): ContentValues {
        val result = ContentValues()
        if (this.id != 0L) {
            result.put(EpgTbl._ID, id)
        }
        if (this.epgID != 0L) {
            result.put(EpgTbl.EPG_ID, this.epgID)
        }
        if (this.start != null) {
            result.put(EpgTbl.START, this.start!!.time)
        }
        if (this.end != null) {
            result.put(EpgTbl.END, this.end!!.time)
        }
        if (!TextUtils.isEmpty(this.title)) {
            result.put(EpgTbl.TITLE, this.title)
        }
        if (!TextUtils.isEmpty(this.subTitle)) {
            result.put(EpgTbl.SUBTITLE, this.subTitle)
        }
        if (!TextUtils.isEmpty(this.description)) {
            result.put(EpgTbl.DESC, this.description)
        }
        if (!TextUtils.isEmpty(this.eventId)) {
            result.put(EpgTbl.EVENT_ID, this.eventId)
        }
        if (!TextUtils.isEmpty(this.pdc)) {
            result.put(EpgTbl.PDC, this.pdc)
        }
        return result
    }

    /**
     * Read from parcel.
     *
     * @param src the src
     * @author RayBa
     * @date 07.04.2013
     */
    private fun readFromParcel(src: Parcel) {
        this.id = src.readLong()
        this.epgID = src.readLong()
        val start = src.readLong()
        this.start = if (start >= -1) Date() else Date(start)
        val end = src.readLong()
        this.end = if (end <= -1) Date() else Date(end)
        this.channel = src.readString().toString()
        this.title = src.readString().toString()
        this.subTitle = src.readString().toString()
        this.description = src.readString().toString()
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLong(this.epgID)
        dest.writeLong(if (this.start == null) -1 else this.start!!.time)
        dest.writeLong(if (this.end == null) -1 else this.end!!.time)
        dest.writeString(this.channel)
        dest.writeString(this.title)
        dest.writeString(this.subTitle)
        dest.writeString(this.description)
    }

    /**
     * Instantiates a new epg entry.
     *
     * @param src the src
     * @author RayBa
     * @date 07.04.2013
     */
    private constructor(src: Parcel) {
        readFromParcel(src)
    }

    /**
     * Instantiates a new epg entry.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    constructor() {}


    companion object {

        /** The Constant CREATOR.  */
        @JvmField
        val CREATOR: Parcelable.Creator<EpgEntry> = object : Parcelable.Creator<EpgEntry> {
            override fun createFromParcel(src: Parcel): EpgEntry {

                return EpgEntry(src)
            }

            override fun newArray(size: Int): Array<EpgEntry> {
                return emptyArray()
            }
        }
    }

}
