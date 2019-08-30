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
package org.dvbviewer.controller.ui.widget

import android.content.Context
import android.text.format.DateUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import java.util.*

/**
 * The Class DateField.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class DateField : AppCompatEditText {

    private var mDate: Date? = null

    /**
     * Gets the date.
     *
     * @return the date
     * @author RayBa
     * @date 07.04.2013
     */
    /**
     * Sets the date.
     *
     * @param date the new date
     * @author RayBa
     * @date 07.04.2013
     */
    var date: Date?
        get() = mDate
        set(date) {
            this.mDate = date
            setText(DateUtils.formatDateTime(context, mDate!!.time, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_ABBREV_WEEKDAY or DateUtils.LENGTH_LONG))
        }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    /**
     * Sets the time.
     *
     * @param date the new time
     * @author RayBa
     * @date 07.04.2013
     */
    fun setTime(date: Date) {
        this.mDate = date
        setText(DateUtils.formatDateTime(context, mDate!!.time, DateUtils.FORMAT_SHOW_TIME))
    }

}
