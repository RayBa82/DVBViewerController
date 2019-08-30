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
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.Checkable
import android.widget.ImageView
import android.widget.RelativeLayout

import org.dvbviewer.controller.R

/**
 * The Class ClickableRelativeLayout.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class ClickableRelativeLayout
/**
 * Instantiates a new clickable relative layout.
 *
 * @param context the context
 * @param attrs the attrs
 * @param defStyle the def style
 * @author RayBa
 * @date 07.04.2013
 */
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RelativeLayout(context, attrs, defStyle), Checkable {

    private var checkIndicator: CheckBox? = null
    private var contextMenuButton: ImageView? = null
    private var touchPadding: Int = 0
    private var mChecked = false
    /**
     * Checks if is error.
     *
     * @return true, if is error
     * @author RayBa
     * @date 07.04.2013
     */
    /**
     * Sets the error.
     *
     * @param mError the new error
     * @author RayBa
     * @date 07.04.2013
     */
    var isError: Boolean = false
        set(mError) {
            field = mError
            refreshDrawableState()
        }
    /**
     * Checks if is disabled.
     *
     * @return true, if is disabled
     * @author RayBa
     * @date 07.04.2013
     */
    /**
     * Sets the disabled.
     *
     * @param mDisabled the new disabled
     * @author RayBa
     * @date 07.04.2013
     */
    var isDisabled: Boolean = false
        set(mDisabled) {
            field = mDisabled
            refreshDrawableState()
        }

    /* (non-Javadoc)
	 * @see android.view.View#onFinishInflate()
	 */
    override fun onFinishInflate() {
        super.onFinishInflate()
        touchPadding = (15 * resources.displayMetrics.density).toInt()
        checkIndicator = findViewById<CheckBox>(R.id.checkIndicator)
        contextMenuButton = findViewById<ImageView>(R.id.contextMenu)
    }

    /* (non-Javadoc)
	 * @see android.view.ViewGroup#dispatchSetPressed(boolean)
	 */
    override fun dispatchSetPressed(pressed: Boolean) {
        /**
         * Empty Override to not select the child views when the whole vie is
         * selected
         */
    }

    /* (non-Javadoc)
	 * @see android.view.ViewGroup#onCreateDrawableState(int)
	 */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 3)
        if (isChecked) {
            View.mergeDrawableStates(drawableState, CheckedStateSet)
        }
        if (isDisabled) {
            View.mergeDrawableStates(drawableState, DisabledStateSet)
        }
        if (isError) {
            View.mergeDrawableStates(drawableState, ErrorStateSet)
        }
        return drawableState
    }

    /* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (checkIndicator != null && checkIndicator!!.visibility == View.VISIBLE && isPointInsideView(event, checkIndicator!!)) {
            event.setLocation(1f, 1f)
            checkIndicator!!.onTouchEvent(event)
            return true
        } else if (contextMenuButton != null && contextMenuButton!!.visibility == View.VISIBLE && isPointInsideView(event, contextMenuButton!!)) {
            event.setLocation(1f, 1f)
            contextMenuButton!!.onTouchEvent(event)
            return true
        } else {
            return super.onTouchEvent(event)
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
    private fun isPointInsideView(event: MotionEvent, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]

        // point is inside view bounds
        val isInX = event.rawX > viewX - touchPadding && event.rawX < viewX + view.width + touchPadding
        if (isInX) {
            event.setLocation((viewX + 1).toFloat(), (viewY + 1).toFloat())
            return true
        } else {
            return false
        }
    }


    /* (non-Javadoc)
	 * @see android.widget.Checkable#isChecked()
	 */
    override fun isChecked(): Boolean {
        return mChecked
    }


    /* (non-Javadoc)
	 * @see android.widget.Checkable#setChecked(boolean)
	 */
    override fun setChecked(checked: Boolean) {
        mChecked = checked
        if (checkIndicator != null) {
            checkIndicator!!.isChecked = mChecked
        }
        refreshDrawableState()
    }

    /* (non-Javadoc)
	 * @see android.widget.Checkable#toggle()
	 */
    override fun toggle() {
        mChecked = !mChecked
        if (checkIndicator != null) {
            checkIndicator!!.isChecked = mChecked
        }
        refreshDrawableState()
    }

    companion object {


        private val CheckedStateSet = intArrayOf(R.attr.state_checked)
        private val DisabledStateSet = intArrayOf(R.attr.state_disabled)
        private val ErrorStateSet = intArrayOf(R.attr.state_error)
    }
}