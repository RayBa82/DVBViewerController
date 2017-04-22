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

import org.dvbviewer.controller.data.DbConsts;

import java.util.LinkedList;
import java.util.List;


/**
 * The Class MediaFile.
 * 
 */
public class MediaFile {

	/** The dirId. */
	private Long id;

	/** The dirId. */
	private Long dirId;
	
	/** The name. */
	private String			name;

	/** The position. */
	private Long			parent;

	private List<MediaFile> children = new LinkedList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDirId() {
		return dirId;
	}

	public void setDirId(Long dirId) {
		this.dirId = dirId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getParent() {
		return parent;
	}

	public void setParent(Long position) {
		this.parent = position;
	}

	public List<MediaFile> getChildren() {
		return children;
	}

	public void setChildren(List<MediaFile> children) {
		this.children = children;
	}

	public ContentValues toContentValues() {
		ContentValues result = new ContentValues();
		if (this.dirId != null && !this.dirId.equals(0l)) {
			result.put(DbConsts.MediaTbl._ID, dirId);
		}
		if (this.name != null && !TextUtils.isEmpty(this.name)) {
			result.put(DbConsts.MediaTbl.NAME, this.name);
		}
		if (this.parent != null) {
			result.put(DbConsts.MediaTbl.PARENT, this.parent);
		}
		return result;
	}

}
