/*
 * Copyright (C) 2012 dvbviewer-controller Project
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
package org.dvbviewer.controller.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.ui.base.BaseActivity;
import org.dvbviewer.controller.ui.fragments.ChannelList;
import org.dvbviewer.controller.ui.fragments.ChannelList.OnChannelSelectedListener;
import org.dvbviewer.controller.ui.fragments.Dashboard.OnDashboardButtonClickListener;
import org.dvbviewer.controller.ui.fragments.EpgPager;
import org.dvbviewer.controller.ui.fragments.RecordingList;
import org.dvbviewer.controller.ui.fragments.Remote;
import org.dvbviewer.controller.ui.fragments.TimerList;
import org.dvbviewer.controller.ui.phone.AboutActivity;
import org.dvbviewer.controller.ui.phone.ChannelListActivity;
import org.dvbviewer.controller.ui.phone.PreferencesActivity;
import org.dvbviewer.controller.ui.phone.RecordinglistActivity;
import org.dvbviewer.controller.ui.phone.RemoteActivity;
import org.dvbviewer.controller.ui.phone.StatusActivity;
import org.dvbviewer.controller.ui.phone.TaskActivity;
import org.dvbviewer.controller.ui.phone.TimerlistActivity;
import org.dvbviewer.controller.ui.tablet.ChannelListMultiActivity;
import org.dvbviewer.controller.utils.Config;

import java.util.List;

/**
 * The Class HomeActivity.
 *
 * @author RayBa
 */
public class HomeActivity extends BaseActivity implements OnClickListener, OnChannelSelectedListener, OnDashboardButtonClickListener, Remote.OnTargetsChangedListener {

	private View		multiContainer;
	private ArrayAdapter mSpinnerAdapter;
	private Spinner mClientSpinner;
	private int spinnerPosition;
	DVBViewerPreferences prefs;

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		multiContainer = findViewById(R.id.multi_container);
		prefs = new DVBViewerPreferences(this);
		
		if (savedInstanceState == null) {
			if (multiContainer != null) {
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
				ChannelList chans = new ChannelList();
				chans.setHasOptionsMenu(true);
				tran.add(multiContainer.getId(), chans);
				tran.commit();
				setTitle(R.string.channelList);
			}
			if (Config.IS_FIRST_START) {
				Config.IS_FIRST_START = false;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getResources().getString(R.string.firstStartMessage)).setPositiveButton(R.string.yes, this).setTitle(getResources().getString(R.string.firstStartMessageTitle))
				.setNegativeButton(R.string.no, this).show();
				prefs = new DVBViewerPreferences(this);
				prefs.getPrefs().edit().putBoolean(DVBViewerPreferences.KEY_IS_FIRST_START, false).commit();
			}
		}

		initRemoteSpinner();
	}

	private void initRemoteSpinner() {
		mClientSpinner = (Spinner) findViewById(R.id.clientSpinner);
		if (mClientSpinner != null){
			mClientSpinner.setVisibility(View.GONE);
			mClientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedClient = (String) mSpinnerAdapter.getItem(position);
					prefs.getPrefs().edit().putString(DVBViewerPreferences.KEY_SELECTED_CLIENT, selectedClient).commit();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
		}
	}


	/* (non-Javadoc)
         * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
         */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			Intent settings = new Intent(HomeActivity.this, PreferencesActivity.class);
			startActivity(settings);
			break;
		default:
			finish();
			break;
		}
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.fragments.Dashboard.OnDashboardButtonClickListener#onDashboarButtonClick(android.view.View)
	 */
	@Override
	public void onDashboarButtonClick(View v) {
		switch (v.getId()) {
		case R.id.home_btn_remote:
			if (multiContainer != null) {
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
				tran.replace(multiContainer.getId(), new Remote());
				tran.commit();
				setTitle(R.string.remote);
			} else {
				startActivity(new Intent(this, RemoteActivity.class));
			}
			break;
		case R.id.home_btn_channels:
			if (multiContainer != null) {
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
				ChannelList chans = new ChannelList();
				chans.setHasOptionsMenu(true);
				tran.replace(multiContainer.getId(), chans);
				tran.commit();
				setTitle(R.string.channelList);
			} else {
				startActivity(new Intent(this, ChannelListActivity.class));
			}
			break;
		case R.id.home_btn_timers:
			if (multiContainer != null) {
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
				tran.replace(multiContainer.getId(), new TimerList());
				tran.commit();
			} else {

				startActivity(new Intent(this, TimerlistActivity.class));
			}
			break;
		case R.id.home_btn_recordings:
			if (multiContainer != null) {
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
				tran.replace(multiContainer.getId(), new RecordingList());
				tran.commit();
				setTitle(R.string.recordings);
			} else {
				startActivity(new Intent(this, RecordinglistActivity.class));
			}
			break;
		case R.id.home_btn_settings:
			startActivity(new Intent(this, PreferencesActivity.class));
			break;
		case R.id.home_btn_tasks:
			startActivity(new Intent(this, TaskActivity.class));
			break;
		case R.id.home_btn_status:
			startActivity(new Intent(this, StatusActivity.class));
			break;

		default:
			break;
		}
		if (mClientSpinner != null){
			mClientSpinner.setVisibility(View.GONE);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuAbout:
			startActivity(new Intent(this, AboutActivity.class));
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void channelSelected(long groupId, int groupIndex, Channel chan, int channelIndex) {
		Intent channelListIntent = new Intent(this, ChannelListMultiActivity.class);
		channelListIntent.putExtra(ChannelList.KEY_GROUP_ID, groupId);
		channelListIntent.putExtra(EpgPager.KEY_HIDE_OPTIONSMENU, true);
		channelListIntent.putExtra(ChannelList.KEY_CHANNEL_INDEX, channelIndex);
		startActivity(channelListIntent);

	}

	@Override
	public void targetsChanged(String title, List<String> spinnerData) {
		setTitle(title);
		if (mClientSpinner != null){
			String[] arr = new String[spinnerData.size()];
			mSpinnerAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spinnerData.toArray(arr));
			mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
			mClientSpinner.setAdapter(mSpinnerAdapter);
			String activeClient = prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT);
			int index = spinnerData.indexOf(activeClient);
			spinnerPosition = index > Spinner.INVALID_POSITION ? index : Spinner.INVALID_POSITION;
			mClientSpinner.setSelection(spinnerPosition);
			mClientSpinner.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public Object getSelectedTarget() {
		return mClientSpinner.getSelectedItem();
	}


}
