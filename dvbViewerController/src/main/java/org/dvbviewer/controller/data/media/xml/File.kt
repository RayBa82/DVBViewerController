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
package org.dvbviewer.controller.data.media.xml


import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

/**
 * The Class Task.
 *
 * @author RayBa
 * @date 01.07.2012
 */
@Xml(name = "file")
class File {

    @Attribute(name = "name")
    var name: String? = null

    @Attribute(name = "title")
    var title: String? = null

    @Attribute(name = "objid")
    var objid: Long = 0

    @Attribute(name = "dur")
    var duration: Int = 0

    @Attribute(name = "hres")
    var hres: Int = 0

    @Attribute(name = "vres")
    var vres: Int = 0

    @PropertyElement(name = "thumb")
    var thumb: String? = null
}
