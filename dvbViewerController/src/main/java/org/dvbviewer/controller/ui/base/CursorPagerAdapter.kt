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
package org.dvbviewer.controller.ui.base

import android.database.Cursor

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * The Class CursorPagerAdapter.
 *
 */
abstract class CursorPagerAdapter
/**
 * Instantiates a new cursor pager adapter.
 *
 * @param fm the fm
 * @param cursor the cursor
 */
@JvmOverloads constructor(fm: FragmentManager, cursor: Cursor? = null) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    /**
     * Gets the cursor.
     *
     * @return the cursor
     */
    var cursor: Cursor? = null
        protected set

    init {
        this.cursor = cursor
    }

    /* (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    override fun getCount(): Int {
        return if (cursor == null) 0 else cursor!!.count
    }

    /**
     * Changes the dursor and without closing the old one
     *
     * @param cursor the Cursor
     */
    fun swapCursor(cursor: Cursor): Cursor? {
        if (cursor === this.cursor) {
            return null
        }
        val oldCursor = this.cursor
        this.cursor = cursor
        return oldCursor
    }

    /**
     * Changes the dursor and closes the old one
     *
     * @param cursor the Cursor
     */
    fun changeCursor(cursor: Cursor): Cursor? {
        val oldCursor = swapCursor(cursor)
        oldCursor?.close()
        return oldCursor
    }
}