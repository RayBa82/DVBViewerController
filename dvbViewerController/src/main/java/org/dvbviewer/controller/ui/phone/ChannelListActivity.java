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
import android.view.View;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.ui.base.BaseActivity;
import org.dvbviewer.controller.ui.fragments.ChannelEpg;
import org.dvbviewer.controller.ui.fragments.ChannelList;
import org.dvbviewer.controller.ui.fragments.ChannelPager;
import org.dvbviewer.controller.ui.fragments.EpgPager;

import java.util.Date;

/**
 * The Class ChannelListActivity.
 *
 * @author RayBa
 */
public class ChannelListActivity extends BaseActivity implements ChannelEpg.EpgDateInfo, ChannelList.OnChannelSelectedListener, ChannelPager.OnGroupChangedListener, EpgPager.OnChannelScrolledListener {

	private Date epgDate = new Date();
	private EpgPager	mEpgPager;
	private ChannelPager mChannelPager;
	private View container;

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDisplayHomeAsUpEnabled(true);
		epgDate = savedInstanceState != null && savedInstanceState.containsKey(ChannelEpg.KEY_EPG_DAY) ? new Date(savedInstanceState.getLong(ChannelEpg.KEY_EPG_DAY)) : new Date();
		setContentView(R.layout.activity_channel_list);
		container = findViewById(R.id.fragment_container_channel_epg);
		initFragments(savedInstanceState);
	}

	private void initFragments(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			mChannelPager = new ChannelPager();
			mChannelPager.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_channel_list, mChannelPager, ChannelList.class.getName()).commit();
			if (container != null){
				mEpgPager = new EpgPager();
				mEpgPager.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
				getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_channel_epg, mEpgPager, EpgPager.class.getName()).commit();
			}
		}else {
			mChannelPager = (ChannelPager) getSupportFragmentManager().findFragmentByTag(ChannelList.class.getName());
			if (container != null){
				mEpgPager = (EpgPager) getSupportFragmentManager().findFragmentByTag(EpgPager.class.getName());
			}
		}
	}

	@Override
	public void channelSelected(long groupId, int groupIndex, int channelIndex) {
		mChannelPager.updateIndex(groupIndex, channelIndex);
		if (container == null){
			Intent epgPagerIntent = new Intent(this, EpgPagerActivity.class);
			epgPagerIntent.putExtra(ChannelPager.KEY_GROUP_ID, groupId);
			epgPagerIntent.putExtra(ChannelList.KEY_CHANNEL_INDEX, channelIndex);
			startActivity(epgPagerIntent);
		}else{
			mEpgPager.setPosition(channelIndex);
		}
	}

	@Override
	public void setEpgDate(long date) {
		epgDate = new Date(date);
	}

	@Override
	public long getEpgDate() {
		return epgDate.getTime();
	}

	@Override
	public void groupChanged(long groupId, int channelIndex) {
		if (mEpgPager != null){
			mEpgPager.refresh(groupId, channelIndex);
		}

	}

	@Override
	public void channelChanged(long groupId, int channelIndex) {
		mChannelPager.setChannelSelection(groupId, channelIndex);
	}

}