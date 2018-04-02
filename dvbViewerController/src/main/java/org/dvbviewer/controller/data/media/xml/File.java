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
package org.dvbviewer.controller.data.media.xml;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * The Class Task.
 *
 * @author RayBa
 * @date 01.07.2012
 */
@Root(name = "file")
public class File {

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "title")
    private String title;

    @Attribute(name = "objid")
    private long objid;

    @Attribute(name = "dur")
    private int duration;

    @Attribute(name = "hres")
    private int hres;

    @Attribute(name = "vres")
    private int vres;

    @Element(name = "thumb", required = false)
    private String thumb;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getObjid() {
        return objid;
    }

    public void setObjid(long objid) {
        this.objid = objid;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getHres() {
        return hres;
    }

    public void setHres(int hres) {
        this.hres = hres;
    }

    public int getVres() {
        return vres;
    }

    public void setVres(int vres) {
        this.vres = vres;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
