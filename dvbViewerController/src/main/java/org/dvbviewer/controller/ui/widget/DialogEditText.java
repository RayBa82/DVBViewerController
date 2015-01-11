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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * The Class DialogEditText.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class DialogEditText extends EditText implements DialogInterface.OnClickListener {

	/**
	 * Instantiates a new dialog edit text.
	 *
	 * @param context the context
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public DialogEditText(Context context) {
		super(context);
		setInputType(EditorInfo.TYPE_NULL);
	}

	/**
	 * Instantiates a new dialog edit text.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public DialogEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setInputType(EditorInfo.TYPE_NULL);
	}
	

	/**
	 * Instantiates a new dialog edit text.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 * @param defStyle the def style
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public DialogEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setInputType(EditorInfo.TYPE_NULL);
	}

	/* (non-Javadoc)
	 * @see android.view.View#performClick()
	 */
	@Override
	public boolean performClick() {
		Log.i(DialogEditText.class.getSimpleName(), "performClick");
		AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

		alert.setTitle("Title");
		alert.setMessage("Message");

		// Set an EditText view to get user input
		final EditText input = new EditText(getContext());
		alert.setView(input);

		alert.setPositiveButton("Ok", this);

		alert.setNegativeButton("Cancel", this);
		alert.show();
		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		
	}

	/* (non-Javadoc)
	 * @see android.widget.TextView#onSaveInstanceState()
	 */
	@Override
	public Parcelable onSaveInstanceState() {
		// TODO Auto-generated method stub
		return super.onSaveInstanceState();
	}
	
}
