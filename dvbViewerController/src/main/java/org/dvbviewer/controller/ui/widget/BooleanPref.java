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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.dvbviewer.controller.R;

/**
 * The Class BooleanPref.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class BooleanPref extends RelativeLayout implements Checkable {

	CheckBox			checkIndicator;
	ImageView			contextMenuButton;
	private int			checkboxTouchPadding;
	private Drawable	drawable;
	private boolean		checked	= false;

	/**
	 * Instantiates a new boolean pref.
	 *
	 * @param context the context
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public BooleanPref(Context context) {
		this(context, null);
	}

	/**
	 * Instantiates a new boolean pref.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public BooleanPref(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Instantiates a new boolean pref.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 * @param defStyle the def style
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public BooleanPref(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setClickable(true);
	}

	/* (non-Javadoc)
	 * @see android.view.View#onFinishInflate()
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		checkboxTouchPadding = (int) (10 * getResources().getDisplayMetrics().density);
		checkIndicator = (CheckBox) findViewById(R.id.checkIndicator);
		contextMenuButton = (ImageView) findViewById(R.id.contextMenu);
		drawable = getBackground();
	}


	/* (non-Javadoc)
	 * @see android.widget.Checkable#setChecked(boolean)
	 */
	public void setChecked(boolean checked) {
		if (checkIndicator != null) {
			checkIndicator.setChecked(checked);
			invalidate();
		}
		this.checked = checked;
	}


	/* (non-Javadoc)
	 * @see android.widget.Checkable#isChecked()
	 */
	@Override
	public boolean isChecked() {
		return checked;
	}

	/* (non-Javadoc)
	 * @see android.widget.Checkable#toggle()
	 */
	@Override
	public void toggle() {
		if (checkIndicator != null) {
			checkIndicator.toggle();
			invalidate();
		}
		this.checked = !checked;
	}

	

	/**
	 * Determines if given points are inside view.
	 *
	 * @param event the event
	 * @param view - view object to compare
	 * @return true if the points are within view bounds, false otherwise
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private boolean isPointInsideView(MotionEvent event, View view) {
		int location[] = new int[2];
		view.getLocationOnScreen(location);
		int viewX = location[0];
		int viewY = location[1];

		// point is inside view bounds
		if ((event.getRawX() > (viewX - checkboxTouchPadding) && event.getRawX() < (viewX + view.getWidth() + checkboxTouchPadding))) {
			event.setLocation(viewX, viewY);
			return true;
		} else {
			return false;
		}
	}

}
