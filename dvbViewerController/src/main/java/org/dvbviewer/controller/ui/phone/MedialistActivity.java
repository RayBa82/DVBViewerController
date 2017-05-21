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
package org.dvbviewer.controller.ui.phone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.MediaFile;
import org.dvbviewer.controller.entities.VideoFile;
import org.dvbviewer.controller.ui.adapter.MediaAdapter;
import org.dvbviewer.controller.ui.adapter.VideoAdapter;
import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.MediaList;
import org.dvbviewer.controller.ui.fragments.StreamConfig;
import org.dvbviewer.controller.ui.fragments.VideoList;
import org.dvbviewer.controller.utils.FileType;

/**
 * The Class TimerlistActivity.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class MedialistActivity extends BaseSinglePaneActivity implements MediaAdapter.OnMediaClickListener, VideoAdapter.OnVideoClickListener{
	
	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDisplayHomeAsUpEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			onBackPressed();
			return true;

		default:
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreatePane()
	 */
	@Override
	protected Fragment onCreatePane() {
		final MediaList mediaList = new MediaList();
		mediaList.setArguments(intentToFragmentArguments(getIntent()));
		return mediaList;
	}

	@Override
	public void onMediaClick(MediaFile mediaFile) {
		if(mediaFile.getDirId() <= 0) {
			final Bundle b = new Bundle();
			b.putLong(MediaList.KEY_PARENT_ID, mediaFile.getId());
			final MediaList mediaList = new MediaList();
			mediaList.setArguments(b);
			changeFragment(R.id.root_container, mediaList);
		}else {
			final Bundle b = new Bundle();
			b.putLong(VideoList.KEY_DIR_ID, mediaFile.getDirId());
			final VideoList videoList = new VideoList();
			videoList.setArguments(b);
			changeFragment(R.id.root_container, videoList);
		}
	}

	@Override
	public void onVideoClick(VideoFile videoFile) {
		Bundle arguments = new Bundle();
		arguments.putLong(StreamConfig.EXTRA_FILE_ID, videoFile.getId());
		arguments.putParcelable(StreamConfig.EXTRA_FILE_TYPE, FileType.VIDEO);
		arguments.putInt(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig);
		arguments.putString(StreamConfig.EXTRA_TITLE, videoFile.getTitle());
		Intent streamConfig = new Intent(this, StreamConfigActivity.class);
		streamConfig.putExtras(arguments);
		startActivity(streamConfig);
	}
}