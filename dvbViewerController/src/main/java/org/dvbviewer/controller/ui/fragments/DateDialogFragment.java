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
package org.dvbviewer.controller.ui.fragments;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * The Class DateDialogFragment.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class DateDialogFragment extends AppCompatDialogFragment {

	public static final int		TYPE_DATE	= 1;
	public static final int		TYPE_TIME	= 2;

	int							type		= TYPE_DATE;

	public static String		TAG			= "DateDialogFragment";

	static Context				sContext;
	static Date					sDate;
	static OnDateSetListener	mDateSetListener;
	static OnTimeSetListener	mTimeSetListener;

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		type = getArguments().getInt("type", TYPE_DATE);
	}

	/**
	 * New instance.
	 *
	 * @param context the context
	 * @param titleResource the title resource
	 * @param date the date
	 * @param type the type
	 * @return the date dialog fragment©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static DateDialogFragment newInstance(Context context, int titleResource, Date date, int type) {
		DateDialogFragment dialog = new DateDialogFragment();

		sContext = context;
		if (date != null) {
			sDate = date;
		} else {
			sDate = new Date();
		}

		Bundle args = new Bundle();
		if (titleResource > 0) {
			args.putInt("title", titleResource);
		}
		args.putInt("type", type);
		dialog.setArguments(args);
		return dialog;
	}

	/**
	 * New instance.
	 *
	 * @param context the context
	 * @param listener the listener
	 * @return the date dialog fragment©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static DateDialogFragment newInstance(Context context, OnDateSetListener listener) {
		mDateSetListener = listener;
		return newInstance(context, 0, null, TYPE_DATE);
	}
	
	/**
	 * New instance.
	 *
	 * @param context the context
	 * @param listener the listener
	 * @param date the date
	 * @return the date dialog fragment©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static DateDialogFragment newInstance(Context context, OnDateSetListener listener, Date date) {
		mDateSetListener = listener;
		return newInstance(context, 0, date, TYPE_DATE);
	}

	/**
	 * New instance.
	 *
	 * @param context the context
	 * @param listener the listener
	 * @return the date dialog fragment©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static DateDialogFragment newInstance(Context context, OnTimeSetListener listener) {
		mTimeSetListener = listener;
		return newInstance(context, 0, null, TYPE_TIME);
	}
	
	/**
	 * New instance.
	 *
	 * @param context the context
	 * @param listener the listener
	 * @param date the date
	 * @return the date dialog fragment©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static DateDialogFragment newInstance(Context context, OnTimeSetListener listener, Date date) {
		mTimeSetListener = listener;
		return newInstance(context, 0, date, TYPE_TIME);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(sDate);
		switch (type) {
			case TYPE_TIME:
				return new TimePickerDialog(sContext, mTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
			default:
				return new DatePickerDialog(sContext, mDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		}

	}

}
