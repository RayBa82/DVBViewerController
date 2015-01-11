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



/**
 * The Class Task.
 *
 * @author RayBa
 * @date 01.07.2012
 */
public class Task {
	
	/**
	 * Instantiates a new task.
	 *
	 * @param title the title
	 * @param command the command
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public Task(String title, String command){
		this.title = title;
		this.command = command;
	}
	
	/** The title. */
	private String title;
	
	/** The command. */
	private String command;
	
	/**
	 * Gets the title.
	 *
	 * @return the title
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Gets the command.
	 *
	 * @return the command
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public String getCommand() {
		return command;
	}
	
	/**
	 * Sets the command.
	 *
	 * @param command the new command
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public void setCommand(String command) {
		this.command = command;
	}

}
