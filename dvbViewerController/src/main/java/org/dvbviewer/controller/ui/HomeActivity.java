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
import org.dvbviewer.controller.activitiy.base.GroupDrawerActivity;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.ui.fragments.ChannelList;
import org.dvbviewer.controller.ui.fragments.ChannelList.OnChannelSelectedListener;
import org.dvbviewer.controller.ui.fragments.ChannelPager;
import org.dvbviewer.controller.ui.fragments.Dashboard;
import org.dvbviewer.controller.ui.fragments.Dashboard.OnDashboardButtonClickListener;
import org.dvbviewer.controller.ui.fragments.RecordingList;
import org.dvbviewer.controller.ui.fragments.Remote;
import org.dvbviewer.controller.ui.fragments.StatusList;
import org.dvbviewer.controller.ui.fragments.TaskList;
import org.dvbviewer.controller.ui.fragments.TimerList;
import org.dvbviewer.controller.ui.phone.AboutActivity;
import org.dvbviewer.controller.ui.phone.ChannelListActivity;
import org.dvbviewer.controller.ui.phone.PreferencesActivity;
import org.dvbviewer.controller.ui.phone.RecordinglistActivity;
import org.dvbviewer.controller.ui.phone.RemoteActivity;
import org.dvbviewer.controller.ui.phone.StatusActivity;
import org.dvbviewer.controller.ui.phone.TaskActivity;
import org.dvbviewer.controller.ui.phone.TimerlistActivity;
import org.dvbviewer.controller.utils.Config;

import java.util.List;

/**
 * The Class HomeActivity.
 *
 * @author RayBa
 */
public class HomeActivity extends GroupDrawerActivity implements OnClickListener, OnChannelSelectedListener, OnDashboardButtonClickListener, Remote.OnTargetsChangedListener {

    public static final String ENABLE_DRAWER = "ENABLE_DRAWER";
	public static final String TITLE 		 = "title";
	private View					multiContainer;
	private ArrayAdapter 			mSpinnerAdapter;
	private Spinner 				mClientSpinner;
	private DVBViewerPreferences 	prefs;
    private ChannelPager chans;
    private boolean enableDrawer;

    /* (non-Javadoc)
     * @see org.dvbviewer.controller.ui.base.BaseActivity#onCreate(android.os.Bundle)
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_home);
		super.onCreate(savedInstanceState);
		multiContainer = findViewById(R.id.right_content);
		prefs = new DVBViewerPreferences(this);
		if (savedInstanceState == null) {
			Dashboard dashboard = new Dashboard();
			FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
			tran.add(R.id.left_content, dashboard);
			tran.commit();
			if (multiContainer != null) {
                enableDrawer = true;
				tran = getSupportFragmentManager().beginTransaction();
                chans = new ChannelPager();
				chans.setHasOptionsMenu(true);
                Bundle bundle = new Bundle();
                bundle.putInt(ChannelPager.KEY_GROUP_INDEX, groupIndex);
                chans.setArguments(bundle);
				tran.add(multiContainer.getId(), chans, CHANNEL_PAGER_TAG);
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
		}else{
            enableDrawer = savedInstanceState.getBoolean(ENABLE_DRAWER, false);
			setTitle(savedInstanceState.getString(TITLE));
        }
		initRemoteSpinner();
        setDrawerEnabled(enableDrawer);
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
				enableDrawer = false;
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
                enableDrawer = true;
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
                chans = new ChannelPager();
				chans.setHasOptionsMenu(true);
                Bundle bundle = new Bundle();
                bundle.putInt(ChannelPager.KEY_GROUP_INDEX, groupIndex);
                chans.setArguments(bundle);
				tran.replace(multiContainer.getId(), chans);
				tran.commit();
				setTitle(R.string.channelList);
			} else {
				startActivity(new Intent(this, ChannelListActivity.class));
			}
			break;
		case R.id.home_btn_timers:
			if (multiContainer != null) {
				enableDrawer = false;
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
				tran.replace(multiContainer.getId(), new TimerList());
				tran.commit();
			} else {

				startActivity(new Intent(this, TimerlistActivity.class));
			}
			break;
		case R.id.home_btn_recordings:
			if (multiContainer != null) {
				enableDrawer = false;
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
			if (multiContainer != null) {
				enableDrawer = false;
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
				tran.replace(multiContainer.getId(), new TaskList());
				tran.commit();
				setTitle(R.string.tasks);
			} else {
				startActivity(new Intent(this, TaskActivity.class));
			}
			break;
		case R.id.home_btn_status:
			if (multiContainer != null) {
				enableDrawer = false;
				FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
				tran.replace(multiContainer.getId(), new StatusList());
				tran.commit();
				setTitle(R.string.status);
			} else {
				startActivity(new Intent(this, StatusActivity.class));
			}
			break;

		default:
			break;
		}
		if (mClientSpinner != null){
			mClientSpinner.setVisibility(View.GONE);
		}
        setDrawerEnabled(enableDrawer);
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
	public void channelSelected(long groupId, int groupIndex, int channelIndex) {
		Intent channelListIntent = new Intent(this, ChannelListActivity.class);
		channelListIntent.putExtra(ChannelPager.KEY_GROUP_ID, groupId);
		channelListIntent.putExtra(ChannelPager.KEY_GROUP_INDEX, groupIndex);
		channelListIntent.putExtra(ChannelList.KEY_CHANNEL_INDEX, channelIndex);
		channelListIntent.putExtra(ChannelPager.KEY_HIDE_FAV_SWITCH, true);
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
			int spinnerPosition = index > Spinner.INVALID_POSITION ? index : Spinner.INVALID_POSITION;
			mClientSpinner.setSelection(spinnerPosition);
			mClientSpinner.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public Object getSelectedTarget() {
		if(mClientSpinner == null){
			return null;
		}
		return mClientSpinner.getSelectedItem();
	}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        if(chans != null){
            chans.setPosition(position);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ENABLE_DRAWER, enableDrawer);
		outState.putString(TITLE, getTitle().toString());
    }

}
