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
package org.dvbviewer.controller.ui.tablet;

import java.util.Date;
import java.util.List;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.ui.base.BaseActivity;
import org.dvbviewer.controller.ui.base.BaseMultiPaneActivity;
import org.dvbviewer.controller.ui.fragments.ChannelEpg;
import org.dvbviewer.controller.ui.fragments.ChannelEpg.EpgDateInfo;
import org.dvbviewer.controller.ui.fragments.ChannelList;
import org.dvbviewer.controller.ui.fragments.ChannelList.OnChannelSelectedListener;
import org.dvbviewer.controller.ui.fragments.EpgPager;

import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;

/**
 * The Class ChannelListMultiActivity.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class ChannelListMultiActivity extends BaseMultiPaneActivity implements EpgDateInfo, OnChannelSelectedListener, OnPageChangeListener {
	
	Date epgDate = new Date();
	private EpgPager	mEpgPager;
	private ChannelList	mChannelList;

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		epgDate = savedInstanceState != null && savedInstanceState.containsKey(ChannelEpg.KEY_EPG_DAY) ? new Date(savedInstanceState.getLong(ChannelEpg.KEY_EPG_DAY)) : new Date();
		setContentView(R.layout.activity_channel_list);
		if (savedInstanceState == null) {
			int position = getIntent().getExtras().getInt(ChannelList.KEY_SELECTED_POSITION);
			mChannelList = new ChannelList();
			mChannelList.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
			mChannelList.setSelectedPosition(position);
			mEpgPager = new EpgPager();
			mEpgPager.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
			mEpgPager.setPosition(position);
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_channel_list, mChannelList, ChannelList.class.getName()).commit();
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_channel_epg, mEpgPager, EpgPager.class.getName()).commit();
		}else {
			mChannelList = (ChannelList) getSupportFragmentManager().findFragmentByTag(ChannelList.class.getName());
			mEpgPager = (EpgPager) getSupportFragmentManager().findFragmentByTag(EpgPager.class.getName());
		}
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.fragments.ChannelEpg.EpgDateInfo#setEpgDate(java.util.Date)
	 */
	@Override
	public void setEpgDate(Date epgDate) {
		this.epgDate = epgDate;
	}


	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.fragments.ChannelEpg.EpgDateInfo#getEpgDate()
	 */
	@Override
	public Date getEpgDate() {
		return epgDate;
	}
	
	
	/* (non-Javadoc)
 * @see org.dvbviewer.controller.ui.base.BaseActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
 */
@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.menuFavourties:
		case R.id.menuChannelList:
		case R.id.menuRefreshChannels:
			mChannelList.onOptionsItemSelected(item);
			return true;
		case R.id.menuRefresh:
			mEpgPager.onOptionsItemSelected(item);
			mChannelList.refresh(ChannelList.LOADER_EPG);
			break;
		case R.id.menuPrev:
			mEpgPager.onOptionsItemSelected(item);
			break;
		case R.id.menuNext:
			mEpgPager.onOptionsItemSelected(item);
			break;
		case R.id.menuToday:
			mEpgPager.onOptionsItemSelected(item);
			break;
		case R.id.menuNow:
			mEpgPager.onOptionsItemSelected(item);
			break;
		case R.id.menuEvening:
			mEpgPager.onOptionsItemSelected(item);
			break;

		default:
			return false;
		}
		supportInvalidateOptionsMenu();
		return true;
	}

	
	/* (non-Javadoc)
 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onSaveInstanceState(android.os.Bundle)
 */
@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ChannelEpg.KEY_EPG_DAY, epgDate.getTime());
		
	}
	
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		epgDate = savedInstanceState != null && savedInstanceState.containsKey(ChannelEpg.KEY_EPG_DAY) ? new Date(savedInstanceState.getLong(ChannelEpg.KEY_EPG_DAY)) : new Date();
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.fragments.ChannelList.OnChannelSelectedListener#channelSelected(java.util.List, org.dvbviewer.controller.entities.Channel, int)
	 */
	@Override
	public void channelSelected(List<Channel> chans, Channel chan, int position) {
		mEpgPager.setPosition(position);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)
	 */
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
	 */
	@Override
	public void onPageSelected(int position) {
		mChannelList.setSelection(position);
		
	}

}
