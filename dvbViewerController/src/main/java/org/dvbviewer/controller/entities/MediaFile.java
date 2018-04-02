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

import org.dvbviewer.controller.data.ProviderConsts;


/**
 * The Class MediaFile.
 */
public class MediaFile {

    private Long id;

    private long dirId;

    private String name;

    private String thumb;

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDirId() {
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

    public boolean isDirectory() {
        return dirId > 0l;
    }


    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public ContentValues toContentValues() {
        ContentValues result = new ContentValues();
        if (this.id != null && this.id > 0l) {
            result.put(ProviderConsts.MediaTbl._ID, id);
        }
        if (this.dirId > 0l) {
            result.put(ProviderConsts.MediaTbl.DIR_ID, dirId);
        }
        if (this.name != null && !TextUtils.isEmpty(this.name)) {
            result.put(ProviderConsts.MediaTbl.NAME, this.name);
        }
        return result;
    }

    @Override
    public String toString() {
        return getName().toLowerCase();
    }
}
