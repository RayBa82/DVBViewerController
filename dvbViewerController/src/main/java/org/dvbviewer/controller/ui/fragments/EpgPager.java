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
package org.dvbviewer.controller.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts;
import org.dvbviewer.controller.data.DbConsts.ChannelTbl;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.ui.base.CursorPagerAdapter;
import org.dvbviewer.controller.ui.fragments.ChannelEpg.EpgDateInfo;
import org.dvbviewer.controller.ui.widget.ActionToolbar;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.UIUtils;

import java.util.Date;

/**
 * The Class EpgPager.
 */
public class EpgPager extends Fragment implements LoaderCallbacks<Cursor>, Toolbar.OnMenuItemClickListener, OnPageChangeListener {

	public static final String 						KEY_HIDE_OPTIONSMENU 	= EpgPager.class.getName() + "KEY_HIDE_OPTIONSMENU";
	private             long   						mGroupId          		= AdapterView.INVALID_POSITION;
	private             int    						chanIndex         		= AdapterView.INVALID_POSITION;
	private             int                     	mGroupIndex       	    = AdapterView.INVALID_POSITION;
	private 			ViewPager 					mPager;
	private 			PagerAdapter 				mAdapter;
	private OnChannelScrolledListener mOnCHannelChanedListener;
	private 			Boolean                 	showFavs;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnChannelScrolledListener) {
			mOnCHannelChanedListener = (OnChannelScrolledListener) context;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new PagerAdapter(getChildFragmentManager());
		DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
		showFavs = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false);
		boolean showOptionsMenu = true;
		if (getArguments() != null){
			showOptionsMenu = !getArguments().getBoolean(KEY_HIDE_OPTIONSMENU, false);
		}
		setHasOptionsMenu(showOptionsMenu);
		if (savedInstanceState == null) {
			if (getArguments() != null){
				mGroupId = getArguments().containsKey(ChannelPager.KEY_GROUP_ID) ? getArguments().getLong(ChannelPager.KEY_GROUP_ID, mGroupId) : mGroupId;
				mGroupIndex = getArguments().containsKey(ChannelPager.KEY_GROUP_INDEX) ? getArguments().getInt(ChannelPager.KEY_GROUP_INDEX, mGroupIndex) : mGroupIndex;
				chanIndex = getArguments().containsKey(ChannelList.KEY_CHANNEL_INDEX) ? getArguments().getInt(ChannelList.KEY_CHANNEL_INDEX, chanIndex) : chanIndex;
			}
		} else {
			mGroupId = savedInstanceState.containsKey(ChannelPager.KEY_GROUP_ID) ? savedInstanceState.getLong(ChannelPager.KEY_GROUP_ID, mGroupId) : mGroupId;
			mGroupIndex = savedInstanceState.containsKey(ChannelPager.KEY_GROUP_INDEX) ? savedInstanceState.getInt(ChannelPager.KEY_GROUP_INDEX, mGroupIndex) : mGroupIndex;
			chanIndex = savedInstanceState.containsKey(ChannelList.KEY_CHANNEL_INDEX) ? savedInstanceState.getInt(ChannelList.KEY_CHANNEL_INDEX, chanIndex) : chanIndex;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPager.setAdapter(mAdapter);
		mPager.setPageMargin((int) UIUtils.dipToPixel(getActivity(), 25));
		mPager.setCurrentItem(chanIndex);
		mPager.addOnPageChangeListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mPager = (ViewPager) view.findViewById(R.id.pager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pager, container, false);
		ActionToolbar bootomBar = (ActionToolbar) v.findViewById(R.id.toolbar);
		bootomBar.inflateMenu(R.menu.channel_epg_bottom_bar);
		bootomBar.setOnMenuItemClickListener(this);
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ChannelPager.KEY_GROUP_ID, mGroupId);
		outState.putInt(ChannelList.KEY_CHANNEL_INDEX, mPager.getCurrentItem());
	}

	/*
		 * (non-Javadoc)
		 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.channel_epg, menu);
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menuRefresh:
				refresh();
				break;
			default:
				return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	public boolean onMenuItemClick(MenuItem menuItem) {
		EpgDateInfo info = (EpgDateInfo) getActivity();
		int itemId = menuItem.getItemId();
		switch (itemId) {
			case R.id.menuPrev:
				info.setEpgDate(DateUtils.substractDay(new Date(info.getEpgDate())).getTime());
				info.setEpgDate(DateUtils.substractDay(new Date(info.getEpgDate())).getTime());
				break;
			case R.id.menuNext:
				info.setEpgDate(DateUtils.addDay(new Date(info.getEpgDate())).getTime());
				break;
			case R.id.menuToday:
				info.setEpgDate(new Date().getTime());
				break;
			case R.id.menuNow:
				info.setEpgDate(DateUtils.setCurrentTime(new Date(info.getEpgDate())).getTime());
				break;
			case R.id.menuEvening:
				info.setEpgDate(DateUtils.setEveningTime(new Date(info.getEpgDate())).getTime());
				break;
			default:
				return false;
		}
		getActivity().supportInvalidateOptionsMenu();
		ChannelEpg mCurrent;
		mCurrent = (ChannelEpg) mAdapter.instantiateItem(mPager, mPager.getCurrentItem());
		mCurrent.refresh();
		return true;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		if (mOnCHannelChanedListener != null){
			mOnCHannelChanedListener.channelChanged(mGroupId, position);
		}

	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}


	/**
	 * The Class PagerAdapter.
	 */
	class PagerAdapter extends CursorPagerAdapter {

		/**
		 * Instantiates a new pager adapter.
		 *
		 * @param fm the fm
		 */
		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
		@Override
		public Fragment getItem(int position) {
			if (getCursor() == null || getCursor().isClosed() || position == AdapterView.INVALID_POSITION) {
				return null;
			}
			getCursor().moveToPosition(position);
			final Channel chan = ChannelList.cursorToChannel(getCursor());
			final Bundle bundle = new Bundle();
			bundle.putString(ChannelEpg.KEY_CHANNEL_NAME, chan.getName());
			bundle.putLong(ChannelEpg.KEY_EPG_ID, chan.getEpgID());
			bundle.putLong(ChannelEpg.KEY_CHANNEL_ID, chan.getChannelID());
			bundle.putString(ChannelEpg.KEY_CHANNEL_LOGO, chan.getLogoUrl());
			bundle.putInt(ChannelEpg.KEY_CHANNEL_POS, chan.getPosition());
			bundle.putInt(ChannelEpg.KEY_FAV_POS, chan.getFavPosition());
			return Fragment.instantiate(getActivity(), ChannelEpg.class.getName(), bundle);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

	}

	/**
	 * Sets the position.
	 *
	 * @param position the new position
	 */
	public void setPosition(int position) {
		chanIndex = position;
		if (mPager != null) {
			mPager.setCurrentItem(chanIndex, false);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		StringBuilder selection = new StringBuilder(showFavs ? ChannelTbl.FLAGS + " & " + Channel.FLAG_FAV + "!= 0" : ChannelTbl.FLAGS + " & " + Channel.FLAG_ADDITIONAL_AUDIO + "== 0");
		if (mGroupId > 0) {
			selection.append(" and ");
			if (showFavs) {
				selection.append(DbConsts.FavTbl.FAV_GROUP_ID).append(" = ").append(mGroupId);
			} else {
				selection.append(ChannelTbl.GROUP_ID).append(" = ").append(mGroupId);
			}
		}
		String orderBy;
		orderBy = showFavs ? ChannelTbl.FAV_POSITION : ChannelTbl.POSITION;
		return new CursorLoader(getContext(), ChannelTbl.CONTENT_URI, null, selection.toString(), null, orderBy);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
	 * .support.v4.content.Loader, java.lang.Object)
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		mAdapter.notifyDataSetChanged();
		mPager.setCurrentItem(chanIndex, false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
	 * .support.v4.content.Loader)
	 */
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}

	public void refresh(long groupId, int selectedPosition) {
		if (mGroupId != groupId){
			mGroupId = groupId;
			chanIndex = selectedPosition;
			refresh();
		}

	}
	private void refresh() {
		resetLoader();
	}

	private void resetLoader() {
		DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
		showFavs = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false);
		mAdapter.swapCursor(null);
		mAdapter.notifyDataSetChanged();
		getLoaderManager().destroyLoader(0);
		getLoaderManager().restartLoader(0, getArguments(), this);
	}


	public interface OnChannelScrolledListener {

		void channelChanged(long groupId, int channelIndex);

	}

}
