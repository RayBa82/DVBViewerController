/*
 * Copyright © 2012 dvbviewer-controller Project
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts.ChannelTbl;
import org.dvbviewer.controller.data.DbConsts.EpgTbl;
import org.dvbviewer.controller.data.DbHelper;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.Channel.Fav;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.EpgEntry;
import org.dvbviewer.controller.entities.Status;
import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.ServerRequest.DVBViewerCommand;
import org.dvbviewer.controller.io.ServerRequest.RecordingServiceGet;
import org.dvbviewer.controller.io.data.ChannelHandler;
import org.dvbviewer.controller.io.data.ChannelListParser;
import org.dvbviewer.controller.io.data.EpgEntryHandler;
import org.dvbviewer.controller.io.data.FavouriteHandler;
import org.dvbviewer.controller.io.data.StatusHandler;
import org.dvbviewer.controller.io.data.VersionHandler;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.BaseListFragment;
import org.dvbviewer.controller.ui.phone.EpgPagerActivity;
import org.dvbviewer.controller.ui.phone.StreamConfigActivity;
import org.dvbviewer.controller.ui.phone.TimerDetailsActivity;
import org.dvbviewer.controller.ui.tablet.ChannelListMultiActivity;
import org.dvbviewer.controller.ui.widget.CheckableLinearLayout;
import org.dvbviewer.controller.utils.Config;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.NetUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.ParseException;
import ch.boye.httpclientandroidlib.auth.AuthenticationException;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.utils.URLEncodedUtils;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.ViewTarget;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * The Class ChannelList.
 *
 * @author RayBa
 * @date 05.07.2012
 */
public class ChannelList extends BaseListFragment implements LoaderCallbacks<Cursor>, OnClickListener {

	public static final String	KEY_SELECTED_POSITION		= "SELECTED_POSITION";
	public static final String	KEY_HAS_OPTIONMENU			= "HAS_OPTIONMENU";
	DVBViewerPreferences		prefs;
	ChannelAdapter				mAdapter;
	int							selectedPosition			= -1;
	boolean						hasOptionsMenu				= true;
	boolean						showFavs;
	boolean						showNowPlaying;
	boolean						showNowPlayingWifi;
	public static final int		LOADER_REFRESH_CHANNELLIST	= 100;
	public static final int		LOADER_CHANNELLIST			= 101;
	public static final int		LOADER_EPG					= 103;
	OnChannelSelectedListener	mCHannelSelectedListener;
	View						selectView;
	Context						mContext;
	private NetworkInfo			mNetworkInfo;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();

		ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		mNetworkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		prefs = new DVBViewerPreferences(getActivity());
		showFavs = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false);
		showNowPlaying = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_NOW_PLAYING, true);
		showNowPlayingWifi = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_SHOW_NOW_PLAYING_WIFI_ONLY, true);
		mAdapter = new ChannelAdapter(getActivity());
		if (getArguments() != null) {
			if (getArguments().containsKey(ChannelList.KEY_HAS_OPTIONMENU)) {
				hasOptionsMenu = getArguments().getBoolean(KEY_HAS_OPTIONMENU);
			}
		}
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(KEY_SELECTED_POSITION)) {
				selectedPosition = savedInstanceState.getInt(KEY_SELECTED_POSITION);
			}
		} else {
			selectedPosition = getActivity().getIntent().getIntExtra(KEY_SELECTED_POSITION, selectedPosition);
		}
		setHasOptionsMenu(hasOptionsMenu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockListFragment#onAttach(android.app.Activity
	 * )
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnChannelSelectedListener) {
			mCHannelSelectedListener = (OnChannelSelectedListener) activity;
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
		setListAdapter(mAdapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		registerForContextMenu(getListView());
		int loaderId = LOADER_CHANNELLIST;
		/**
		 * Pr©fung ob das EPG in der Senderliste angezeigt werden soll.
		 */
		if (!Config.CHANNELS_SYNCED) {
			loaderId = LOADER_REFRESH_CHANNELLIST;
		} else if ((showNowPlaying && !showNowPlayingWifi) || (showNowPlaying && showNowPlayingWifi && mNetworkInfo.isConnected())) {
			loaderId = LOADER_EPG;
		}
		setEmptyText(showFavs ? getResources().getString(R.string.no_favourites) : getResources().getString(R.string.no_channels));
		Loader<Cursor> loader = getLoaderManager().initLoader(loaderId, savedInstanceState, this);
		setListShown(!(!isResumed() || loader.isStarted()));
		setSelection(selectedPosition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
	 * android.os.Bundle)
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		Loader<Cursor> loader = null;
		switch (loaderId) {
		case LOADER_CHANNELLIST:
			String selection = showFavs ? ChannelTbl.FLAGS + " & " + Channel.FLAG_FAV + "!= 0" : ChannelTbl.FLAGS + " & " + Channel.FLAG_ADDITIONAL_AUDIO + "== 0";
			String orderBy = showFavs ? ChannelTbl.FAV_POSITION : ChannelTbl.POSITION;
			loader = new CursorLoader(getActivity().getApplicationContext(), ChannelTbl.CONTENT_URI_NOW, null, selection, null, orderBy);
			break;
		case LOADER_EPG:
			loader = new AsyncLoader<Cursor>(getActivity().getApplicationContext()) {

				@Override
				public Cursor loadInBackground() {
					List<EpgEntry> result = null;
					String nowFloat = org.dvbviewer.controller.utils.DateUtils.getFloatDate(new Date());
					String url = ServerConsts.URL_EPG + "&start=" + nowFloat + "&end=" + nowFloat;
					try {
						EpgEntryHandler handler = new EpgEntryHandler();
						String xml = ServerRequest.getRSString(url);
						result = handler.parse(xml);
						DbHelper helper = new DbHelper(getContext());
						helper.saveNowPlaying(result);
					} catch (AuthenticationException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_invalid_credentials));
					} catch (UnknownHostException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_unknonwn_host) + "\n\n" + ServerConsts.REC_SERVICE_URL);
					} catch (ConnectTimeoutException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_connection_timeout));
					} catch (SAXException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_parsing_xml));
					} catch (ParseException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_common) + "\n\n" + e.getMessage());
					} catch (ClientProtocolException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_common) + "\n\n" + e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_common) + "\n\n" + e.getMessage());
					} catch (URISyntaxException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_invalid_url) + "\n\n" + ServerConsts.REC_SERVICE_URL);
					} catch (IllegalStateException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_invalid_url) + "\n\n" + ServerConsts.REC_SERVICE_URL);
					} catch (IllegalArgumentException e) {
						showToast(getStringSafely(R.string.error_invalid_url) + "\n\n" + ServerConsts.REC_SERVICE_URL);
					} catch (Exception e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_common) + "\n\n" + e.getMessage() != null ? e.getMessage() : e.getClass().getName());
					}
					return null;
				}


			};
			break;
		case LOADER_REFRESH_CHANNELLIST:
			loader = new AsyncLoader<Cursor>(getActivity().getApplicationContext()) {

				@Override
				public Cursor loadInBackground() {

					try {
						/**
						 * Request the Status.xml to get some default Recordingservice Configs
						 */
						String statusXml = ServerRequest.getRSString(ServerConsts.URL_STATUS);
						StatusHandler statusHandler = new StatusHandler();
						Status s = statusHandler.parse(statusXml);
						String version = getVersionString();
					
						DbHelper mDbHelper = new DbHelper(mContext);
						/**
						 * Request the Channels
						 */
						if (Config.isOldRsVersion(version)) {
							byte[] rawData = ServerRequest.getRSBytes(ServerConsts.URL_CHANNELS_OLD);
							List<Channel> chans = ChannelListParser.parseChannelList(getContext(), rawData);
							mDbHelper.saveChannels(chans);
						}else {
							String chanXml = ServerRequest.getRSString(ServerConsts.URL_CHANNELS);
							ChannelHandler channelHandler = new ChannelHandler();
							List<Channel> chans = channelHandler.parse(chanXml);
							mDbHelper.saveChannels(chans);
						}
						
						/**
						 * Request the Favourites
						 */
						String favXml = ServerRequest.getRSString(ServerConsts.URL_FAVS);
						if (!TextUtils.isEmpty(favXml)) {
							FavouriteHandler handler = new FavouriteHandler();
							List<Fav> favs = handler.parse(getActivity(), favXml);
							mDbHelper.saveFavs(favs);
						}

						mDbHelper.close();
						
						/**
						 * Get the Mac Address for WOL
						 */
						String macAddress = NetUtils.getMacFromArpCache(ServerConsts.REC_SERVICE_HOST);
						ServerConsts.REC_SERVICE_MAC_ADDRESS = macAddress;
						/**
						 * Save the data in sharedpreferences
						 */
						Editor prefEditor = prefs.getPrefs().edit();
						if (s != null) {
							prefEditor.putInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, s.getEpgBefore());
							prefEditor.putInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, s.getEpgAfter());
							prefEditor.putInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, s.getDefAfterRecord());
						}
						prefEditor.putString(DVBViewerPreferences.KEY_RS_MAC_ADDRESS, macAddress);
						prefEditor.putBoolean(DVBViewerPreferences.KEY_CHANNELS_SYNCED, true);
						prefEditor.commit();
						Config.CHANNELS_SYNCED = true;
					} catch (AuthenticationException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_invalid_credentials));
					} catch (UnknownHostException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_unknonwn_host) + "\n\n" + ServerConsts.REC_SERVICE_URL);
					} catch (ConnectTimeoutException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_connection_timeout));
					} catch (SAXException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_parsing_xml));
					} catch (ParseException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_common) + "\n\n" + e.getMessage());
					} catch (ClientProtocolException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_common) + "\n\n" + e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_common) + "\n\n" + e.getMessage());
					} catch (URISyntaxException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_invalid_url) + "\n\n" + ServerConsts.REC_SERVICE_URL);
					} catch (IllegalStateException e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_invalid_url) + "\n\n" + ServerConsts.REC_SERVICE_URL);
					} catch (IllegalArgumentException e) {
						showToast(getStringSafely(R.string.error_invalid_url) + "\n\n" + ServerConsts.REC_SERVICE_URL);
					} catch (Exception e) {
						e.printStackTrace();
						showToast(getStringSafely(R.string.error_common) + "\n\n" + e.getMessage());
					}
					return null;
				}

				private String getVersionString() throws Exception {
					String version = null;
					try {
						String versionXml = ServerRequest.getRSString(ServerConsts.URL_VERSION);
						VersionHandler versionHandler = new VersionHandler();
						version = versionHandler.parse(versionXml);
//						here is a regex required! 
						version = version.replace("DVBViewer Recording Service ", "");
						String[] arr = version.split(" ");
						version = arr[0];
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					return version;
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
	@SuppressLint("NewApi")
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOADER_EPG:
			refresh(LOADER_CHANNELLIST);
			break;
		case LOADER_REFRESH_CHANNELLIST:
			/**
			 * Pr©fung ob das EPG in der Senderliste angezeigt werden soll.
			 */
			if ((showNowPlaying && !showNowPlayingWifi) || (showNowPlaying && showNowPlayingWifi && mNetworkInfo.isConnected())) {
				refresh(LOADER_EPG);
			}else {
				refresh(LOADER_CHANNELLIST);
			}
			break;
		default:
			mAdapter.swapCursor(cursor);
			if (selectedPosition != ListView.INVALID_POSITION) {
				getListView().setItemChecked(selectedPosition, true);
			}
			getListView().setSelectionFromTop(selectedPosition, (int) getResources().getDimension(R.dimen.list_preferred_item_height_small) * 3);
			setListShown(true);
			break;
		}
		getActivity().supportInvalidateOptionsMenu();
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
		if (isVisible()) {
			setListShown(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater
	 * , android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// View v = getActivity().getLayoutInflater().inflate(R.layout.list,
		// container);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

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
		if (getActivity() instanceof ChannelListMultiActivity) {
			menu.findItem(R.id.menu_refresh_now_playing).setVisible(false);
			menu.findItem(R.id.menuRefreshChannels).setVisible(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu
	 * , android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.context_menu_channellist, menu);
		menu.findItem(R.id.menuSwitch).setVisible(URLUtil.isValidUrl(ServerConsts.DVBVIEWER_URL));
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
		case R.id.menu_refresh_now_playing:
			refresh(LOADER_EPG);
			return true;
		case R.id.menuRefreshChannels:
			refresh(LOADER_REFRESH_CHANNELLIST);
			return true;
		case R.id.menuChannelList:
		case R.id.menuFavourties:
			showFavs = !showFavs;
			setTitle();
			refresh(LOADER_CHANNELLIST);
			persistChannelConfigConfig();
			return true;

		default:
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem
	 * )
	 */
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getMenuInfo() != null) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			selectedPosition = info.position;
		}
		Cursor c = mAdapter.getCursor();
		c.moveToPosition(selectedPosition);
		Channel chan = cursorToChannel(c);
		switch (item.getItemId()) {
		case R.id.menuTimer:
			showTimerDialog(c);
			return true;
		case R.id.menuStream:
			if (prefs.getBoolean(DVBViewerPreferences.KEY_SHOW_QUICK_STREAM_HINT, true)) {
				prefs.getPrefs().edit().putBoolean(DVBViewerPreferences.KEY_SHOW_QUICK_STREAM_HINT, false).commit();
				showQuickstreamHint(chan.getPosition());
			}else {
				showStreamConfig(selectedPosition);
			}	
			return true;
		case R.id.menuSwitch:
			String switchRequest = ServerConsts.URL_SWITCH_COMMAND+chan.getPosition();
			DVBViewerCommand command = new DVBViewerCommand(switchRequest);
			Thread exexuterTHread = new Thread(command);
			exexuterTHread.start();
			return true;
		case R.id.menuRecord:
			recordChannel(c);
			return true;

		default:
			break;
		}
		return false;
	}

	private void recordChannel(Cursor c) {
		Timer timer;
		timer = cursorToTimer(c);
		String url = timer.getId() < 0l ? ServerConsts.URL_TIMER_CREATE : ServerConsts.URL_TIMER_EDIT;
		String title = timer.getTitle();
		String days = String.valueOf(DateUtils.getDaysSinceDelphiNull(timer.getStart()));
		String start = String.valueOf(DateUtils.getMinutesOfDay(timer.getStart()));
		String stop = String.valueOf(DateUtils.getMinutesOfDay(timer.getEnd()));
		String endAction = String.valueOf(timer.getTimerAction());
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ch", String.valueOf(timer.getChannelId())));
		params.add(new BasicNameValuePair("dor", days));
		params.add(new BasicNameValuePair("encoding", "255"));
		params.add(new BasicNameValuePair("enable", "1"));
		params.add(new BasicNameValuePair("start", start));
		params.add(new BasicNameValuePair("stop", stop));
		params.add(new BasicNameValuePair("title", title));
		params.add(new BasicNameValuePair("endact", endAction));
		if (timer.getId() >= 0) {
			params.add(new BasicNameValuePair("id", String.valueOf(timer.getId())));
		}

		String query = URLEncodedUtils.format(params, "utf-8");
		String request = url + query;
		RecordingServiceGet rsGet = new RecordingServiceGet(request);
		Thread executionThread = new Thread(rsGet);
		executionThread.start();
	}

	private void showTimerDialog(Cursor c) {
		Timer timer;
		timer = cursorToTimer(c);
		if (UIUtils.isTablet(getActivity())) {
			TimerDetails timerdetails = TimerDetails.newInstance();
			Bundle args = new Bundle();
			args.putString(TimerDetails.EXTRA_TITLE, timer.getTitle());
			args.putString(TimerDetails.EXTRA_CHANNEL_NAME, timer.getChannelName());
			args.putLong(TimerDetails.EXTRA_CHANNEL_ID, timer.getChannelId());
			args.putLong(TimerDetails.EXTRA_START, timer.getStart().getTime());
			args.putLong(TimerDetails.EXTRA_END, timer.getEnd().getTime());
			args.putInt(TimerDetails.EXTRA_ACTION, timer.getTimerAction());
			args.putBoolean(TimerDetails.EXTRA_ACTIVE, true);
			timerdetails.setArguments(args);
			timerdetails.show(getActivity().getSupportFragmentManager(), TimerDetails.class.getName());
		} else {
			Intent timerIntent = new Intent(getActivity(), TimerDetailsActivity.class);
			timerIntent.putExtra(TimerDetails.EXTRA_TITLE, timer.getTitle());
			timerIntent.putExtra(TimerDetails.EXTRA_CHANNEL_NAME, timer.getChannelName());
			timerIntent.putExtra(TimerDetails.EXTRA_CHANNEL_ID, timer.getChannelId());
			timerIntent.putExtra(TimerDetails.EXTRA_START, timer.getStart().getTime());
			timerIntent.putExtra(TimerDetails.EXTRA_END, timer.getEnd().getTime());
			timerIntent.putExtra(TimerDetails.EXTRA_ACTION, timer.getTimerAction());
			timerIntent.putExtra(TimerDetails.EXTRA_ACTIVE, !timer.isFlagSet(Timer.FLAG_DISABLED));
			startActivity(timerIntent);
		}
	}

	private void showStreamConfig(int position) {
		if (UIUtils.isTablet(getActivity())) {
			StreamConfig cfg = StreamConfig.newInstance();
			Bundle arguments = new Bundle();
			arguments.putInt(StreamConfig.EXTRA_FILE_ID, position);
			arguments.putInt(StreamConfig.EXTRA_FILE_TYPE, StreamConfig.FILE_TYPE_LIVE);
			arguments.putInt(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig);
			cfg.setArguments(arguments);
			cfg.show(getActivity().getSupportFragmentManager(), StreamConfig.class.getName());
		} else {
			Intent streamConfig = new Intent(getActivity(), StreamConfigActivity.class);
			streamConfig.putExtra(StreamConfig.EXTRA_FILE_ID, position);
			streamConfig.putExtra(StreamConfig.EXTRA_FILE_TYPE, StreamConfig.FILE_TYPE_LIVE);
			streamConfig.putExtra(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig);
			startActivity(streamConfig);
		}
	}

	/**
	 * Persist channel config config.
	 *
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public void persistChannelConfigConfig() {
		Editor editor = prefs.getPrefs().edit();
		editor.putBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, showFavs);
		editor.commit();
		super.onPause();
	}

	/**
	 * Refresh.
	 *
	 * @param id the id
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public void refresh(int id) {
		getLoaderManager().restartLoader(id, getArguments(), this);
		setListShown(false);
	}

	/**
	 * The Class ViewHolder.
	 *
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private class ViewHolder {
		CheckableLinearLayout	v;
		ImageView				icon;
		View 					iconContainer;
		TextView				position;
		TextView				channelName;
		TextView				epgTime;
		ProgressBar				progress;
		TextView				epgTitle;
		ImageView				contextMenu;
	}

	/**
	 * The Class ChannelAdapter.
	 *
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public class ChannelAdapter extends CursorAdapter {

		Context		mContext;
		ImageLoader	imageChacher;

		/**
		 * Instantiates a new channel adapter.
		 *
		 * @param context the context
		 * @author RayBa
		 * @date 05.07.2012
		 */
		public ChannelAdapter(Context context) {
			super(context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
			mContext = context;
			imageChacher = ImageLoader.getInstance();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.support.v4.widget.CursorAdapter#bindView(android.view.View,
		 * android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View view, Context context, Cursor c) {
			ViewHolder holder = (ViewHolder) view.getTag();
			imageChacher.cancelDisplayTask(holder.icon);
			holder.icon.setImageBitmap(null);
			String channelName = c.getString(c.getColumnIndex(ChannelTbl.NAME));
			String logoUrl = c.getString(c.getColumnIndex(ChannelTbl.LOGO_URL));
			String epgTitle = c.getString(c.getColumnIndex(EpgTbl.TITLE));
			long epgStart = c.getLong(c.getColumnIndex(EpgTbl.START));
			long epgEnd = c.getLong(c.getColumnIndex(EpgTbl.END));
			Integer position = c.getInt(c.getColumnIndex(ChannelTbl.POSITION));
			Integer favPosition = c.getInt(c.getColumnIndex(ChannelTbl.FAV_POSITION));
			holder.channelName.setText(channelName);
			if (TextUtils.isEmpty(epgTitle)) {
				holder.epgTime.setVisibility(View.GONE);
				holder.epgTitle.setVisibility(View.GONE);
				holder.progress.setVisibility(View.GONE);
			} else {
				holder.epgTitle.setVisibility(View.VISIBLE);
				holder.epgTime.setVisibility(View.VISIBLE);
				holder.progress.setVisibility(View.VISIBLE);
				String start = DateUtils.formatDateTime(context, epgStart, DateUtils.FORMAT_SHOW_TIME);
				String end = DateUtils.formatDateTime(context, epgEnd, DateUtils.FORMAT_SHOW_TIME);
				float timeAll = epgEnd - epgStart;
				float timeNow = new Date().getTime() - epgStart;
				float progress = timeNow / timeAll;
				holder.progress.setProgress((int) (progress * 100));
				holder.epgTime.setText(start + " - " + end);
				holder.epgTitle.setText(epgTitle);
			}
			holder.position.setText(!showFavs ? position.toString() : favPosition.toString());
			holder.contextMenu.setTag(c.getPosition());
			holder.iconContainer.setTag(c.getPosition());
			holder.v.setChecked(getListView().isItemChecked(c.getPosition()));
			
			if (!TextUtils.isEmpty(logoUrl)) {
				StringBuffer url = new StringBuffer(ServerConsts.REC_SERVICE_URL);
				url.append("/");
				url.append(logoUrl);
				imageChacher.displayImage(url.toString(), holder.icon);
			}else{
				holder.icon.setImageBitmap(null);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.support.v4.widget.CursorAdapter#newView(android.content.Context
		 * , android.database.Cursor, android.view.ViewGroup)
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater vi = getActivity().getLayoutInflater();
			ViewHolder holder = new ViewHolder();
			View view = vi.inflate(R.layout.list_row_channel, null);
			holder.v = (CheckableLinearLayout) view;
			holder.iconContainer = view.findViewById(R.id.iconContainer);
			holder.icon = (ImageView) view.findViewById(R.id.icon);
			holder.position = (TextView) view.findViewById(R.id.position);
			holder.channelName = (TextView) view.findViewById(R.id.title);
			holder.epgTime = (TextView) view.findViewById(R.id.epgTime);
			holder.progress = (ProgressBar) view.findViewById(R.id.progress);
			holder.epgTitle = (TextView) view.findViewById(R.id.epgTitle);
			holder.contextMenu = (ImageView) view.findViewById(R.id.contextMenu);
			holder.contextMenu.setOnClickListener(ChannelList.this);
			holder.iconContainer.setOnClickListener(ChannelList.this);
			view.setTag(holder);
			return view;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView
	 * , android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		selectedPosition = position;
		if (prefs.getBoolean(DVBViewerPreferences.KEY_SHOW_QUICK_STREAM_HINT, true)) {
			prefs.getPrefs().edit().putBoolean(DVBViewerPreferences.KEY_SHOW_QUICK_STREAM_HINT, false).commit();
			showQuickstreamHint(position);
		}else {
			ArrayList<Channel> chans = cursorToChannellist();
			EpgPager.CHANNELS = chans;
			if (mCHannelSelectedListener != null) {
				Cursor c = mAdapter.getCursor();
				c.moveToPosition(position);
				Channel chan = cursorToChannel(c);
				mCHannelSelectedListener.channelSelected(chans, chan, position);
				getListView().setItemChecked(position, true);
			} else {
				Intent epgPagerIntent = new Intent(getActivity(), EpgPagerActivity.class);
				// long[] feedIds = new long[data.getCount()];
				
				epgPagerIntent.putExtra("position", position);
				startActivity(epgPagerIntent);
				selectedPosition = ListView.INVALID_POSITION;
			}
		}
	}

	private void showQuickstreamHint(int position) {
		int wantedPosition = position;
		int firstPosition = getListView().getFirstVisiblePosition() - getListView().getHeaderViewsCount(); // This is the same as child #0
		int wantedChild = wantedPosition - firstPosition;
		View listItem = getListView().getChildAt(wantedChild);
		View icon = listItem.findViewById(R.id.icon);
		
		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		//can only dismiss by button click
		co.hideOnClickOutside = false;
		co.block = true;
		co.centerText = true;
		ViewTarget target = new ViewTarget(icon);
		ShowcaseView showCase = ShowcaseView.insertShowcaseView(target, getActivity(),
		        getActivity().getString(R.string.quick_stream_hint_title), getActivity().getString(R.string.quick_stream_hint_text), co);
		showCase.setBackgroundColor(getResources().getColor(R.color.black_transparent));
		showCase.show();
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		setTitle();
		if (!UIUtils.isTablet(getActivity())) {
			clearSelection();
		}
	}

	private void setTitle() {
		getActivity().setTitle(showFavs ? R.string.favourites : R.string.channelList);
	}

	/**
	 * Cursor to channellist.
	 *
	 * @param position the position
	 * @return the array list©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private ArrayList<Channel> cursorToChannellist() {
		Cursor c = (Cursor) mAdapter.getCursor();
		ArrayList<Channel> chans = new ArrayList<Channel>();
		c.moveToPosition(-1);
		while (c.moveToNext()) {
			Channel channel = cursorToChannel(c);
			chans.add(channel);
		}
		return chans;
	}

	/**
	 * Clears the selection of a ListView.
	 *
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private void clearSelection() {
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			getListView().setItemChecked(i, false);
		}
//		mAdapter.notifyDataSetChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_SELECTED_POSITION, selectedPosition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.contextMenu:
			selectedPosition = (Integer) v.getTag();
			getListView().showContextMenu();
			break;
		case R.id.iconContainer:
			try {
				selectedPosition = (Integer) v.getTag();
				Cursor c = mAdapter.getCursor();
				c.moveToPosition(selectedPosition);
				Channel chan = cursorToChannel(c);
				String videoUrl = StreamConfig.buildLiveUrl(getActivity(), chan.getPosition());
				Log.i("Stream", videoUrl);
				Intent videoIntent;
				String videoType = "video/mpeg";
				videoIntent = new Intent(Intent.ACTION_VIEW);
				videoIntent.setDataAndType(Uri.parse(videoUrl), videoType);
				getActivity().startActivity(videoIntent);
			} catch (ActivityNotFoundException e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(getResources().getString(R.string.noFlashPlayerFound)).setPositiveButton(getResources().getString(R.string.yes), null).setNegativeButton(getResources().getString(R.string.no), null).show();
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
	}

	/**
	 * Reads the current cursorposition to a Channel.
	 *
	 * @param c the c
	 * @return the Channel
	 * @author RayBa
	 * @date 13.05.2012
	 */
	private Channel cursorToChannel(Cursor c) {
		Channel channel = new Channel();
		channel.setId(c.getLong(c.getColumnIndex(ChannelTbl._ID)));
		channel.setChannelID(c.getLong(c.getColumnIndex(ChannelTbl.CHANNEL_ID)));
		channel.setEpgID(c.getLong(c.getColumnIndex(ChannelTbl.EPG_ID)));
		channel.setLogoUrl(c.getString(c.getColumnIndex(ChannelTbl.LOGO_URL)));
		String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
		channel.setName(name);
		channel.setPosition(c.getInt(c.getColumnIndex(ChannelTbl.POSITION)));
		return channel;
	}
	
	/**
	 * Cursor to timer.
	 *
	 * @param c the c
	 * @return the timer©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private Timer cursorToTimer(Cursor c) {
		String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
		long channelID = c.getLong(c.getColumnIndex(ChannelTbl.CHANNEL_ID));
		String epgTitle = !c.isNull(c.getColumnIndex(EpgTbl.TITLE)) ? c.getString(c.getColumnIndex(EpgTbl.TITLE)) : name;
		long epgStart = c.getLong(c.getColumnIndex(EpgTbl.START));
		long epgEnd =  c.getLong(c.getColumnIndex(EpgTbl.END));
		DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
		int epgBefore = prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, 5);
		int epgAfter = prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, 5);
		Date start = epgStart > 0 ? new Date(epgStart) : new Date();
		Date end = epgEnd > 0 ? new Date(epgEnd) : new Date(start.getTime()+(1000*60*120));
		Log.i(ChannelList.class.getSimpleName(), "start: "+start.toString());
		Log.i(ChannelList.class.getSimpleName(), "end: "+end.toString());
		start = DateUtils.addMinutes(start, 0 - epgBefore);
		end = DateUtils.addMinutes(end, epgAfter);
		Timer timer = new Timer();
		timer.setTitle(epgTitle);
		timer.setChannelId(channelID);
		timer.setChannelName(name);
		timer.setStart(start);
		timer.setEnd(end);
		timer.setTimerAction(prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, 0));
		return timer;
	}

	/**
	 * The listener interface for receiving onChannelSelected events.
	 * The class that is interested in processing a onChannelSelected
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addOnChannelSelectedListener<code> method. When
	 * the onChannelSelected event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see OnChannelSelectedEvent
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public static interface OnChannelSelectedListener {

		/**
		 * Channel selected.
		 *
		 * @param chans the chans
		 * @param chan the chan
		 * @param position the position
		 * @author RayBa
		 * @date 05.07.2012
		 */
		public void channelSelected(List<Channel> chans, Channel chan, int position);

	}

	/**
	 * Sets the selected position.
	 *
	 * @param selectedPosition the new selected position
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public void setSelectedPosition(int selectedPosition) {
		this.selectedPosition = selectedPosition;
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseListFragment#setSelection(int)
	 */
	@Override
	public void setSelection(int position) {
		clearSelection();
		getListView().setItemChecked(position, true);
		setSelectedPosition(position);
		super.setSelection(position);
	}

	/**
	 * Checks if is show favs.
	 *
	 * @return true, if is show favs
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public boolean isShowFavs() {
		return showFavs;
	}

}
