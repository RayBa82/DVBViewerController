/*
 * Copyright © 2015 dvbviewer-controller Project
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
package org.dvbviewer.controller.entities;

import android.content.ContentValues;
import android.text.TextUtils;

import org.dvbviewer.controller.data.ProviderConsts.RootTbl;

import java.util.ArrayList;
import java.util.List;

public class ChannelRoot {

	private Long			id;

	private String			name;

	private List<ChannelGroup>	groups	= new ArrayList<ChannelGroup>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<ChannelGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<ChannelGroup> categories) {
		this.groups = categories;
	}
	
	public ContentValues toContentValues() {
		ContentValues result = new ContentValues();
		if (this.id != null && !this.id.equals(0l)) {
			result.put(RootTbl._ID, id);
		}
		if (this.name != null && !TextUtils.isEmpty(this.name)) {
			result.put(RootTbl.NAME, this.name);
		}
		return result;
	}

}
