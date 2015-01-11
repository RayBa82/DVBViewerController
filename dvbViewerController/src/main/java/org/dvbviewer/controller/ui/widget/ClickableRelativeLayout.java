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

import org.dvbviewer.controller.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * The Class ClickableRelativeLayout.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class ClickableRelativeLayout extends RelativeLayout implements Checkable {

	CheckBox					checkIndicator;
	ImageView					contextMenuButton;
	private int					touchPadding;
	private boolean				mChecked = false;
	private boolean				error;
	private boolean				disabled;
	

	private static final int[] CheckedStateSet = {
	    R.attr.state_checked
	};
	private static final int[] DisabledStateSet = {
		R.attr.state_disabled
	};
	private static final int[] ErrorStateSet = {
		R.attr.state_error
	};

	/**
	 * Instantiates a new clickable relative layout.
	 *
	 * @param context the context
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public ClickableRelativeLayout(Context context) {
		this(context, null);
	}

	/**
	 * Instantiates a new clickable relative layout.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public ClickableRelativeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	
	/**
	 * Instantiates a new clickable relative layout.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 * @param defStyle the def style
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public ClickableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
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
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);
		if (isChecked()) {
	        mergeDrawableStates(drawableState, CheckedStateSet);
	    }
		if (isDisabled()) {
			mergeDrawableStates(drawableState, DisabledStateSet);
		}
		if (isError()) {
			mergeDrawableStates(drawableState, ErrorStateSet);
		}
		return drawableState;
	}

	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (checkIndicator != null && checkIndicator.getVisibility() == View.VISIBLE && isPointInsideView(event, checkIndicator)) {
			event.setLocation(1, 1);
			checkIndicator.onTouchEvent(event);
			return true;
		} else if (contextMenuButton != null && contextMenuButton.getVisibility() == View.VISIBLE && isPointInsideView(event, contextMenuButton)) {
			event.setLocation(1, 1);
			contextMenuButton.onTouchEvent(event);
			return true;
		} else {
			return super.onTouchEvent(event);
		}
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
		boolean isInX = event.getRawX() > (viewX - touchPadding) && event.getRawX() < (viewX + view.getWidth() + touchPadding);
		if (isInX) {
			event.setLocation(viewX+ 1, viewY+1);
			return true;
		} else {
			return false;
		}
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
	

	/**
	 * Checks if is error.
	 *
	 * @return true, if is error
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * Sets the error.
	 *
	 * @param mError the new error
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setError(boolean mError) {
		this.error = mError;
		refreshDrawableState();
	}

	/**
	 * Checks if is disabled.
	 *
	 * @return true, if is disabled
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Sets the disabled.
	 *
	 * @param mDisabled the new disabled
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setDisabled(boolean mDisabled) {
		this.disabled = mDisabled;
		refreshDrawableState();
	}
}
