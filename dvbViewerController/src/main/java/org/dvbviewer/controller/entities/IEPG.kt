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

import android.os.Parcelable
import java.util.*

/**
 * Interface for EPG entries.
 *
 * @author RayBa
 */
interface IEPG : Parcelable {

    /**
     * Gets the id.
     *
     * @return the id
     */
    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    var id: Long

    /**
     * Gets the channel.
     *
     * @return the channel
     */
    /**
     * Sets the channel.
     *
     * @param channel the channel to set
     */
    var channel: String

    /**
     * Gets the channel logo.
     *
     * @return the channel logo
     */
    /**
     * Sets the channel logo.
     *
     * @param channelLogo the new channel logo
     */
    var channelLogo: String

    /**
     * Gets the epg id.
     *
     * @return the epgID
     */
    /**
     * Sets the epg id.
     *
     * @param epgID the epgID to set
     */
    var epgID: Long

    /**
     * Gets the start.
     *
     * @return the start
     */
    /**
     * Sets the start.
     *
     * @param start the start to set
     */
    var start: Date

    /**
     * Gets the end.
     *
     * @return the end
     */
    /**
     * Sets the end.
     *
     * @param end the end to set
     */
    var end: Date

    /**
     * Gets the title.
     *
     * @return the title
     */
    /**
     * Sets the title.
     *
     * @param title the title to set
     */
    var title: String

    /**
     * Gets the subtitle.
     *
     * @return the subtitle
     */
    /**
     * Sets the subtitle.
     *
     * @param subTitle the subtitle to set
     */
    var subTitle: String

    /**
     * Gets the description.
     *
     * @return the description
     */
    /**
     * Sets the description.
     *
     * @param description the description to set
     */
    var description: String

}