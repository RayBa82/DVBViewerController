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
package org.dvbviewer.controller.data.task.xml;


import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.PropertyElement;
import com.tickaroo.tikxml.annotation.Xml;

/**
 * The Class Task.
 *
 * @author RayBa
 * @date 01.07.2012
 */
@Xml(name = "task")
public class Task {

    @Attribute(name = "type")
    private String type;

	@PropertyElement
	private String name;
	
    @PropertyElement
	private String action;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}

}