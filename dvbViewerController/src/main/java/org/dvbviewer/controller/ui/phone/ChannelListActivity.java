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
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.content.Loader;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.activitiy.base.GroupDrawerActivity;
import org.dvbviewer.controller.data.ProviderConsts;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.IEPG;
import org.dvbviewer.controller.ui.base.BaseActivity;
import org.dvbviewer.controller.ui.fragments.ChannelList;
import org.dvbviewer.controller.ui.fragments.ChannelPager;
import org.dvbviewer.controller.ui.fragments.EPGDetails;
import org.dvbviewer.controller.ui.fragments.EpgPager;

/**
 * The Class ChannelListActivity.
 *
 * @author RayBa
 */
public class ChannelListActivity extends GroupDrawerActivity implements ChannelList.OnChannelSelectedListener, EpgPager.OnChannelScrolledListener, ChannelPager.OnGroupTypeChangedListener, IEpgDetailsActivity.OnIEPGClickListener {

	private 	ChannelPager 			mChannelPager;
	private 	View 					container;
	private 	boolean 				groupTypeChanged;

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_drawer);
		super.onCreate(savedInstanceState);
		container = findViewById(R.id.right_content);
		initFragments(savedInstanceState);
	}

	private void initFragments(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			mChannelPager = new ChannelPager();
			mChannelPager.setArguments(BaseActivity.Companion.intentToFragmentArguments(getIntent()));
			getSupportFragmentManager().beginTransaction()
					.add(R.id.left_content, mChannelPager, Companion.getCHANNEL_PAGER_TAG())
					.commit();
			if (container != null){
				setMEpgPager(new EpgPager());
				getMEpgPager().setArguments(BaseActivity.Companion.intentToFragmentArguments(getIntent()));
				getSupportFragmentManager().beginTransaction()
						.add(R.id.right_content, getMEpgPager(), Companion.getEPG_PAGER_TAG())
						.commit();
			}
		}else {
			mChannelPager = (ChannelPager) getSupportFragmentManager().findFragmentByTag(Companion.getCHANNEL_PAGER_TAG());
			if (container != null){
				setMEpgPager((EpgPager) getSupportFragmentManager().findFragmentByTag(Companion.getEPG_PAGER_TAG()));
			}
		}
	}

	@Override
	public void channelSelected(long groupId, int groupIndex, int channelIndex) {
		mChannelPager.updateIndex(groupIndex, channelIndex);
		if (container == null){
			Intent epgPagerIntent = new Intent(this, EpgPagerActivity.class);
			epgPagerIntent.putExtra(ChannelPager.Companion.getKEY_GROUP_ID(), groupId);
			epgPagerIntent.putExtra(ChannelPager.Companion.getKEY_GROUP_INDEX(), groupIndex);
			epgPagerIntent.putExtra(ChannelList.Companion.getKEY_CHANNEL_INDEX(), channelIndex);
			startActivity(epgPagerIntent);
		}else{
			getMEpgPager().setPosition(channelIndex);
		}
	}


	@Override
	public void channelChanged(long groupId, int channelIndex) {
		mChannelPager.setChannelSelection(groupId, channelIndex);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		super.onItemClick(parent, view, position, id);
		mChannelPager.setPosition(position);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		if (mChannelPager != null){
			result = mChannelPager.onOptionsItemSelected(item);
		}
		if (getMEpgPager() != null){
			result = getMEpgPager().onOptionsItemSelected(item);
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		super.onLoadFinished(loader, data);
		if (container == null){
			Fragment f = getSupportFragmentManager().findFragmentByTag(Companion.getCHANNEL_PAGER_TAG());
			if (f == null) {
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
				mChannelPager = new ChannelPager();
				tran.add(R.id.content_frame, mChannelPager, Companion.getCHANNEL_PAGER_TAG());
				tran.commitAllowingStateLoss();
			} else {
				mChannelPager = (ChannelPager) f;
			}
		}
		if (container != null && groupTypeChanged){
			data.moveToFirst();
			getMEpgPager().refresh(data.getLong(data.getColumnIndex(ProviderConsts.GroupTbl._ID)), 0);
		}
		groupTypeChanged = false;
	}

	@Override
	public void groupTypeChanged(int type) {
		groupTypeChanged = true;
		setShowFavs(getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false));
		getSupportLoaderManager().restartLoader(0, getIntent().getExtras(), this);
		setGroupIndex(0);
	}

	@Override
	public void onIEPGClick(IEPG iepg) {
		EPGDetails details = new EPGDetails();
		Bundle bundle = new Bundle();
		bundle.putParcelable(IEPG.class.getSimpleName(), iepg);
		details.setArguments(bundle);
		details.show(getSupportFragmentManager(), IEPG.class.getName());
	}
}