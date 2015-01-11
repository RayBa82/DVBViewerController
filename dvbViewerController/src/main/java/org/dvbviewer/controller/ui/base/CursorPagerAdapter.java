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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
 
/**
 * The Class CursorPagerAdapter.
 *
 * @param <F> the
 * @author RayBa
 * @date 07.04.2013
 */
public class CursorPagerAdapter<F extends Fragment> extends FragmentStatePagerAdapter {
    private final Class<F> fragmentClass;
    private final String[] projection;
    private Cursor cursor;
 
    /**
     * Instantiates a new cursor pager adapter.
     *
     * @param fm the fm
     * @param fragmentClass the fragment class
     * @param projection the projection
     * @param cursor the cursor
     * @author RayBa
     * @date 07.04.2013
     */
    public CursorPagerAdapter(FragmentManager fm, Class<F> fragmentClass, String[] projection, Cursor cursor) {
        super(fm);
        this.fragmentClass = fragmentClass;
        this.projection = projection;
        this.cursor = cursor;
    }
 
    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentStatePagerAdapter#getItem(int)
     */
    @Override
    public F getItem(int position) {
        if (cursor == null) // shouldn't happen
            return null;
 
        cursor.moveToPosition(position);
        F frag;
        try {
            frag = fragmentClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        Bundle args = new Bundle();
        for (int i = 0; i < projection.length; ++i) {
            args.putString(projection[i], cursor.getString(i));
        }
        frag.setArguments(args);
        return frag;
    }
 
    /* (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        if (cursor == null)
            return 0;
        else
            return cursor.getCount();
    }
 
    /**
     * Swap cursor.
     *
     * @param c the c
     * @author RayBa
     * @date 07.04.2013
     */
    public void swapCursor(Cursor c) {
        if (cursor == c)
            return;
 
        this.cursor = c;
        notifyDataSetChanged();
    }
 
    /**
     * Gets the cursor.
     *
     * @return the cursor
     * @author RayBa
     * @date 07.04.2013
     */
    public Cursor getCursor() {
        return cursor;
    }
}