/*
 * Copyright © 2010 dvbviewer-controller Project
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
package org.dvbviewer.controller.utils;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * The Class DateUtils.
 * 
 * @author RayBa
 * @date 21.06.2010
 */
public class DateUtils extends android.text.format.DateUtils {

	/** The Constant DATEFORMAT_RS_EPG. */
	public static final String		DATEFORMAT_RS_EPG		= "yyyyMMddHHmmss";

	/** The Constant DATEFORMAT_RS_TIMER. */
	public static final String		DATEFORMAT_RS_TIMER		= "dd.MM.yyyy";

	/** The Constant DATEFORMAT_TIME. */
	public static final String		TIMEFORMAT_RS_TIMER		= "HH:mm:ss";

	public static final String		TIMEFORMAT_RS_RECORDING	= "HHmmss";

	private static final TimeZone	timeZone				= TimeZone.getDefault();

	/** The date format. */
	private static SimpleDateFormat	dateFormat;

	/**
	 * Date to string.
	 * 
	 * @param d
	 *            the d
	 * @param format
	 *            the format
	 * @return the string©
	 * @author RayBa
	 * @date 08.04.2012
	 */
	public static String dateToString(Date d, String format) {
		String result = "";
		try {
			if (dateFormat == null) {
				dateFormat = new SimpleDateFormat();
			}
			dateFormat.applyPattern(format);
			result = dateFormat.format(d);
		} catch (Exception e) {
			Log.e(DateUtils.class.getSimpleName(), "ERROR CONVERTING DATE TO STRING");
			Log.d(DateUtils.class.getSimpleName(), e.getLocalizedMessage());
			Log.d(DateUtils.class.getSimpleName(), e.getMessage());
			Log.d(DateUtils.class.getSimpleName(), e.toString());
		}
		return result;
	}

	/**
	 * converts time (in seconds) to human-readable format
	 */
	public static CharSequence secondsToReadableFormat(long duration) {
		final long now = System.currentTimeMillis();
		final long time = now + (duration * 1000);
		return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.SECOND_IN_MILLIS);
	}

	/**
	 * String to date.
	 * 
	 * @param date
	 *            the date
	 * @param format
	 *            the format
	 * @return the date©
	 * @author RayBa
	 * @date 08.04.2012
	 */
	public static Date stringToDate(String date, String format) {
		Date result = null;
		try {
			if (dateFormat == null) {
				dateFormat = new SimpleDateFormat();
			}
			result = new SimpleDateFormat(format).parse(date);
		} catch (Exception e) {
			Log.e(DateUtils.class.getSimpleName(), "ERROR CONVERTING DATE TO STRING");
			Log.d(DateUtils.class.getSimpleName(), e.getLocalizedMessage());
			Log.d(DateUtils.class.getSimpleName(), e.getMessage());
			Log.d(DateUtils.class.getSimpleName(), e.toString());
		}
		return result;
	}

	/**
	 * Gets the calender.
	 * 
	 * @return the calender
	 * 
	 * @author RayBa
	 * @date 22.06.2010
	 * @description Gets the calender.
	 */
	public static Calendar getCalender() {
		return GregorianCalendar.getInstance();
	}

	/**
	 * Gets the float date.
	 * 
	 * @param date
	 *            the date
	 * @return the float date
	 * @author RayBa
	 * @date 08.04.2012
	 */
	public static String getFloatDate(Date date) {
		StringBuffer result = new StringBuffer();
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);
		long days = getDaysSinceDelphiNull(date);
		long hours = cal.get(Calendar.HOUR_OF_DAY);
		long minutesOfDay = hours * 60;
		long minutes = cal.get(Calendar.MINUTE);
		minutesOfDay = minutesOfDay + minutes;
		Double percentage = minutesOfDay / (24d * 60d);
		StringBuffer percentageString = new StringBuffer(percentage.toString());
		percentageString.replace(0, 2, "");
		result.append(days).append(".").append(percentageString);
		return result.toString();
	}

	/**
	 * Gets the days since delphi null.
	 * 
	 * @param date
	 *            the date
	 * @return the days since delphi null
	 * @author RayBa
	 * @date 08.04.2012
	 */
	public static long getDaysSinceDelphiNull(Date date) {
		return getDifference(truncate(date), getBaseDate());
	}

	/**
	 * Gets the base date.
	 * 
	 * @return the base date
	 * @author RayBa
	 * @date 08.04.2012
	 */
	public static Date getBaseDate() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 30);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		cal.set(Calendar.YEAR, 1899);
		// cal.set(Calendar.HOUR_OF_DAY,12);
		// cal.set(Calendar.MINUTE, 12);
		// cal.set(Calendar.MILLISECOND, 12);
		// cal.set(Calendar.SECOND, 12);
		return truncate(cal.getTime());
	}

	/**
	 * Truncate.
	 *
	 * @param date the date
	 * @return the date©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static Date truncate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		date = cal.getTime();
		return date;
	}

	/**
	 * Sets the current time.
	 *
	 * @param date the date
	 * @return the date©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static Date setCurrentTime(Date date) {
		Calendar calToSet = Calendar.getInstance();
		Calendar currentCal = Calendar.getInstance();
		calToSet.setTime(date);
		calToSet.set(Calendar.HOUR_OF_DAY, currentCal.get(Calendar.HOUR_OF_DAY));
		calToSet.set(Calendar.MINUTE, currentCal.get(Calendar.MINUTE));
		calToSet.set(Calendar.SECOND, currentCal.get(Calendar.SECOND));
		calToSet.set(Calendar.MILLISECOND, currentCal.get(Calendar.MILLISECOND));
		date = calToSet.getTime();
		return date;
	}

	/**
	 * Sets the evening time.
	 *
	 * @param date the date
	 * @return the date©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static Date setEveningTime(Date date) {
		Calendar calToSet = Calendar.getInstance();
		calToSet.setTime(date);
		calToSet.set(Calendar.HOUR_OF_DAY, 20);
		calToSet.set(Calendar.MINUTE, 20);
		calToSet.set(Calendar.SECOND, 0);
		calToSet.set(Calendar.MILLISECOND, 0);
		date = calToSet.getTime();
		return date;
	}

	/**
	 * Gets the used minutes of day for the date param.
	 * 
	 * @param date
	 *            the date
	 * @return the minutes of day
	 * @author RayBa
	 * @date 08.04.2012
	 */
	public static int getMinutesOfDay(Date date) {
		Calendar cal = getCalender();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
	}

	/**
	 * Gets the days between two dates.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the difference
	 * @author RayBa
	 * @date 08.04.2012
	 */
	public static long getDifference(Date a, Date b) {
		Calendar startCal = GregorianCalendar.getInstance();
		startCal.setTimeZone(timeZone);
		startCal.setTime(a);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.MILLISECOND, 0);
		startCal.set(Calendar.SECOND, 0);

		Calendar endCal = GregorianCalendar.getInstance();
		endCal.setTimeZone(timeZone);
		endCal.setTime(b);
		endCal.set(Calendar.HOUR_OF_DAY, 0);
		endCal.set(Calendar.MINUTE, 0);
		endCal.set(Calendar.MILLISECOND, 0);
		endCal.set(Calendar.SECOND, 0);

		long endL = endCal.getTimeInMillis() + endCal.getTimeZone().getOffset(endCal.getTimeInMillis());
		long startL = startCal.getTimeInMillis() + startCal.getTimeZone().getOffset(startCal.getTimeInMillis());
		return (startL - endL) / (1000 * 60 * 60 * 24);

		// return units.convert(startCal.getTimeInMillis() -
		// endCal.getTimeInMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Gets the date in local format.
	 * 
	 * @param date
	 *            the date
	 * 
	 * @return the date in local format
	 * 
	 * @author RayBa
	 * @date 26.06.2010
	 * @description Gets the date in local format.
	 */
	public static String getDateInLocalFormat(Date date) {
		return DateFormat.getDateInstance().format(date);
	}

	/**
	 * Gets the time in local format.
	 * 
	 * @param date
	 *            the date
	 * 
	 * @return the time in local format
	 * 
	 * @author RayBa
	 * @date 26.06.2010
	 * @description Gets the time in local format.
	 */
	public static String getTimeInLocalFormat(Context context, Date date) {
		String result = android.text.format.DateFormat.getTimeFormat(context).format(date);
		return result;
	}

	/**
	 * Adds one day of the given Date.
	 *
	 * @param d the d
	 * @return date + 1 day
	 * @author RayBa
	 * @date 29.04.2012
	 */
	public static Date addDay(Date d) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		return cal.getTime();
	}

	/**
	 * Substracts one day of the given Date.
	 *
	 * @param date the date
	 * @return date - 1 day
	 * @author RayBa
	 * @date 29.04.2012
	 */
	public static Date substractDay(Date date) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		return cal.getTime();
	}

	/**
	 * Adds the time value of the time parameter to the base Date parameter.
	 *
	 * @param base the base
	 * @param time the time
	 * @return the date©
	 * @author RayBa
	 * @date 29.04.2012
	 */
	public static Date addTime(Date base, Date time) {
		Calendar cal1 = GregorianCalendar.getInstance();
		cal1.setTime(base);
		Calendar cal2 = GregorianCalendar.getInstance();
		cal2.setTime(time);
		cal1.add(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
		cal1.add(Calendar.SECOND, cal2.get(Calendar.SECOND));
		cal1.add(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
		cal1.add(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
		return cal1.getTime();
	}

	/**
	 * Adds the given amount of Minutes the base Date.
	 *
	 * @param base the base
	 * @param minutes the minutes
	 * @return the date©
	 * @author RayBa
	 * @date 29.04.2012
	 */
	public static Date addMinutes(Date base, int minutes) {
		Calendar cal1 = GregorianCalendar.getInstance();
		cal1.setTime(base);
		cal1.add(Calendar.MINUTE, minutes);
		return cal1.getTime();
	}

	/**
	 * Checks if the when parameter is tomorrow.
	 * 
	 * @param when
	 *            the when
	 * @return true if the supplied when is tommorrow else false
	 * @author RayBa
	 * @date 29.04.2012
	 */
	public static boolean isTomorrow(long when) {
		Time time = new Time();
		time.set(when);

		int thenYear = time.year;
		int thenMonth = time.month;
		int thenMonthDay = time.monthDay;

		long tommorrow = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
		time.set(tommorrow);
		return (thenYear == time.year) && (thenMonth == time.month) && (thenMonthDay == time.monthDay);
	}

}
