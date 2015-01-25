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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.dvbviewer.controller.R;

/**
 * The Class CheckableLinearLayout.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {

	private boolean				mChecked;
	private int					touchPadding;
	CheckBox					checkIndicator;
	ImageView					contextMenuButton;
	
	private static final int[] CheckedStateSet = {
	    R.attr.state_checked
	};

	/**
	 * Instantiates a new checkable linear layout.
	 *
	 * @param context the context
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public CheckableLinearLayout(Context context) {
	    this(context, null);
	}

	/**
	 * Instantiates a new checkable linear layout.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public CheckableLinearLayout(Context context, AttributeSet attrs) {
	    super(context, attrs);       
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onFinishInflate()
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		touchPadding = (int) (15 * getResources().getDisplayMetrics().density);
		checkIndicator = (CheckBox) findViewById(R.id.checkIndicator);
		contextMenuButton = (ImageView) findViewById(R.id.contextMenu);
	}

	/* (non-Javadoc)
	 * @see android.widget.Checkable#isChecked()
	 */
	@Override
	public boolean isChecked() {
		return mChecked;
	}
	

	/* (non-Javadoc)
	 * @see android.widget.Checkable#setChecked(boolean)
	 */
	@Override
	public void setChecked(boolean checked) {
	    mChecked = checked;
	    if (checkIndicator != null) {
			checkIndicator.setChecked(mChecked);
		}
	    refreshDrawableState();
	}

	/* (non-Javadoc)
	 * @see android.widget.Checkable#toggle()
	 */
	@Override
	public void toggle() {
		mChecked = !mChecked;
		if (checkIndicator != null) {
			checkIndicator.setChecked(mChecked);
		}
		refreshDrawableState();
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
        int width = getWidth();
        int leftBound = width/7;
        int rightbound = width/7*6;
		if (checkIndicator != null && contextMenuButton.getVisibility() == View.VISIBLE && event.getX() < leftBound) {
			event.setLocation(1, 1);
			checkIndicator.onTouchEvent(event);
			return true;
		} else if (contextMenuButton != null && contextMenuButton.getVisibility() == View.VISIBLE &&  event.getX() > rightbound) {
			event.setLocation(1, 1);
			contextMenuButton.onTouchEvent(event);
			return true;
		} else {
			return super.onTouchEvent(event);
		}
	}

	/* (non-Javadoc)
	 * @see android.view.ViewGroup#dispatchSetPressed(boolean)
	 */
	@Override
	protected void dispatchSetPressed(boolean pressed) {
		/**
		 * Empty Override to not select the child views when the whole vie is
		 * selected
		 */
	}

			
			
	/* (non-Javadoc)
	 * @see android.view.ViewGroup#onCreateDrawableState(int)
	 */
	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
	    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
	    if (isChecked()) {
	        mergeDrawableStates(drawableState, CheckedStateSet);
	    }
	    return drawableState;
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
		if ((event.getRawX() > (viewX - touchPadding) && event.getRawX() < (viewX + view.getWidth() + touchPadding))) {
			event.setLocation(viewX, viewY);
			return true;
		} else {
			return false;
		}
	}
}
