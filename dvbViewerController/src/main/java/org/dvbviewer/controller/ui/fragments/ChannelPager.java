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

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.github.jrejaud.viewpagerindicator2.TitlePageIndicator;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.squareup.okhttp.HttpUrl;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts.GroupTbl;
import org.dvbviewer.controller.data.DbHelper;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.EpgEntry;
import org.dvbviewer.controller.io.RecordingService;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.data.ChannelHandler;
import org.dvbviewer.controller.io.data.EpgEntryHandler;
import org.dvbviewer.controller.io.data.FavMatcher;
import org.dvbviewer.controller.io.data.FavouriteHandler;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.BaseFragment;
import org.dvbviewer.controller.ui.base.CursorPagerAdapter;
import org.dvbviewer.controller.utils.AnalyticsTracker;
import org.dvbviewer.controller.utils.Config;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.NetUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * The Class EpgPager.
 *
 * @author RayBa
 */
public class ChannelPager extends BaseFragment implements LoaderCallbacks<Cursor>, OnPageChangeListener {

	public static final String KEY_GROUP_INDEX = ChannelPager.class.getName()+"KEY_GROUP_INDEX";
	int mPosition = AdapterView.INVALID_POSITION;
	int mInitialPosition = AdapterView.INVALID_POSITION;
	int initialChanIndex = AdapterView.INVALID_POSITION;

	private Cursor mGroupCursor;

	private ViewPager mPager;

	private View mProgress;

	private TitlePageIndicator mPagerIndicator;

	private PagerAdapter mAdapter;

	private boolean showFavs;
	private boolean showGroups;
	private boolean showExtraGroup;

	private DVBViewerPreferences prefs;

	private static final int SYNCHRONIZE_CHANNELS = 0;

	public static final int LOAD_CHANNELS = 1;

	private static final int LOAD_CURRENT_PROGRAM = 2;

	private NetworkInfo mNetworkInfo;

	private boolean showNowPlaying;

	private boolean                    showNowPlayingWifi;
	private OnGroupTypeChangedListener mOnGroupTypeCHangedListener;
	private OnGroupChangedListener     mGroupCHangedListener;
	boolean setInitialSelection;
	private boolean refreshGroupType;

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
		setInitialSelection = true;
		if (savedInstanceState == null && getArguments() != null) {
			if (getArguments().containsKey(KEY_GROUP_INDEX)) {
				mPosition = getArguments().getInt(KEY_GROUP_INDEX);
				mInitialPosition = getArguments().getInt(KEY_GROUP_INDEX);
			}
			if (getArguments().containsKey(ChannelList.KEY_CHANNEL_INDEX)) {
				initialChanIndex = getArguments().getInt(ChannelList.KEY_CHANNEL_INDEX);
			}
		} else if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(KEY_GROUP_INDEX)) {
				mPosition = savedInstanceState.getInt(KEY_GROUP_INDEX);
			}
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
		mPager.setCurrentItem(mPosition);
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

		inflater.inflate(R.menu.channel_list, menu);
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
			getActivity().onOptionsItemSelected(item);
			return true;
		case R.id.menuRefreshChannels:
			refreshGroupType = true;
			refresh(SYNCHRONIZE_CHANNELS);
			return true;
		case R.id.menuChannelList:
		case R.id.menuFavourties:
			showFavs = !showFavs;
			mPosition = 0;
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
	public void persistChannelConfigConfig() {
		Editor editor = prefs.getPrefs().edit();
		editor.putBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, showFavs);
		editor.commit();
		super.onPause();
	}

	/**
	 * The Class PagerAdapter.
	 */
	class PagerAdapter extends CursorPagerAdapter {

		HashMap<Integer, WeakReference<ChannelList>> fragmentCache = new HashMap<>();

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
			ChannelList channelList = (ChannelList) Fragment.instantiate(getActivity(), ChannelList.class.getName());
			Bundle args = new Bundle();
			args.putLong(ChannelList.KEY_GROUP_ID, groupId);
			args.putInt(ChannelPager.KEY_GROUP_INDEX, position);
			args.putInt(ChannelList.KEY_CHANNEL_INDEX, (setInitialSelection && mInitialPosition == position) ? initialChanIndex : 0);
			if (position == mInitialPosition){
				mInitialPosition = AdapterView.INVALID_POSITION;
				setInitialSelection = false;
			}
			args.putBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, showFavs);
			args.putBoolean(ChannelList.KEY_HAS_OPTIONMENU, false);
			channelList.setArguments(args);
			return channelList;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		public long getGroupId(int position) {
			mCursor.moveToPosition(position);
			return mCursor.getLong(mCursor.getColumnIndex(GroupTbl._ID));
		}


		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			super.setPrimaryItem(container, position, object);
			ChannelList channelList = (ChannelList) object;
			fragmentCache.put(position, new WeakReference<>(channelList));
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

		public ChannelList getCurrentFragment(int position) {
			WeakReference<ChannelList> ref = fragmentCache.get(position);
			if (ref == null){
				Log.i(ChannelList.class.getSimpleName(), "no Fragment ref found ");
				return null;
			}
			ChannelList result = ref.get();
			return result;
		}

	}

	/**
	 * Sets the position.
	 *
	 * @param position the new position
	 */
	public void setPosition(int position) {
		mPosition = position;
		if (mPager != null) {
			mPager.setCurrentItem(mPosition, false);
		}
	}

	public void setSelection(int groupIndex, int channelIndex) {
		setPosition(groupIndex);
		getCurrentFragment().setSelection(channelIndex);
	}

	/**
	 * Sets the position.
	 */
	public int getPosition() {
		int result = AbsListView.INVALID_POSITION;
		if (mPager != null) {
			result = mPager.getCurrentItem();
		}
		return result;
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
				loader = new AsyncLoader<Cursor>(getActivity().getApplicationContext()) {

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
				loader = new CursorLoader(getActivity().getApplicationContext(), GroupTbl.CONTENT_URI, null, selection, null, orderBy);
				break;
			case LOAD_CURRENT_PROGRAM:
				loader = new AsyncLoader<Cursor>(getActivity().getApplicationContext()) {

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
				mPager.setCurrentItem(mPosition, false);
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
	}

	public ChannelList getCurrentFragment() {
		if (mAdapter == null){
			return null;
		}
		return mAdapter.getCurrentFragment(mPager.getCurrentItem());
	}

	private void performRefresh() {
		JSONObject trackingData = AnalyticsTracker.buildTracker();
		DbHelper mDbHelper = new DbHelper(getContext());
		try {
			String version = RecordingService.getVersionString();
			AnalyticsTracker.addData(trackingData, "version", version);
			if (!Config.isRSVersionSupported(version)) {
				showToast(getContext(), MessageFormat.format(getStringSafely(R.string.version_unsupported_text), Config.SUPPORTED_RS_VERSION));
				return;
			}
			/**
			 * Request the Channels
			 */
			String chanXml = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_CHANNELS);
			AnalyticsTracker.addData(trackingData, "channels", chanXml);
			ChannelHandler channelHandler = new ChannelHandler();
			List<ChannelRoot> chans = channelHandler.parse(chanXml);
			chans = mDbHelper.saveChannelRoots(chans);
			/**
			 * Request the Favourites
			 */
			String favXml = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_FAVS);
			if (!TextUtils.isEmpty(favXml)) {
				AnalyticsTracker.addData(trackingData, "favourites", favXml);
				FavouriteHandler handler = new FavouriteHandler();
				List<ChannelGroup> favGroups = handler.parse(getActivity(), favXml);
				FavMatcher favMatcher = new FavMatcher();
				List<ChannelGroup> favs = favMatcher.matchFavs(chans, favGroups);
				mDbHelper.saveFavGroups(favs);
			}


			/**
			 * Get the Mac Address for WOL
			 */
			String macAddress = NetUtils.getMacFromArpCache(ServerConsts.REC_SERVICE_HOST);
			ServerConsts.REC_SERVICE_MAC_ADDRESS = macAddress;


			/**
			 * Save the data in sharedpreferences
			 */
			Editor prefEditor = prefs.getPrefs().edit();
			StatusList.getStatus(prefs, version, trackingData);
			prefEditor.putString(DVBViewerPreferences.KEY_RS_MAC_ADDRESS, macAddress);
			prefEditor.putBoolean(DVBViewerPreferences.KEY_CHANNELS_SYNCED, true);
			prefEditor.putString(DVBViewerPreferences.KEY_RS_VERSION, version);
			prefEditor.commit();
			Config.CHANNELS_SYNCED = true;
		} catch (Exception e) {
			catchException(getClass().getSimpleName(), e);
		} finally {
			AnalyticsTracker.trackSync(getContext(), trackingData);
			mDbHelper.close();
		}
	}

	private void loadEpg() {
		List<EpgEntry> result;
		DbHelper helper = new DbHelper(getContext());
		InputStream is = null;
		try {
			String nowFloat = DateUtils.getFloatDate(new Date());
			HttpUrl.Builder builder = ChannelEpg.buildBaseEpgUrl()
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
	public void refresh(int id) {
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

		void groupChanged(long groupId, int groupIndex);

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		mPosition = position;
		int channelIndex = 0;
		ChannelList channelList = mAdapter.getCurrentFragment(position);
		if (channelList != null) {
			channelIndex = channelList.getChannelIndex();
		}
		Log.i(ChannelPager.class.getSimpleName(), "channelIndex " + channelIndex);
		if (mGroupCHangedListener != null) {
			mGroupCHangedListener.groupChanged(mAdapter.getGroupId(position), position);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
}
