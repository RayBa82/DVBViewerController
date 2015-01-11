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
package org.dvbviewer.controller.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Status.
 *
 * @author RayBa
 * @date 01.07.2012
 */
public class Status {

	private String				version;

	private int					recordCount;

	private int					clientCount;

	private int					epgUdate;

	private int					epgBefore	= 5;

	private int					epgAfter	= 5;

	private int					timeZone;

	private int					defAfterRecord;

	private List<StatusItem>	items;

	private List<Folder>		folders		= new ArrayList<Status.Folder>();

	/**
	 * Gets the version.
	 *
	 * @return the version
	 * @author RayBa
	 * @date 01.11.2012
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version.
	 *
	 * @param version the new version
	 * @author RayBa
	 * @date 01.11.2012
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Gets the record count.
	 *
	 * @return the record count 
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public int getRecordCount() {
		return recordCount;
	}

	/**
	 * Sets the record count.
	 *
	 * @param recordCount the new record count
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	/**
	 * Gets the client count.
	 *
	 * @return the client count
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public int getClientCount() {
		return clientCount;
	}

	/**
	 * Sets the client count.
	 *
	 * @param clientCount the new client count
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setClientCount(int clientCount) {
		this.clientCount = clientCount;
	}

	/**
	 * Gets the epg udate.
	 *
	 * @return the epg udate
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public int getEpgUdate() {
		return epgUdate;
	}

	/**
	 * Sets the epg udate.
	 *
	 * @param epgUdate the new epg udate
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setEpgUdate(int epgUdate) {
		this.epgUdate = epgUdate;
	}

	/**
	 * Gets the epg before.
	 *
	 * @return the epg before
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public int getEpgBefore() {
		return epgBefore;
	}

	/**
	 * Sets the epg before.
	 *
	 * @param epgBefore the new epg before
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setEpgBefore(int epgBefore) {
		this.epgBefore = epgBefore;
	}

	/**
	 * Gets the epg after.
	 *
	 * @return the epg after
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public int getEpgAfter() {
		return epgAfter;
	}

	/**
	 * Sets the epg after.
	 *
	 * @param epgAfter the new epg after
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setEpgAfter(int epgAfter) {
		this.epgAfter = epgAfter;
	}

	/**
	 * Gets the time zone.
	 *
	 * @return the time zone
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public int getTimeZone() {
		return timeZone;
	}

	/**
	 * Sets the time zone.
	 *
	 * @param timeZone the new time zone
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setTimeZone(int timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * Gets the def after record.
	 *
	 * @return the def after record
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public int getDefAfterRecord() {
		return defAfterRecord;
	}

	/**
	 * Sets the def after record.
	 *
	 * @param defAfterRecord the new def after record
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setDefAfterRecord(int defAfterRecord) {
		this.defAfterRecord = defAfterRecord;
	}

	/**
	 * Gets the folders.
	 *
	 * @return the folders
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public List<Folder> getFolders() {
		return folders;
	}

	/**
	 * Gets the items.
	 *
	 * @return the items
	 * @author RayBa
	 * @date 01.11.2012
	 */
	public List<StatusItem> getItems() {
		return items;
	}

	/**
	 * Sets the items.
	 *
	 * @param items the new items
	 * @author RayBa
	 * @date 01.11.2012
	 */
	public void setItems(List<StatusItem> items) {
		this.items = items;
	}

	/**
	 * Sets the folders.
	 *
	 * @param folders the new folders
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setFolders(List<Folder> folders) {
		this.folders = folders;
	}

	/**
	 * The Class StatusItem.
	 *
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public static class StatusItem {

		/** The name ressource. */
		int		nameRessource;

		/** The value. */
		String	value;

		/**
		 * Gets the name.
		 *
		 * @return the name
		 * @author RayBa
		 * @date 05.07.2012
		 */
		public int getNameRessource() {
			return nameRessource;
		}

		/**
		 * Sets the name.
		 *
		 * @param nameRessource the new name ressource
		 * @author RayBa
		 * @date 05.07.2012
		 */
		public void setNameRessource(int nameRessource) {
			this.nameRessource = nameRessource;
		}

		/**
		 * Gets the value.
		 *
		 * @return the value
		 * @author RayBa
		 * @date 05.07.2012
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Sets the value.
		 *
		 * @param value the new value
		 * @author RayBa
		 * @date 05.07.2012
		 */
		public void setValue(String value) {
			this.value = value;
		}

	}

	/**
	 * The Class Folder.
	 *
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public static class Folder {

		/** The path. */
		String	path;

		/** The size. */
		long	size;

		/** The free. */
		long	free;

		/**
		 * Gets the path.
		 *
		 * @return the path
		 * @author RayBa
		 * @date 01.07.2012
		 */
		public String getPath() {
			return path;
		}

		/**
		 * Sets the path.
		 *
		 * @param path the new path
		 * @author RayBa
		 * @date 01.07.2012
		 */
		public void setPath(String path) {
			this.path = path;
		}

		/**
		 * Gets the size.
		 *
		 * @return the size
		 * @author RayBa
		 * @date 01.07.2012
		 */
		public long getSize() {
			return size;
		}

		/**
		 * Sets the size.
		 *
		 * @param size the new size
		 * @author RayBa
		 * @date 01.07.2012
		 */
		public void setSize(long size) {
			this.size = size;
		}

		/**
		 * Gets the free.
		 *
		 * @return the free
		 * @author RayBa
		 * @date 01.07.2012
		 */
		public long getFree() {
			return free;
		}

		/**
		 * Sets the free.
		 *
		 * @param freed the new free
		 * @author RayBa
		 * @date 01.07.2012
		 */
		public void setFree(long freed) {
			this.free = freed;
		}

	}

}
