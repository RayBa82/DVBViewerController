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

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.ViewTarget;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts;
import org.dvbviewer.controller.data.DbConsts.ChannelTbl;
import org.dvbviewer.controller.data.DbConsts.EpgTbl;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.io.ServerRequest.DVBViewerCommand;
import org.dvbviewer.controller.io.ServerRequest.RecordingServiceGet;
import org.dvbviewer.controller.io.UrlBuilderException;
import org.dvbviewer.controller.ui.base.BaseListFragment;
import org.dvbviewer.controller.ui.phone.StreamConfigActivity;
import org.dvbviewer.controller.ui.phone.TimerDetailsActivity;
import org.dvbviewer.controller.ui.tablet.ChannelListMultiActivity;
import org.dvbviewer.controller.ui.widget.CheckableLinearLayout;
import org.dvbviewer.controller.utils.AnalyticsTracker;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;

import java.text.MessageFormat;
import java.util.Date;

/**
 * The Class ChannelList.
 *
 * @author RayBa
 */
public class ChannelList extends BaseListFragment implements LoaderCallbacks<Cursor>, OnClickListener, PopupMenu.OnMenuItemClickListener {

    public static final String                      KEY_SELECTED_POSITION       = "SELECTED_POSITION";
    public static final String                      KEY_HAS_OPTIONMENU          = "HAS_OPTIONMENU";
    public static final String                      KEY_GROUP_ID      	        = EpgPager.class.getName() + "KEY_GROUP_ID";
    public static final String                      KEY_CHANNEL_INDEX 	        = EpgPager.class.getName() + "KEY_CHANNEL_INDEX";
    public static final int                         LOADER_REFRESH_CHANNELLIST  = 100;
    public static final int                         LOADER_CHANNELLIST          = 101;
    public static final int                         LOADER_EPG                  = 103;
    private             int                         selectedPosition            = -1;
    private             long                        mGroupId                    = -1;
    private             int                         mGroupIndex                 = -1;
    private             int                         mChannelIndex               = -1;
    private             boolean                     hasOptionsMenu              = true;
    private             boolean                     showFavs;
    private             boolean                     showNowPlaying;
    private             boolean                     showNowPlayingWifi;
    private             DVBViewerPreferences        prefs;
    private             ChannelAdapter              mAdapter;
    private             OnChannelSelectedListener   mCHannelSelectedListener;
    private             NetworkInfo                 mNetworkInfo;

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            if (getArguments().containsKey(ChannelList.KEY_GROUP_ID)) {
                mGroupId = getArguments().getLong(KEY_GROUP_ID);
            }
            if (getArguments().containsKey(ChannelList.KEY_CHANNEL_INDEX)) {
                mChannelIndex = getArguments().getInt(KEY_CHANNEL_INDEX, mChannelIndex);
            }
            if (getArguments().containsKey(ChannelPager.KEY_GROUP_INDEX)) {
                mGroupIndex = getArguments().getInt(ChannelPager.KEY_GROUP_INDEX);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChannelSelectedListener) {
            mCHannelSelectedListener = (OnChannelSelectedListener) context;
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
        int loaderId = LOADER_CHANNELLIST;
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
        Loader<Cursor> loader;
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
        loader = new CursorLoader(getActivity().getApplicationContext(), ChannelTbl.CONTENT_URI_NOW, null, selection.toString(), null, orderBy);
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
        mAdapter.swapCursor(cursor);
        if (selectedPosition != ListView.INVALID_POSITION) {
            getListView().setItemChecked(selectedPosition, true);
        }
        getListView().setSelectionFromTop(selectedPosition, (int) getResources().getDimension(R.dimen.list_preferred_item_height_small) * 3);
        setListShown(true);
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
        arg0.reset();
        if (isVisible()) {
            setListShown(true);
        }
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


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(selectedPosition);
        switch (item.getItemId()) {
            case R.id.menuTimer:
                showTimerDialog(c);
                return true;
            case R.id.menuStream:
                showStreamConfig(c);
                return true;
            case R.id.menuSwitch:
                switchChannel(c);
                return true;
            case R.id.menuRecord:
                recordChannel(c);
                return true;

            default:
                break;
        }
        return false;
    }

    private void switchChannel(Cursor c) {
        Channel chan = cursorToChannel(c);
        StringBuilder cid = new StringBuilder(":").append(chan.getChannelID());
        final String url = ServerConsts.REC_SERVICE_URL + ServerConsts.URL_SWITCH_COMMAND;
        String switchRequest = MessageFormat.format(url, prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT), cid);
        DVBViewerCommand command = new DVBViewerCommand(switchRequest);
        Thread executerThread = new Thread(command);
        executerThread.start();
    }

    private void recordChannel(Cursor c) {
        Timer timer = cursorToTimer(c);
        String request = TimerDetails.buildTimerUrl(timer);
        RecordingServiceGet rsGet = new RecordingServiceGet(request);
        Thread executionThread = new Thread(rsGet);
        executionThread.start();
    }

    private void showTimerDialog(Cursor c) {
        Timer timer = cursorToTimer(c);
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

    private void showStreamConfig(Cursor cursor) {
        Channel chan = cursorToChannel(cursor);
        if (UIUtils.isTablet(getActivity())) {
            StreamConfig cfg = StreamConfig.newInstance();
            Bundle arguments = new Bundle();
            arguments.putInt(StreamConfig.EXTRA_FILE_ID, chan.getPosition());
            arguments.putInt(StreamConfig.EXTRA_FILE_TYPE, StreamConfig.FILE_TYPE_LIVE);
            arguments.putInt(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig);
            cfg.setArguments(arguments);
            cfg.show(getActivity().getSupportFragmentManager(), StreamConfig.class.getName());
        } else {
            Intent streamConfig = new Intent(getActivity(), StreamConfigActivity.class);
            streamConfig.putExtra(StreamConfig.EXTRA_FILE_ID, chan.getPosition());
            streamConfig.putExtra(StreamConfig.EXTRA_FILE_TYPE, StreamConfig.FILE_TYPE_LIVE);
            streamConfig.putExtra(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig);
            startActivity(streamConfig);
        }
    }

    /**
     * Persist channel config config.
     *
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
     */
    public void refresh(int id) {
        getLoaderManager().restartLoader(id, getArguments(), this);
        setListShown(false);
    }

    public int getChannelIndex() {
        return selectedPosition;
    }

    /**
     * The Class ViewHolder.
     *
     * @author RayBa
     */
    private static class ViewHolder {
        CheckableLinearLayout v;
        ImageView icon;
        View iconContainer;
        TextView position;
        TextView channelName;
        TextView epgTime;
        ProgressBar progress;
        TextView epgTitle;
        ImageView contextMenu;
    }

    /**
     * The Class ChannelAdapter.
     *
     * @author RayBa
     */
    public class ChannelAdapter extends CursorAdapter {

        ImageLoader imageChacher;

        /**
         * Instantiates a new channel adapter.
         *
         * @param context the context
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
            holder.contextMenu.setTag(AdapterView.INVALID_POSITION);
            holder.iconContainer.setTag(AdapterView.INVALID_POSITION);
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
                imageChacher.displayImage(ServerConsts.REC_SERVICE_URL + "/" + logoUrl, holder.icon);
            } else {
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
            View view = LayoutInflater.from(context).inflate(R.layout.list_row_channel, parent, false);
            ViewHolder holder = new ViewHolder();
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
        } else {
            if (mCHannelSelectedListener != null) {
                Cursor c = mAdapter.getCursor();
                c.moveToPosition(position);
                Channel chan = cursorToChannel(c);
                mCHannelSelectedListener.channelSelected(-1, -1, chan, position);
                getListView().setItemChecked(position, true);
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
     * Clears the selection of a ListView.
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
        selectedPosition = (Integer) v.getTag();
        switch (v.getId()) {
            case R.id.contextMenu:
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.context_menu_channellist, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
                break;
            case R.id.iconContainer:
                try {
                    Cursor c = mAdapter.getCursor();
                    c.moveToPosition(selectedPosition);
                    Channel chan = cursorToChannel(c);
                    try {
                        getActivity().startActivity(StreamConfig.buildLiveUrl(getActivity(), chan.getPosition()));
                        AnalyticsTracker.trackQuickStream(getActivity().getApplication());
                    } catch (UrlBuilderException e) {
                        e.printStackTrace();
                    }
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
     */
    public static Channel cursorToChannel(Cursor c) {
        final Channel channel = new Channel();
        channel.setId(c.getLong(c.getColumnIndex(ChannelTbl._ID)));
        channel.setChannelID(c.getLong(c.getColumnIndex(ChannelTbl.CHANNEL_ID)));
        channel.setEpgID(c.getLong(c.getColumnIndex(ChannelTbl.EPG_ID)));
        channel.setLogoUrl(c.getString(c.getColumnIndex(ChannelTbl.LOGO_URL)));
        channel.setName(c.getString(c.getColumnIndex(ChannelTbl.NAME)));
        channel.setPosition(c.getInt(c.getColumnIndex(ChannelTbl.POSITION)));
        channel.setFavPosition(c.getInt(c.getColumnIndex(ChannelTbl.FAV_POSITION)));
        return channel;
    }

    /**
     * Cursor to timer.
     *
     * @param c the c
     * @return the timer©
     */
    private Timer cursorToTimer(Cursor c) {
        String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
        long channelID = c.getLong(c.getColumnIndex(ChannelTbl.CHANNEL_ID));
        String epgTitle = !c.isNull(c.getColumnIndex(EpgTbl.TITLE)) ? c.getString(c.getColumnIndex(EpgTbl.TITLE)) : name;
        long epgStart = c.getLong(c.getColumnIndex(EpgTbl.START));
        long epgEnd = c.getLong(c.getColumnIndex(EpgTbl.END));
        DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
        int epgBefore = prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, 5);
        int epgAfter = prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, 5);
        Date start = epgStart > 0 ? new Date(epgStart) : new Date();
        Date end = epgEnd > 0 ? new Date(epgEnd) : new Date(start.getTime() + (1000 * 60 * 120));
        Log.i(ChannelList.class.getSimpleName(), "start: " + start.toString());
        Log.i(ChannelList.class.getSimpleName(), "end: " + end.toString());
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
     * @author RayBa
     */
    public interface OnChannelSelectedListener {

        /**
         * Channel selected.
         *
         * @param chan     the chan
         */
        void channelSelected(long groupId, int groupIndex, Channel chan, int channelIndex);

    }

    /**
     * Sets the selected position.
     *
     * @param selectedPosition the new selected position
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

}
