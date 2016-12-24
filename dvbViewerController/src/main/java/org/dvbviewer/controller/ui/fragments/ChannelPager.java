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
package org.dvbviewer.controller.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.github.jrejaud.viewpagerindicator2.TitlePageIndicator;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts.GroupTbl;
import org.dvbviewer.controller.data.DbHelper;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.EpgEntry;
import org.dvbviewer.controller.io.HTTPUtil;
import org.dvbviewer.controller.io.RecordingService;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.data.ChannelHandler;
import org.dvbviewer.controller.io.data.EpgEntryHandler;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.BaseFragment;
import org.dvbviewer.controller.ui.base.CursorPagerAdapter;
import org.dvbviewer.controller.utils.Config;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.NetUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * The Class EpgPager.
 *
 * @author RayBa82
 */
public class ChannelPager extends BaseFragment implements LoaderCallbacks<Cursor>, OnPageChangeListener {

	private static final String 					KEY_INDEX 					= ChannelPager.class.getName()+"KEY_INDEX";
	public static final  String 					KEY_GROUP_INDEX 			= ChannelPager.class.getName()+"KEY_GROUP_INDEX";
	public static final  String                     KEY_GROUP_ID      	        = ChannelPager.class.getName() + "KEY_GROUP_ID";
	private				 int 						mGroupIndex 				= AdapterView.INVALID_POSITION;
	private 			 HashMap<Integer, Integer> 	index 						= new HashMap<>();
	private static final int 						SYNCHRONIZE_CHANNELS 		= 0;
	private static final int 						LOAD_CHANNELS 				= 1;
	private static final int 						LOAD_CURRENT_PROGRAM 		= 2;
	private 			 boolean 	 				showFavs;
	private 			 boolean 	 				showGroups;
	private 			 boolean 	 				showExtraGroup;
	private 			 boolean 					showNowPlaying;
	private 			 boolean 					showNowPlayingWifi;
	private 			 boolean 					refreshGroupType;
	private 			 View 						mProgress;
	private 			 Cursor 					mGroupCursor;
	private 			 ViewPager 					mPager;
	private 			 NetworkInfo 				mNetworkInfo;
	private 			 PagerAdapter 				mAdapter;
	private 			 TitlePageIndicator	 		mPagerIndicator;
	private 			 DVBViewerPreferences 		prefs;
	private 			 OnGroupChangedListener 	mGroupCHangedListener;
	private 			 OnGroupTypeChangedListener	mOnGroupTypeCHangedListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnGroupTypeChangedListener) {
			mOnGroupTypeCHangedListener = (OnGroupTypeChangedListener) context;
		}
		if (context instanceof OnGroupChangedListener) {
			mGroupCHangedListener = (OnGroupChangedListener) context;
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
		setHasOptionsMenu(true);
		mAdapter = new PagerAdapter(getChildFragmentManager(), mGroupCursor);
		ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		mNetworkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		prefs = new DVBViewerPreferences(getActivity());
		showGroups = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_GROUPS, true);
		showExtraGroup = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_ALL_AS_GROUP, false);
		showFavs = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false);
		showNowPlaying = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_NOW_PLAYING, true);
		showNowPlayingWifi = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_NOW_PLAYING_WIFI_ONLY, true);
		if (savedInstanceState == null && getArguments() != null) {
			if (getArguments().containsKey(KEY_GROUP_INDEX)) {
				mGroupIndex = getArguments().getInt(KEY_GROUP_INDEX);
			}
			int	initialChanIndex = getArguments().getInt(ChannelList.KEY_CHANNEL_INDEX);
			index.put(mGroupIndex, initialChanIndex);
		} else if (savedInstanceState != null) {
			mGroupIndex = savedInstanceState.getInt(KEY_GROUP_INDEX);
			index = (HashMap<Integer, Integer>) savedInstanceState.getSerializable(KEY_INDEX);
		}
	}

	public void setPosition(int position) {
		mGroupIndex = position;
		if (mPager != null) {
			mPager.setCurrentItem(mGroupIndex, false);
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
		getActivity().setTitle(showFavs ? R.string.favourites : R.string.channelList);
		mPager.setAdapter(mAdapter);
		mPager.setPageMargin((int) UIUtils.dipToPixel(getActivity(), 25));
		mPagerIndicator.setViewPager(mPager);
		mPagerIndicator.setOnPageChangeListener(this);

		int loaderId = LOAD_CHANNELS;
		if (savedInstanceState == null) {
			/**
			 * Prüfung ob das EPG in der Senderliste angezeigt werden soll.
			 */
			if (!Config.CHANNELS_SYNCED) {
				loaderId = SYNCHRONIZE_CHANNELS;
			} else if ((showNowPlaying && !showNowPlayingWifi) || (showNowPlaying && mNetworkInfo.isConnected())) {
				loaderId = LOAD_CURRENT_PROGRAM;
			}
		}
		mPager.setCurrentItem(mGroupIndex);
		getLoaderManager().initLoader(loaderId, savedInstanceState, this);
		showProgress(savedInstanceState == null);
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
		View view = inflater.inflate(R.layout.pager, container, false);
		mProgress = view.findViewById(android.R.id.progress);
		mPager = (ViewPager) view.findViewById(R.id.pager);
		mPagerIndicator = (TitlePageIndicator) view.findViewById(R.id.titles);
		mPagerIndicator.setVisibility(showGroups ? View.VISIBLE : View.GONE);
		View c = view.findViewById(R.id.bottom_container);
		c.setVisibility(View.GONE);
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(android
	 * .view.Menu, android.view.MenuInflater)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockListFragment#onCreateOptionsMenu(android
	 * .view.Menu, android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.channel_pager, menu);
		for (int i = 0; i < menu.size(); i++) {
			if (menu.getItem(i).getItemId() == R.id.menuChannelList) {
				menu.getItem(i).setVisible(showFavs);
			} else if (menu.getItem(i).getItemId() == R.id.menuFavourties) {
				menu.getItem(i).setVisible(!showFavs);
			}
		}
		menu.findItem(R.id.menuChannelList).setVisible(showFavs);
		menu.findItem(R.id.menuFavourties).setVisible(!showFavs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockListFragment#onOptionsItemSelected(
	 * android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {

		case R.id.menuRefresh:
			refresh(LOAD_CURRENT_PROGRAM);
			return true;
		case R.id.menuSyncChannels:
			refreshGroupType = true;
			refresh(SYNCHRONIZE_CHANNELS);
			return true;
		case R.id.menuChannelList:
		case R.id.menuFavourties:
			showFavs = !showFavs;
			mGroupIndex = 0;
			persistChannelConfigConfig();
			getActivity().setTitle(showFavs ? R.string.favourites : R.string.channelList);
			refreshGroupType = true;
			refresh(LOAD_CHANNELS);
			getActivity().supportInvalidateOptionsMenu();
			return true;

		default:
			return false;
		}
	}

	/**
	 * Persist channel config config.
	 */
	@SuppressLint("CommitPrefEdits")
	private void persistChannelConfigConfig() {
		Editor editor = prefs.getPrefs().edit();
		editor.putBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, showFavs);
		editor.commit();
		super.onPause();
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
		public PagerAdapter(FragmentManager fm, Cursor cursor) {
			super(fm);
			mCursor = cursor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
		@Override
		public Fragment getItem(int position) {
			if (mCursor == null){
				return null;
			}
			long groupId = -1;
			if (showGroups) {
				if (showExtraGroup) {
					mCursor.moveToPosition(position - 1);
					groupId = position > 0 ? mCursor.getLong(mCursor.getColumnIndex(GroupTbl._ID)) : -1;
				} else {
					mCursor.moveToPosition(position);
					groupId = mCursor.getLong(mCursor.getColumnIndex(GroupTbl._ID));
				}
			}
			Bundle args = new Bundle();
			args.putLong(ChannelPager.KEY_GROUP_ID, groupId);
			args.putInt(ChannelPager.KEY_GROUP_INDEX, position);
			args.putInt(ChannelList.KEY_CHANNEL_INDEX, getChannelIndex(position));
			args.putBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, showFavs);
			args.putBoolean(ChannelList.KEY_HAS_OPTIONMENU, false);
			return Fragment.instantiate(getActivity(), ChannelList.class.getName(), args);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		public long getGroupId(int position) {
			mCursor.moveToPosition(position);
			return mCursor.getLong(mCursor.getColumnIndex(GroupTbl._ID));
		}


		/*
				 * (non-Javadoc)
				 *
				 * @see android.support.v4.view.PagerAdapter#getCount()
				 */
		@Override
		public int getCount() {
			if (mCursor != null) {
				if (showGroups) {
					if (showExtraGroup) {
						return mCursor.getCount() + 1;
					} else {
						return mCursor.getCount();
					}
				} else {
					return 1;
				}
			}
			return 0;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String title = getString(R.string.common_all);
			if (showExtraGroup) {
				mCursor.moveToPosition(position - 1);
				if (position > 0) {
					title = mCursor.getString(mCursor.getColumnIndex(GroupTbl.NAME));
					return title;
				}
			} else {
				mCursor.moveToPosition(position);
				title = mCursor.getString(mCursor.getColumnIndex(GroupTbl.NAME));

			}
			return title;
		}

	}

	/**
	 * Sets the position.
	 *
	 * @param channelIndex the new channel index
	 */
	public void setChannelSelection(long groupId, int channelIndex) {
		Uri uri = ChannelList.BASE_CONTENT_URI.buildUpon().appendPath(String.valueOf(groupId)).appendQueryParameter("index", String.valueOf(channelIndex)).build();
		index.put(mPager.getCurrentItem(), channelIndex);
		getContext().getContentResolver().notifyChange(uri, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
	 * android.os.Bundle)
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		Loader<Cursor> loader = null;
		switch (id) {
			case SYNCHRONIZE_CHANNELS:
				loader = new AsyncLoader<Cursor>(getContext()) {

					@Override
					public Cursor loadInBackground() {

						performRefresh();
						return new MatrixCursor(new String[1]);
					}

				};
				break;
			case LOAD_CHANNELS:
				String selection = showFavs ? GroupTbl.TYPE + " = " + ChannelGroup.TYPE_FAV : GroupTbl.TYPE + " = " + ChannelGroup.TYPE_CHAN;
				String orderBy = GroupTbl._ID;
				loader = new CursorLoader(getContext(), GroupTbl.CONTENT_URI, null, selection, null, orderBy);
				break;
			case LOAD_CURRENT_PROGRAM:
				loader = new AsyncLoader<Cursor>(getContext()) {

					@Override
					public Cursor loadInBackground() {
						loadEpg();
						return new MatrixCursor(new String[1]);
					}

				};
				break;
			default:
				break;
		}
		return loader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
	 * .support.v4.content.Loader, java.lang.Object)
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
			case LOAD_CURRENT_PROGRAM:
				refresh(LOAD_CHANNELS);
				break;
			case SYNCHRONIZE_CHANNELS:
				/**
				 * Pr�fung ob das EPG in der Senderliste angezeigt werden soll.
				 */
				if ((showNowPlaying && !showNowPlayingWifi) || (showNowPlaying && mNetworkInfo.isConnected())) {
					refresh(LOAD_CURRENT_PROGRAM);
				} else {
					refresh(LOAD_CHANNELS);
				}
				break;
			case LOAD_CHANNELS:
				mGroupCursor = cursor;
				mAdapter.changeCursor(mGroupCursor);
				mAdapter.notifyDataSetChanged();
				mPager.setCurrentItem(mGroupIndex, false);
				// mPager.setPageTransformer(true, new DepthPageTransformer());
				getActivity().supportInvalidateOptionsMenu();
				showProgress(false);
				if (refreshGroupType){
					if (mOnGroupTypeCHangedListener != null) {
						mOnGroupTypeCHangedListener.groupTypeChanged(showFavs ? ChannelGroup.TYPE_FAV : ChannelGroup.TYPE_CHAN);
					}
				}
				refreshGroupType = false;
			break;

		default:
			showProgress(false);
			break;
		}
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
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_GROUP_INDEX, mPager.getCurrentItem());
		outState.putSerializable(KEY_INDEX, index);
	}

	private void performRefresh() {
		DbHelper mDbHelper = new DbHelper(getContext());
		InputStream chanXml = null;
		InputStream favXml = null;
		try {
			String version = RecordingService.getVersionString();
			if (!Config.isRSVersionSupported(version)) {
				showToast(getContext(), MessageFormat.format(getStringSafely(R.string.version_unsupported_text), Config.SUPPORTED_RS_VERSION));
				return;
			}
			/**
			 * Request the Channels
			 */
			chanXml = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_CHANNELS);
			ChannelHandler channelHandler = new ChannelHandler();
			List<ChannelRoot> chans = channelHandler.parse(chanXml, false);
			/**
			 * Request the Favourites
			 */
			favXml = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_FAVS);
			if (favXml != null) {
				List<ChannelRoot> favs = channelHandler.parse(favXml, true);
				if(favs != null && !favs.isEmpty()) {
					chans.addAll(favs);
				}
			}
			mDbHelper.saveChannelRoots(chans);


			/**
			 * Get the Mac Address for WOL
			 */
			String macAddress = NetUtils.getMacFromArpCache(ServerConsts.REC_SERVICE_HOST);
			ServerConsts.REC_SERVICE_MAC_ADDRESS = macAddress;


			/**
			 * Save the data in sharedpreferences
			 */
			Editor prefEditor = prefs.getPrefs().edit();
			StatusList.getStatus(prefs, version);
			prefEditor.putString(DVBViewerPreferences.KEY_RS_MAC_ADDRESS, macAddress);
			prefEditor.putBoolean(DVBViewerPreferences.KEY_CHANNELS_SYNCED, true);
			prefEditor.putString(DVBViewerPreferences.KEY_RS_VERSION, version);
			prefEditor.commit();
			Config.CHANNELS_SYNCED = true;
		} catch (Exception e) {
			catchException(getClass().getSimpleName(), e);
		} finally {
			mDbHelper.close();
			IoUtils.closeSilently(chanXml);
			IoUtils.closeSilently(favXml);
		}
	}

	private void loadEpg() {
		List<EpgEntry> result;
		DbHelper helper = new DbHelper(getContext());
		InputStream is = null;
		try {
			String nowFloat = DateUtils.getFloatDate(new Date());
			HTTPUtil.UrlBuilder builder = ChannelEpg.buildBaseEpgUrl()
					.addQueryParameter("start", nowFloat)
					.addQueryParameter("end", nowFloat);
			EpgEntryHandler handler = new EpgEntryHandler();
			is = ServerRequest.getInputStream(builder.build().toString());
			result = handler.parse(is);
			helper.saveNowPlaying(result);
		} catch (Exception e) {
			catchException(getClass().getSimpleName(), e);
		} finally {
			IoUtils.closeSilently(is);
			helper.close();
		}
	}

	/**
	 * Refresh.
	 *
	 * @param id the id
	 */
	private void refresh(int id) {
		mGroupCursor = null;
		mPager.setAdapter(null);
		mAdapter.notifyDataSetChanged();
		mAdapter = new PagerAdapter(getChildFragmentManager(), mGroupCursor);
		mPager.setAdapter(mAdapter);
		mPagerIndicator.notifyDataSetChanged();
		getLoaderManager().destroyLoader(id);
		getLoaderManager().restartLoader(id, getArguments(), this);
		showProgress(true);
	}

	private void showProgress(boolean show) {
		mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
		mPager.setVisibility(show ? View.GONE : View.VISIBLE);
		if (showGroups) {
			mPagerIndicator.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public String getStringSafely(int resId) {
		String result = "";
		if (!isDetached() && isAdded() && isVisible()) {
			try {
				result = getString(resId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public interface OnGroupTypeChangedListener {

		void groupTypeChanged(int type);

	}

	public interface OnGroupChangedListener {

		void groupChanged(long groupId, int groupIndex, int channelIndex);

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		mGroupIndex = position;
		if (mGroupCHangedListener != null) {
			mGroupCHangedListener.groupChanged(mAdapter.getGroupId(mGroupIndex), mPager.getCurrentItem(), getChannelIndex(mGroupIndex));
		}
	}

	public void updateIndex(int groupIndex, int channelIndex) {
		index.put(groupIndex,channelIndex);
	}

	private int getChannelIndex(int groupIndex){
		Integer channelIndex = index.get(groupIndex);
		return channelIndex != null ? channelIndex : 0;
	}

}
