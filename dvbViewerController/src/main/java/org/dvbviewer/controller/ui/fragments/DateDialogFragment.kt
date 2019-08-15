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
package org.dvbviewer.controller.ui.fragments

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import java.util.*

/**
 * The Class DateDialogFragment.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class DateDialogFragment : AppCompatDialogFragment() {

    internal var type = TYPE_DATE

    /* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments!!.getInt("type", TYPE_DATE)
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cal = GregorianCalendar.getInstance()
        cal.time = sDate
        return when (type) {
            TYPE_TIME -> TimePickerDialog(context, mTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
            else -> DatePickerDialog(context!!, mDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }

    }

    companion object {

        const val TYPE_DATE = 1
        const val TYPE_TIME = 2

        const val TAG = "DateDialogFragment"

        lateinit var sDate: Date
        private var mDateSetListener: OnDateSetListener? = null
        private var mTimeSetListener: OnTimeSetListener? = null

        /**
         * New instance.
         *
         * @param titleResource the title resource
         * @param date the date
         * @param type the type
         * @return the date dialog fragment©
         * @author RayBa
         * @date 07.04.2013
         */
        fun newInstance(titleResource: Int, date: Date?, type: Int): DateDialogFragment {
            val dialog = DateDialogFragment()

            sDate = date ?: Date()

            val args = Bundle()
            if (titleResource > 0) {
                args.putInt("title", titleResource)
            }
            args.putInt("type", type)
            dialog.arguments = args
            return dialog
        }

        /**
         * New instance.
         *
         * @param listener the listener
         * @return the date dialog fragment©
         * @author RayBa
         * @date 07.04.2013
         */
        fun newInstance(listener: OnDateSetListener): DateDialogFragment {
            mDateSetListener = listener
            return newInstance(0, null, TYPE_DATE)
        }

        /**
         * New instance.
         *
         * @param listener the listener
         * @param date the date
         * @return the date dialog fragment©
         * @author RayBa
         * @date 07.04.2013
         */
        fun newInstance(listener: OnDateSetListener, date: Date): DateDialogFragment {
            mDateSetListener = listener
            return newInstance(0, date, TYPE_DATE)
        }

        /**
         * New instance.
         *
         * @param listener the listener
         * @return the date dialog fragment©
         * @author RayBa
         * @date 07.04.2013
         */
        fun newInstance(listener: OnTimeSetListener): DateDialogFragment {
            mTimeSetListener = listener
            return newInstance(0, null, TYPE_TIME)
        }

        /**
         * New instance.
         *
         * @param listener the listener
         * @param date the date
         * @return the date dialog fragment©
         * @author RayBa
         * @date 07.04.2013
         */
        fun newInstance(listener: OnTimeSetListener, date: Date): DateDialogFragment {
            mTimeSetListener = listener
            return newInstance(0, date, TYPE_TIME)
        }
    }

}
