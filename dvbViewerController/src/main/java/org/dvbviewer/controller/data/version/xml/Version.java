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
package org.dvbviewer.controller.data.version.xml;


import com.tickaroo.tikxml.annotation.Attribute;
import com.tickaroo.tikxml.annotation.TextContent;
import com.tickaroo.tikxml.annotation.Xml;


/**
 * The Class Task.
 *
 * @author RayBa
 * @date 01.07.2012
 */
@Xml(name = "version")
public class Version {

    @Attribute(name = "iver")
	private int  internalVersion;

    @TextContent
	private String version;

    public int getInternalVersion() {
        return internalVersion;
    }

    public void setInternalVersion(int internalVersion) {
        this.internalVersion = internalVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
