/*
 * Copyright (C) 2012 dvbviewer-controller Project
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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import org.dvbviewer.controller.R;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class CategoryAdapter.
 *
 * @author RayBa
 * @date 01.07.2012
 */
public class CategoryAdapter extends BaseAdapter {
	public final Map<String, Adapter>	sections			= new LinkedHashMap<String, Adapter>();
	public final ArrayAdapter<String>	headers;
	public final static int				TYPE_SECTION_HEADER	= 0;

	/**
	 * Instantiates a new category adapter.
	 *
	 * @param context the context
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public CategoryAdapter(Context context) {
		headers = new ArrayAdapter<String>(context, R.layout.preference_category, android.R.id.title);
	}

	/**
	 * Adds the section.
	 *
	 * @param section the section
	 * @param adapter the adapter
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void addSection(String section, Adapter adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int position) {
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return section;
			if (position < size)
				return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : this.sections.values())
			total += adapter.getCount() + 1;
		return total;
	}

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (Adapter adapter : this.sections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#getItemViewType(int)
	 */
	@Override
	public int getItemViewType(int position) {
		int type = 1;
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return TYPE_SECTION_HEADER;
			if (position < size)
				return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	/**
	 * Are all items selectable.
	 *
	 * @return true, if successful
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public boolean areAllItemsSelectable() {
		return false;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see android.widget.BaseAdapter#isEnabled(int)
	 */
	@Override
	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return headers.getView(sectionnum, convertView, parent);
			if (position < size)
				return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

}
