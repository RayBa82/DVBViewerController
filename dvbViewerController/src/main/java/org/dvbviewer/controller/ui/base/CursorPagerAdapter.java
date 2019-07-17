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
package org.dvbviewer.controller.ui.base;

import android.database.Cursor;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * The Class CursorPagerAdapter.
 *
 */
public abstract class CursorPagerAdapter extends FragmentStatePagerAdapter {

    protected Cursor mCursor;

    /**
     * Instantiates a new cursor pager adapter.
     *
     * @param fm the fm
     * @param cursor the cursor
     */
    public CursorPagerAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);
        this.mCursor = cursor;
    }
    public CursorPagerAdapter(FragmentManager fm) {
        this(fm, null);
    }

    /* (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    /**
     * Changes the dursor and without closing the old one
     *
     * @param cursor the Cursor
     */
    public Cursor swapCursor(Cursor cursor) {
        if (cursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;
        return oldCursor;
    }

    /**
     * Changes the dursor and closes the old one
     *
     * @param cursor the Cursor
     */
    public Cursor changeCursor(Cursor cursor) {
        Cursor oldCursor = swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
        return oldCursor;
    }

    /**
     * Gets the cursor.
     *
     * @return the cursor
     */
    public Cursor getCursor() {
        return mCursor;
    }
}