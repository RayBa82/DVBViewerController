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
import android.view.MenuItem;

import androidx.fragment.app.Fragment;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.media.MediaFile;
import org.dvbviewer.controller.ui.adapter.MediaAdapter;
import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.MediaList;
import org.dvbviewer.controller.ui.fragments.StreamConfig;
import org.dvbviewer.controller.utils.AnalyticsTracker;
import org.dvbviewer.controller.utils.FileType;
import org.dvbviewer.controller.utils.StreamUtils;

/**
 * The Class TimerlistActivity.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class MedialistActivity extends BaseSinglePaneActivity implements MediaAdapter.OnMediaClickListener{
	
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
		mediaList.setArguments(Companion.intentToFragmentArguments(getIntent()));
		return mediaList;
	}

	@Override
	public void onMediaClick(MediaFile mediaFile) {
		if(mediaFile.getDirId() > 0) {
			final Bundle b = new Bundle();
			b.putLong(MediaList.Companion.getKEY_PARENT_ID(), mediaFile.getDirId());
			final MediaList mediaList = new MediaList();
			mediaList.setArguments(b);
			changeFragment(R.id.root_container, mediaList);
		}else {
			Bundle arguments = new Bundle();
			arguments.putLong(StreamConfig.Companion.getEXTRA_FILE_ID(), mediaFile.getId());
			arguments.putParcelable(StreamConfig.Companion.getEXTRA_FILE_TYPE(), FileType.VIDEO);
			arguments.putInt(StreamConfig.Companion.getEXTRA_DIALOG_TITLE_RES(), R.string.streamConfig);
			arguments.putString(StreamConfig.Companion.getEXTRA_TITLE(), mediaFile.getName());
			Intent streamConfig = new Intent(this, StreamConfigActivity.class);
			streamConfig.putExtras(arguments);
			startActivity(streamConfig);
		}
	}

	@Override
	public void onMediaStreamClick(MediaFile mediaFile) {
		final Intent videoIntent = StreamUtils.buildQuickUrl(this, mediaFile.getId(), mediaFile.getName(), FileType.VIDEO);
		startActivity(videoIntent);
		AnalyticsTracker.trackMediaStream(getApplication());
	}

	@Override
	public void onMediaContextClick(MediaFile mediaFile) {

	}

}