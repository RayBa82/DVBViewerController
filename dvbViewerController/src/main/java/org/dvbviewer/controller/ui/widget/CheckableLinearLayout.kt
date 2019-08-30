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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.Checkable
import android.widget.ImageView
import android.widget.LinearLayout

import org.dvbviewer.controller.R

/**
 * The Class CheckableLinearLayout.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class CheckableLinearLayout
/**
 * Instantiates a new checkable linear layout.
 *
 * @param context the context
 * @param attrs the attrs
 * @author RayBa
 * @date 07.04.2013
 */
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs), Checkable {

    private var mChecked: Boolean = false
    private var touchPadding: Int = 0
    private var checkIndicator: CheckBox? = null
    private var contextMenuButton: ImageView? = null

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

    /* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val width = width
        val leftBound = width / 7
        val rightbound = width / 7 * 6
        if (checkIndicator != null && contextMenuButton!!.visibility == View.VISIBLE && event.x < leftBound) {
            event.setLocation(1f, 1f)
            checkIndicator!!.onTouchEvent(event)
            return true
        } else if (contextMenuButton != null && contextMenuButton!!.visibility == View.VISIBLE && event.x > rightbound) {
            event.setLocation(1f, 1f)
            contextMenuButton!!.onTouchEvent(event)
            return true
        } else {
            return super.onTouchEvent(event)
        }
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
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            View.mergeDrawableStates(drawableState, CheckedStateSet)
        }
        return drawableState
    }


    companion object {

        private val CheckedStateSet = intArrayOf(R.attr.state_checked)
    }
}
