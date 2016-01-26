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
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.HttpUrl;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts.EpgTbl;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.EpgEntry;
import org.dvbviewer.controller.entities.IEPG;
import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.io.HTTPUtil;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.ServerRequest.DVBViewerCommand;
import org.dvbviewer.controller.io.ServerRequest.RecordingServiceGet;
import org.dvbviewer.controller.io.UrlBuilderException;
import org.dvbviewer.controller.io.data.EpgEntryHandler;
import org.dvbviewer.controller.ui.base.BaseListFragment;
import org.dvbviewer.controller.ui.base.EpgLoader;
import org.dvbviewer.controller.ui.phone.IEpgDetailsActivity;
import org.dvbviewer.controller.ui.phone.TimerDetailsActivity;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * The Class ChannelEpg.
 *
 * @author RayBa
 */
public class ChannelEpg extends BaseListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener, OnClickListener, PopupMenu.OnMenuItemClickListener {
    public static final String KEY_CHANNEL_NAME = "KEY_CHANNEL_NAME";
    public static final String KEY_CHANNEL_ID = "KEY_CHANNEL_ID";
    public static final String KEY_CHANNEL_LOGO = "KEY_CHANNEL_LOGO";
    public static final String KEY_CHANNEL_POS = "KEY_CHANNEL_POS";
    public static final String KEY_FAV_POS = "KEY_FAV_POS";
    public static final String KEY_EPG_ID = "KEY_EPG_ID";
    public static final String KEY_EPG_DAY = "EPG_DAY";
    private ChannelEPGAdapter mAdapter;
    private String channel;
    private long channelId;
    private long epgId;
    private String logoUrl;
    private int channelPos;
    private int favPos;
    private int selectedPosition;
    private ImageView channelLogo;
    private TextView channelName;
    private TextView dayIndicator;
    private EpgDateInfo mDateInfo;
    private Date lastRefresh;

    @Override
    protected int getLayoutRessource() {
        return R.layout.fragment_channel_epg;
    }

    /* (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mDateInfo = (EpgDateInfo) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageLoader mImageCacher = ImageLoader.getInstance();
        fillFromBundle(getArguments());
        mAdapter = new ChannelEPGAdapter(getActivity());
        setListAdapter(mAdapter);
        setListShown(false);
        getListView().setOnItemClickListener(this);
        channelLogo.setImageBitmap(null);
        if (channel != null) {
            mImageCacher.cancelDisplayTask(channelLogo);
            String url = ServerConsts.REC_SERVICE_URL + "/" + logoUrl;
            mImageCacher.displayImage(url, channelLogo);
            channelName.setText(channel);
        }
        if (DateUtils.isToday(mDateInfo.getEpgDate().getTime())) {
            dayIndicator.setText(R.string.today);
        } else if (DateUtils.isTomorrow(mDateInfo.getEpgDate().getTime())) {
            dayIndicator.setText(R.string.tomorrow);
        } else {
            dayIndicator.setText(DateUtils.formatDateTime(getActivity(), mDateInfo.getEpgDate().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY));
        }

        setEmptyText(getResources().getString(R.string.no_epg));
        getLoaderManager().initLoader(channelPos, savedInstanceState, this);
    }

    private void fillFromBundle(Bundle savedInstanceState) {
        channel = savedInstanceState.getString(KEY_CHANNEL_NAME);
        channelId = savedInstanceState.getLong(KEY_CHANNEL_ID);
        epgId = savedInstanceState.getLong(KEY_EPG_ID);
        logoUrl = savedInstanceState.getString(KEY_CHANNEL_LOGO);
        channelPos = savedInstanceState.getInt(KEY_CHANNEL_POS);
        favPos = savedInstanceState.getInt(KEY_FAV_POS);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#setUserVisibleHint(boolean)
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            refreshDate();
        }
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
     */
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Loader<Cursor> loader = new EpgLoader<Cursor>(getContext(), mDateInfo) {

            @Override
            protected void onForceLoad() {
                super.onForceLoad();
                setListShown(false);
            }

            @Override
            public Cursor loadInBackground() {
                MatrixCursor cursor = null;
                try {
                    List<EpgEntry> result;
                    Date now = mDateInfo.getEpgDate();
                    String nowFloat = DateUtils.getFloatDate(now);
                    Date tommorrow = DateUtils.addDay(now);
                    String tommorrowFloat = DateUtils.getFloatDate(tommorrow);
                    HttpUrl.Builder builder = buildBaseEpgUrl()
                            .addQueryParameter("channel", String.valueOf(epgId))
                            .addQueryParameter("start", String.valueOf(nowFloat))
                            .addQueryParameter("end", String.valueOf(tommorrowFloat));
                    EpgEntryHandler handler = new EpgEntryHandler();
                    String xml = ServerRequest.getRSString(builder.build().toString());
                    result = handler.parse(xml);
                    if (result != null && !result.isEmpty()) {
                        String[] columnNames = new String[]{EpgTbl._ID, EpgTbl.EPG_ID, EpgTbl.TITLE, EpgTbl.SUBTITLE, EpgTbl.DESC, EpgTbl.START, EpgTbl.END};
                        cursor = new MatrixCursor(columnNames);
                        for (EpgEntry entry : result) {
                            cursor.addRow(new Object[]{entry.getId(), entry.getEpgID(), entry.getTitle(), entry.getSubTitle(), entry.getDescription(), entry.getStart().getTime(), entry.getEnd().getTime()});
                        }
                    }

                } catch (Exception e) {
                    catchException(getClass().getSimpleName(), e);
                }
                return cursor;
            }
        };

        return loader;
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        mAdapter.changeCursor(cursor);
        setSelection(0);
        if (DateUtils.isToday(mDateInfo.getEpgDate().getTime())) {
            dayIndicator.setText(R.string.today);
        } else if (DateUtils.isTomorrow(mDateInfo.getEpgDate().getTime())) {
            dayIndicator.setText(R.string.tomorrow);
        } else {
            dayIndicator.setText(DateUtils.formatDateTime(getActivity(), mDateInfo.getEpgDate().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY));
        }
        lastRefresh = mDateInfo.getEpgDate();
        setListShown(true);
    }

    /* (non-Javadoc)
     * @see org.dvbviewer.controller.ui.base.BaseListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null){
            channelLogo = (ImageView) v.findViewById(R.id.icon);
            channelName = (TextView) v.findViewById(R.id.title);
            dayIndicator = (TextView) v.findViewById(R.id.dayIndicator);
        }
        return v;
    }

    /**
     * The Class ViewHolder.
     *
     * @author RayBa
     */
    private static class ViewHolder {
        TextView startTime;
        TextView title;
        TextView description;
        ImageView contextMenu;
    }

    /* (non-Javadoc)
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);
        IEPG entry = cursorToEpgEntry(c);

        Intent i = new Intent(getActivity(), IEpgDetailsActivity.class);
        i.putExtra(IEPG.class.getSimpleName(), entry);
        startActivity(i);
    }

    /**
     * Reads the current cursorposition to an EpgEntry.
     *
     * @param c the c
     * @return the iEPG©
     */
    private IEPG cursorToEpgEntry(Cursor c) {
        IEPG entry = new EpgEntry();
        entry.setChannel(channel);
        entry.setDescription(c.getString(c.getColumnIndex(EpgTbl.DESC)));
        entry.setEnd(new Date(c.getLong(c.getColumnIndex(EpgTbl.END))));
        entry.setEpgID(epgId);
        entry.setStart(new Date(c.getLong(c.getColumnIndex(EpgTbl.START))));
        entry.setSubTitle(c.getString(c.getColumnIndex(EpgTbl.SUBTITLE)));
        entry.setTitle(c.getString(c.getColumnIndex(EpgTbl.TITLE)));
        return entry;
    }

    /**
     * The Class ChannelEPGAdapter.
     *
     * @author RayBa
     */
    public class ChannelEPGAdapter extends CursorAdapter {


        /**
         * Instantiates a new channel epg adapter.
         *
         * @param context the context
         */
        public ChannelEPGAdapter(Context context) {
            super(context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        }

        /* (non-Javadoc)
         * @see android.support.v4.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
         */
        @Override
        public void bindView(View view, Context context, Cursor c) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.contextMenu.setTag(c.getPosition());
            long millis = c.getLong(c.getColumnIndex(EpgTbl.START));
            int flags = DateUtils.FORMAT_SHOW_TIME;
            String date = DateUtils.formatDateTime(getActivity(), millis, flags);
            holder.startTime.setText(date);
            holder.title.setText(c.getString(c.getColumnIndex(EpgTbl.TITLE)));
            String subTitle = c.getString(c.getColumnIndex(EpgTbl.SUBTITLE));
            String desc = c.getString(c.getColumnIndex(EpgTbl.DESC));
            holder.description.setText(TextUtils.isEmpty(subTitle) ? desc : subTitle);
            holder.description.setVisibility(TextUtils.isEmpty(holder.description.getText()) ? View.GONE : View.VISIBLE);
        }

        /* (non-Javadoc)
         * @see android.support.v4.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_row_epg, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.startTime = (TextView) view.findViewById(R.id.startTime);
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.description = (TextView) view.findViewById(R.id.description);
            holder.contextMenu = (ImageView) view.findViewById(R.id.contextMenu);
            holder.contextMenu.setOnClickListener(ChannelEpg.this);
            view.setTag(holder);
            return view;
        }

    }

    public static class ToolbarActionClickListener implements Toolbar.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            return false;
        }
    }

    /**
     * Refresh.
     *
     * @param force the force
     */
    public void refresh(boolean force) {
        setListShown(false);
        getLoaderManager().restartLoader(channelPos, getArguments(), this);
    }

    /**
     * Refresh date.
     *
     */
    public void refreshDate() {
        if (lastRefresh != null && lastRefresh.getTime() != mDateInfo.getEpgDate().getTime()) {
            refresh(true);
        }
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CHANNEL_NAME, channel);
        outState.putLong(KEY_CHANNEL_ID, channelId);
        outState.putLong(KEY_EPG_ID, epgId);
        outState.putString(KEY_CHANNEL_LOGO, logoUrl);
        outState.putInt(KEY_EPG_ID, channelPos);
        outState.putInt(KEY_EPG_ID, favPos);
        outState.putLong(KEY_EPG_DAY, mDateInfo.getEpgDate().getTime());
    }

    /* (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.channel_epg, menu);
        menu.findItem(R.id.menuPrev).setEnabled(!DateUtils.isToday(mDateInfo.getEpgDate().getTime()));
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contextMenu:
                selectedPosition = (int) v.getTag();
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.context_menu_epg, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final Cursor c = mAdapter.getCursor();
        c.moveToPosition(selectedPosition);
        final int pos = selectedPosition;
        Timer timer;
            switch (item.getItemId()) {
                case R.id.menuRecord:
                    timer = cursorToTimer(c);
                    String url = TimerDetails.buildTimerUrl(timer);
                    RecordingServiceGet rsGet = new RecordingServiceGet(url);
                    Thread executionThread = new Thread(rsGet);
                    executionThread.start();
                    return true;
                case R.id.menuTimer:
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
                    }else{
                        Intent timerIntent = new Intent(getActivity(), TimerDetailsActivity.class);
                        timerIntent.putExtra(TimerDetails.EXTRA_TITLE, timer.getTitle());
                        timerIntent.putExtra(TimerDetails.EXTRA_CHANNEL_NAME, timer.getChannelName());
                        timerIntent.putExtra(TimerDetails.EXTRA_CHANNEL_ID, timer.getChannelId());
                        timerIntent.putExtra(TimerDetails.EXTRA_START, timer.getStart().getTime());
                        timerIntent.putExtra(TimerDetails.EXTRA_END, timer.getEnd().getTime());
                        timerIntent.putExtra(TimerDetails.EXTRA_ACTIVE, true);
                        startActivity(timerIntent);
                    }
                    return true;
                case R.id.menuDetails:
                    Intent details = new Intent(getActivity(), IEpgDetailsActivity.class);
                    c.moveToPosition(pos);
                    IEPG entry = cursorToEpgEntry(c);
                    details.putExtra(IEPG.class.getSimpleName(), entry);
                    startActivity(details);
                    return true;
                case R.id.menuSwitch:
                    DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
                    String cid = ":" + String.valueOf(channelId);
                    String switchRequest = MessageFormat.format(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_SWITCH_COMMAND, prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT), cid);
                    DVBViewerCommand command = new DVBViewerCommand(switchRequest);
                    Thread exexuterTHread = new Thread(command);
                    exexuterTHread.start();
                    return true;
                default:
                    break;
            }
        return false;
    }


    /**
     * The Interface EpgDateInfo.
     *
     * @author RayBa
     */
    public interface EpgDateInfo {

        /**
         * Sets the epg date.
         *
         * @param date the new epg date
         */
        void setEpgDate(Date date);

        /**
         * Gets the epg date.
         *
         * @return the epg date
         */
        Date getEpgDate();

    }

    /**
     * Sets the date info.
     *
     * @param mDateInfo the new date info
     */
    public void setDateInfo(EpgDateInfo mDateInfo) {
        this.mDateInfo = mDateInfo;
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        arg0.reset();
        mAdapter.swapCursor(null);
    }

    /**
     * Cursor to timer.
     *
     * @param c the c
     * @return the timer©
     */
    private Timer cursorToTimer(Cursor c) {
        String epgTitle = !c.isNull(c.getColumnIndex(EpgTbl.TITLE)) ? c.getString(c.getColumnIndex(EpgTbl.TITLE)) : channel;
        long epgStart = c.getLong(c.getColumnIndex(EpgTbl.START));
        long epgEnd = c.getLong(c.getColumnIndex(EpgTbl.END));
        DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
        int epgBefore = prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, 5);
        int epgAfter = prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, 5);
        Date start = epgStart > 0 ? new Date(epgStart) : new Date();
        Date end = epgEnd > 0 ? new Date(epgEnd) : new Date();
        Log.i(ChannelList.class.getSimpleName(), start.toString());
        Log.i(ChannelList.class.getSimpleName(), start.toString());
        Log.i(ChannelList.class.getSimpleName(), start.toString());
        start = DateUtils.addMinutes(start, 0 - epgBefore);
        end = DateUtils.addMinutes(end, epgAfter);
        Timer timer = new Timer();
        timer.setTitle(epgTitle);
        timer.setChannelId(channelId);
        timer.setChannelName(channel);
        timer.setStart(start);
        timer.setEnd(end);
        timer.setTimerAction(prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, 0));
        return timer;
    }

    public static HttpUrl.Builder buildBaseEpgUrl() throws UrlBuilderException {
        HttpUrl.Builder builder = HTTPUtil.getUrlBuilder(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_EPG)
                .addQueryParameter("utf8", "1")
                .addQueryParameter("lvl", "2");
        return builder;
    }

}
