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
package org.dvbviewer.controller.data.entities;


public class Preset {

    private String title = "";

    private String extension;

    private String mimeType;

    private int encodingSpeed = 4;

    private int audioTrack = 0;

    private int subTitle = -1;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getEncodingSpeed() {
        return encodingSpeed;
    }

    public void setEncodingSpeed(int encodingSpeed) {
        this.encodingSpeed = encodingSpeed;
    }

    public int getAudioTrack() {
        return audioTrack;
    }

    public void setAudioTrack(int audioTrack) {
        this.audioTrack = audioTrack;
    }

    public int getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(int subTitle) {
        this.subTitle = subTitle;
    }



    @Override
    public boolean equals(Object o) {
        if (o instanceof Preset){
            Preset comparator = (Preset) o;
            return this.title.equals(comparator.getTitle());
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString() {
        return this.title;
    }

}
