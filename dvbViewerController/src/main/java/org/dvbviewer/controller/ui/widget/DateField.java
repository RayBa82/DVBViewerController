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
package org.dvbviewer.controller.ui.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.format.DateUtils;
import android.util.AttributeSet;

import java.util.Date;

/**
 * The Class DateField.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class DateField extends AppCompatEditText {

	private Date	mDate;

	public DateField(Context context) {
		super(context);
	}

	public DateField(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DateField(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public Date getDate() {
		return mDate;
	}

	/**
	 * Sets the date.
	 *
	 * @param date the new date
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setDate(Date date) {
		this.mDate = date;
		setText(DateUtils.formatDateTime(getContext(), mDate.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.LENGTH_LONG));
	}

	/**
	 * Sets the time.
	 *
	 * @param date the new time
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setTime(Date date) {
		this.mDate = date;
		setText(DateUtils.formatDateTime(getContext(), mDate.getTime(), DateUtils.FORMAT_SHOW_TIME));
	}

}
