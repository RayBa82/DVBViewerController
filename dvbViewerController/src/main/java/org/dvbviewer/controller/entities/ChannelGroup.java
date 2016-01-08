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

import org.dvbviewer.controller.data.DbConsts.GroupTbl;
import org.dvbviewer.controller.entities.Channel.Fav;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Group.
 *
 * @author RayBa
 * @date 24.08.2013
 */
public class ChannelGroup {
	
	/** Type channel group */
	public static final int	TYPE_CHAN	= 0;
	
	/** Type favourite group. */
	public static final int	TYPE_FAV	= 1;

	/** The id. */
	private Long			id = null;

	/** The root id. */
	private Long			rootId;

	/** The name. */
	private String			name;

	/** The type. */
	private int				type		= TYPE_CHAN;

	/** The channels. */
	private List<Channel>	channels	= new ArrayList<Channel>();
	
	/** The favs. */
	private List<Fav>		favs		= new ArrayList<Fav>();

	/**
	 * Gets the id.
	 *
	 * @return the id
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Gets the root id.
	 *
	 * @return the root id
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public Long getRootId() {
		return rootId;
	}

	/**
	 * Sets the root id.
	 *
	 * @param rootId the new root id
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public void setRootId(Long rootId) {
		this.rootId = rootId;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the channels.
	 *
	 * @return the channels
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public List<Channel> getChannels() {
		return channels;
	}

	/**
	 * Sets the channels.
	 *
	 * @param channels the new channels
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 * @author RayBa
	 * @date 25.08.2013
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 * @author RayBa
	 * @date 25.08.2013
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * To content values.
	 *
	 * @return the content values�
	 * @author RayBa
	 * @date 24.08.2013
	 */
	public ContentValues toContentValues() {
		ContentValues result = new ContentValues();
		if (this.id != null && !this.id.equals(0l)) {
			result.put(GroupTbl._ID, id);
		}
		if (this.rootId != null && !this.rootId.equals(0l)) {
			result.put(GroupTbl.ROOT_ID, rootId);
		}
		if (this.name != null && !TextUtils.isEmpty(this.name)) {
			result.put(GroupTbl.NAME, this.name);
		}
		result.put(GroupTbl.TYPE, type);
		return result;
	}

	public List<Fav> getFavs() {
		return favs;
	}

	public void setFavs(List<Fav> favs) {
		this.favs = favs;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ChannelGroup){
			ChannelGroup cp = (ChannelGroup) o;
			if (id == null){
				return name.equals(cp.getName());
			}else {
				return id == cp.getId();
			}
		}else{
			return false;
		}
	}

	@Override
	public int hashCode() {
		if (id == null){
			return name.hashCode();
		}else {
			return String.valueOf(id).hashCode();
		}
	}
}
