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
package org.dvbviewer.controller.data.entities

import java.io.Serializable
import java.util.*

/**
 * The Class Timer.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class Timer : Comparable<Timer>, Serializable {

    var id: Long = -1
    var channelId: Long = 0
    var channelName: String? = null
    var title: String? = null
    var start: Date? = null
    var end: Date? = null
    var timerAction: Int = 0
    var pre: Int = 0
    var post: Int = 0
    var eventId: String? = null
    var pdc: String? = null
    var adjustPAT = -1
    var allAudio = -1
    var dvbSubs = -1
    var teletext = -1
    var eitEPG = -1
    var monitorPDC = -1
    var runningStatusSplit = -1
    /**
     * Gets the flags.
     *
     * @return the flags
     * @author RayBa
     * @date 07.04.2013
     */
    var flags = FLAG_EXECUTABLE
        private set

    /**
     * Sets the flag.
     *
     * @param flag the new flag
     * @author RayBa
     * @date 07.04.2013
     */
    fun setFlag(flag: Int) {
        this.flags = this.flags or flag
    }

    /**
     * Unset flag.
     *
     * @param flag the flag
     * @author RayBa
     * @date 07.04.2013
     */
    fun unsetFlag(flag: Int) {
        this.flags = this.flags and flag.inv()
    }

    /**
     * Checks if is flag set.
     *
     * @param flag the flag
     * @return true, if is flag set
     * @author RayBa
     * @date 07.04.2013
     */
    fun isFlagSet(flag: Int): Boolean {
        return flag and flags != 0
    }

    /* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
    override fun compareTo(comparator: Timer): Int {
        return this.start!!.compareTo(comparator.start)
    }

    companion object {

        val FLAG_EXECUTABLE = 1 shl 0                    // 0x01
        val FLAG_RECORDING = 1 shl 1                    // 0x02
        val FLAG_DISABLED = 1 shl 2                    // 0x02
    }


}
