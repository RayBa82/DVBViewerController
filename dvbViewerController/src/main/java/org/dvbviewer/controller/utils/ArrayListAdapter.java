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
package org.dvbviewer.controller.utils;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;

/**
 * The Class ArrayListAdapter.
 *
 * @param <E> the
 * @author RayBa
 * @date 07.04.2013
 */
public abstract class ArrayListAdapter<E> extends BaseAdapter {

	protected List<E>			mItems;
	protected LayoutInflater	mInflater;

	/**
	 * Adds the item.
	 *
	 * @param item the item
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void addItem(E item) {
		if (mItems == null) {
			mItems = new ArrayList<E>();
		}
		if (mItems.contains(item)) {
			return;
		}
		mItems.add(item);
	}

	/**
	 * Sets the items.
	 *
	 * @param items the new items
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setItems(List<E> items) {
		this.mItems = items;
		notifyDataSetChanged();
	}

	/**
	 * Gets the items.
	 *
	 * @return the items
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public List<E> getItems() {
		return mItems;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return mItems == null ? 0 :mItems.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public E getItem(int position) {
		return mItems.get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	

}
