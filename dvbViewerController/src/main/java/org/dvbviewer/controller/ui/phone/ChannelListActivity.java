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

import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.ChannelList;

/**
 * The Class ChannelListActivity.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class ChannelListActivity extends BaseSinglePaneActivity implements ChannelList.OnChannelSelectedListener{
	
	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreatePane()
	 */
	@Override
	protected Fragment onCreatePane() {
		ChannelList chans = new ChannelList();
		Bundle args = new Bundle();
		args.putBoolean(ChannelList.KEY_HAS_OPTIONMENU, true);
		chans.setArguments(args);
		return chans;
	}

	@Override
	public void channelSelected(long groupId, int groupIndex, Channel chan, int channelIndex) {
		Intent epgPagerIntent = new Intent(this, EpgPagerActivity.class);
		epgPagerIntent.putExtra(ChannelList.KEY_GROUP_ID, groupId);
		epgPagerIntent.putExtra(ChannelList.KEY_CHANNEL_INDEX, channelIndex);
		startActivity(epgPagerIntent);
	}
}