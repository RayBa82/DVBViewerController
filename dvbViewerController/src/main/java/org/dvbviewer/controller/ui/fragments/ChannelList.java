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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import org.dvbviewer.controller.ui.widget.CheckableLinearLayout;
import org.dvbviewer.controller.utils.AnalyticsTracker;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.FileType;
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

    public static final Uri                         BASE_CONTENT_URI	        = Uri.parse("content://org.dvbviewer.controller/channelselector");
    public static final String                      KEY_CHANNEL_INDEX 	        = ChannelList.class.getName() + "KEY_CHANNEL_INDEX";
    private static final int                        LOADER_CHANNELLIST          = 101;
    private             long                        mGroupId                    = -1;
    private             int                         mGroupIndex                 = -1;
    private             int                         mChannelIndex               = -1;
    private             boolean                     showFavs;
    private             DVBViewerPreferences        prefs;
    private             ChannelAdapter              mAdapter;
    private             OnChannelSelectedListener   mCHannelSelectedListener;
    private             ChannelPagedObserver        mChannelPagedOberserver;

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new DVBViewerPreferences(getContext());
        showFavs = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false);
        mAdapter = new ChannelAdapter(getContext());
        getExtras(savedInstanceState);
        registerObserver();
    }

    private void getExtras(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (getArguments().containsKey(ChannelPager.KEY_GROUP_ID)) {
                mGroupId = getArguments().getLong(ChannelPager.KEY_GROUP_ID);
            }
            if (getArguments().containsKey(ChannelPager.KEY_GROUP_INDEX)) {
                mGroupIndex = getArguments().getInt(ChannelPager.KEY_GROUP_INDEX);
            }
            mChannelIndex = getArguments().getInt(KEY_CHANNEL_INDEX, mChannelIndex);
        }else{
            mGroupId = savedInstanceState.getLong(ChannelPager.KEY_GROUP_ID);
            mGroupIndex = savedInstanceState.getInt(ChannelPager.KEY_GROUP_INDEX);
            mChannelIndex = savedInstanceState.getInt(KEY_CHANNEL_INDEX, mChannelIndex);
        }
    }

    private void registerObserver() {
        final Handler handler = new Handler();
        final Uri contentUri = BASE_CONTENT_URI.buildUpon().appendPath(String.valueOf(mGroupId)).appendQueryParameter("index", String.valueOf(mChannelIndex)).build();
        mChannelPagedOberserver = new ChannelPagedObserver(handler);
        getContext().getContentResolver().registerContentObserver(contentUri, true, mChannelPagedOberserver);
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
        setEmptyText(showFavs ? getResources().getString(R.string.no_favourites) : getResources().getString(R.string.no_channels));
        Loader<Cursor> loader = getLoaderManager().initLoader(LOADER_CHANNELLIST, savedInstanceState, this);
        setListShown(!(!isResumed() || loader.isStarted()));
        setSelection(mChannelIndex);
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
        StringBuilder selection = new StringBuilder(ChannelTbl.FLAGS + " & " + Channel.FLAG_ADDITIONAL_AUDIO + "== 0");
        if (mGroupId > 0) {
            selection.append(" and ");
            selection.append(ChannelTbl.GROUP_ID).append(" = ").append(mGroupId);
        }

        loader = new CursorLoader(getContext(), ChannelTbl.CONTENT_URI_NOW, null, selection.toString(), null, ChannelTbl.POSITION);
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
        mAdapter.changeCursor(cursor);
        setSelection(mChannelIndex);
        getListView().setSelectionFromTop(mChannelIndex, (int) getResources().getDimension(R.dimen.list_preferred_item_height_small) * 3);
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




    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(mChannelIndex);
        switch (item.getItemId()) {
            case R.id.menuStreamDirect:
                streamDirect(c);
                return true;
            case R.id.menuStreamTranscoded:
                streamTranscoded(c);
                return true;
            case R.id.menuStreamConfig:
                showStreamConfig(c);
                return true;
            case R.id.menuTimer:
            showTimerDialog(c);
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

    private void streamDirect(final Cursor c) {
        try {
            Channel chan = cursorToChannel(c);
            final Intent videoIntent = StreamConfig.getDirectUrl(chan.getChannelID(), chan.getName(), FileType.CHANNEL);
            getActivity().startActivity(videoIntent);
            prefs.getStreamPrefs().edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true).apply();
            AnalyticsTracker.trackQuickRecordingStream(getActivity().getApplication());
        } catch (ActivityNotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getResources().getString(R.string.noFlashPlayerFound)).setPositiveButton(getResources().getString(R.string.yes), null).setNegativeButton(getResources().getString(R.string.no), null).show();
            e.printStackTrace();
        }
    }

    private void streamTranscoded(final Cursor c) {
        try {
            Channel chan = cursorToChannel(c);
            final Intent videoIntent = StreamConfig.getTranscodedUrl(getContext(), chan.getChannelID(), chan.getName(), FileType.CHANNEL);
            getActivity().startActivity(videoIntent);
            prefs.getStreamPrefs().edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, false).apply();
            AnalyticsTracker.trackQuickRecordingStream(getActivity().getApplication());
        } catch (ActivityNotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getResources().getString(R.string.noFlashPlayerFound)).setPositiveButton(getResources().getString(R.string.yes), null).setNegativeButton(getResources().getString(R.string.no), null).show();
            e.printStackTrace();
        } catch (UrlBuilderException e) {
            e.printStackTrace();
        }
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
        if (UIUtils.isTablet(getContext())) {
            TimerDetails timerdetails = TimerDetails.newInstance();
            Bundle args = TimerDetails.buildBundle(timer);
            timerdetails.setArguments(args);
            timerdetails.show(getActivity().getSupportFragmentManager(), TimerDetails.class.getName());
        } else {
            Intent timerIntent = new Intent(getContext(), TimerDetailsActivity.class);
            Bundle extras = TimerDetails.buildBundle(timer);
            timerIntent.putExtras(extras);
            startActivity(timerIntent);
        }
    }

    private void showStreamConfig(Cursor cursor) {
        Channel chan = cursorToChannel(cursor);
        if (UIUtils.isTablet(getContext())) {
            Bundle arguments = getIntentExtras(chan);
            StreamConfig cfg = StreamConfig.newInstance();
            cfg.setArguments(arguments);
            cfg.show(getActivity().getSupportFragmentManager(), StreamConfig.class.getName());
        } else {
            Bundle arguments = getIntentExtras(chan);
            Intent streamConfig = new Intent(getContext(), StreamConfigActivity.class);
            streamConfig.putExtras(arguments);
            startActivity(streamConfig);
        }
    }

    @NonNull
    private Bundle getIntentExtras(Channel chan) {
        Bundle arguments = new Bundle();
        arguments.putLong(StreamConfig.EXTRA_FILE_ID, chan.getChannelID());
        arguments.putParcelable(StreamConfig.EXTRA_FILE_TYPE, FileType.CHANNEL);
        arguments.putInt(StreamConfig.EXTRA_DIALOG_TITLE_RES, R.string.streamConfig);
        arguments.putString(StreamConfig.EXTRA_TITLE, chan.getName());
        return arguments;
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

        final ImageLoader imageChacher;

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
            holder.position.setText(position.toString());
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
        mChannelIndex = position;
        if (prefs.getBoolean(DVBViewerPreferences.KEY_SHOW_QUICK_STREAM_HINT, true)) {
            prefs.getPrefs().edit().putBoolean(DVBViewerPreferences.KEY_SHOW_QUICK_STREAM_HINT, false).commit();
            showQuickstreamHint(position);
        } else {
            if (mCHannelSelectedListener != null) {
                mCHannelSelectedListener.channelSelected(mGroupId, mGroupIndex, position);
                getListView().setItemChecked(position, true);
            }
        }
    }

    private void showQuickstreamHint(int position) {
        int firstPosition = getListView().getFirstVisiblePosition() - getListView().getHeaderViewsCount(); // This is the same as child #0
        int wantedChild = position - firstPosition;
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
        outState.putLong(ChannelPager.KEY_GROUP_ID, mGroupId);
        outState.putInt(ChannelPager.KEY_GROUP_INDEX, mGroupIndex);
        outState.putInt(KEY_CHANNEL_INDEX, mChannelIndex);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        mChannelIndex = (Integer) v.getTag();
        switch (v.getId()) {
            case R.id.contextMenu:
                PopupMenu popup = new PopupMenu(getContext(), v);
                popup.inflate(R.menu.context_menu_stream);
                popup.inflate(R.menu.context_menu_channellist);
                popup.setOnMenuItemClickListener(this);
                popup.show();
                break;
            case R.id.iconContainer:
                try {
                    Cursor c = mAdapter.getCursor();
                    c.moveToPosition(mChannelIndex);
                    Channel chan = cursorToChannel(c);
                    try {

                        final Intent videoIntent = StreamConfig.buildQuickUrl(getContext(), chan.getChannelID(), chan.getName(), FileType.CHANNEL);
                        getActivity().startActivity(videoIntent);
                        AnalyticsTracker.trackQuickStream(getActivity().getApplication());
                    } catch (UrlBuilderException e) {
                        e.printStackTrace();
                    }
                } catch (ActivityNotFoundException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        final String name = c.getString(c.getColumnIndex(ChannelTbl.NAME));
        final long channelID = c.getLong(c.getColumnIndex(ChannelTbl.CHANNEL_ID));
        final String epgTitle = !c.isNull(c.getColumnIndex(EpgTbl.TITLE)) ? c.getString(c.getColumnIndex(EpgTbl.TITLE)) : name;
        final long epgStart = c.getLong(c.getColumnIndex(EpgTbl.START));
        final long epgEnd = c.getLong(c.getColumnIndex(EpgTbl.END));
        final DVBViewerPreferences prefs = new DVBViewerPreferences(getContext());
        final int epgBefore = prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, DVBViewerPreferences.DEFAULT_TIMER_TIME_BEFORE);
        final int epgAfter = prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, DVBViewerPreferences.DEFAULT_TIMER_TIME_AFTER);
        final Date start = epgStart > 0 ? new Date(epgStart) : new Date();
        final Date end = epgEnd > 0 ? new Date(epgEnd) : new Date(start.getTime() + (1000 * 60 * 120));
        final String eventId = c.getString(c.getColumnIndex(EpgTbl.EVENT_ID));
        final String pdc = c.getString(c.getColumnIndex(EpgTbl.PDC));
        Timer timer = new Timer();
        timer.setTitle(epgTitle);
        timer.setChannelId(channelID);
        timer.setChannelName(name);
        timer.setStart(start);
        timer.setEnd(end);
        timer.setPre(epgBefore);
        timer.setPost(epgAfter);
        timer.setEventId(eventId);
        timer.setPdc(pdc);
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
         * Notifys about channel selections in the channel List
         *
         * @param groupId the groupId
         * @param groupIndex the groupIndex
         * @param channelIndex the channelIndex
         */
        void channelSelected(long groupId, int groupIndex, int channelIndex);

    }

    /**
     * Sets the selected position.
     *
     * @param selectedPosition the new selected position
     */
    private void setSelectedPosition(int selectedPosition) {
        this.mChannelIndex = selectedPosition;
    }

    /* (non-Javadoc)
     * @see org.dvbviewer.controller.ui.base.BaseListFragment#setSelection(int)
     */
    @Override
    public void setSelection(int position) {
        setSelectedPosition(position);
        clearSelection();
        getListView().setItemChecked(position, true);
        super.setSelection(position);
    }

    class ChannelPagedObserver extends ContentObserver {

        public ChannelPagedObserver(Handler handler){
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null){
                int index = Integer.parseInt(uri.getQueryParameter("index"));
                setSelection(index);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().getContentResolver().unregisterContentObserver(mChannelPagedOberserver);
    }
}
