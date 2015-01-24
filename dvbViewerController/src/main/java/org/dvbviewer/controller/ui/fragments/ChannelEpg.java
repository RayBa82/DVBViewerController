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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.PopupMenu;
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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts.EpgTbl;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.EpgEntry;
import org.dvbviewer.controller.entities.IEPG;
import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.ServerRequest.DVBViewerCommand;
import org.dvbviewer.controller.io.ServerRequest.RecordingServiceGet;
import org.dvbviewer.controller.io.data.EpgEntryHandler;
import org.dvbviewer.controller.ui.base.BaseListFragment;
import org.dvbviewer.controller.ui.base.EpgLoader;
import org.dvbviewer.controller.ui.phone.IEpgDetailsActivity;
import org.dvbviewer.controller.ui.phone.TimerDetailsActivity;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.ParseException;
import ch.boye.httpclientandroidlib.auth.AuthenticationException;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.utils.URLEncodedUtils;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

/**
 * The Class ChannelEpg.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class ChannelEpg extends BaseListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener, OnClickListener, PopupMenu.OnMenuItemClickListener {
    public static final String KEY_EPG_DAY = "EPG_DAY";
    ChannelEPGAdapter mAdapter;
    Channel mCHannel;
    ImageLoader mImageCacher;
    int selectedPosition;
    private ImageView channelLogo;
    private TextView position;
    private TextView channelName;
    private TextView dayIndicator;
    EpgDateInfo mDateInfo;
    Date lastRefresh;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayoutRessource(R.layout.fragment_channel_epg);
        mImageCacher = ImageLoader.getInstance();
        if (savedInstanceState != null && savedInstanceState.containsKey(Channel.class.getName())) {
            mCHannel = savedInstanceState.getParcelable(Channel.class.getName());
        }
    }


    /* (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDateInfo = (EpgDateInfo) activity;
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
        mAdapter = new ChannelEPGAdapter(getActivity());
        setListAdapter(mAdapter);
        setListShown(false);
        getListView().setOnItemClickListener(this);
        registerForContextMenu(getListView());
        channelLogo.setImageBitmap(null);
        if (mCHannel != null) {
            mImageCacher.cancelDisplayTask(channelLogo);
            setChannel(mCHannel);
            String url = ServerConsts.REC_SERVICE_URL + "/" + mCHannel.getLogoUrl();
            mImageCacher.displayImage(url, channelLogo);
            position.setText(mCHannel.getPosition().toString());
            channelName.setText(mCHannel.getName());
        }
        if (DateUtils.isToday(mDateInfo.getEpgDate().getTime())) {
            dayIndicator.setText(R.string.today);
        } else if (DateUtils.isTomorrow(mDateInfo.getEpgDate().getTime())) {
            dayIndicator.setText(R.string.tomorrow);
        } else {
            dayIndicator.setText(DateUtils.formatDateTime(getActivity(), mDateInfo.getEpgDate().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY));
        }

        setEmptyText(getResources().getString(R.string.no_epg));
        getLoaderManager().initLoader(mCHannel.getPosition(), savedInstanceState, this);
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
        Loader<Cursor> loader = null;
        loader = new EpgLoader<Cursor>(getActivity().getApplicationContext(), mDateInfo) {

            @Override
            protected void onForceLoad() {
                super.onForceLoad();
                setListShown(false);
            }

            @Override
            public Cursor loadInBackground() {
                MatrixCursor cursor = null;
                Date now = mDateInfo.getEpgDate();
                String nowFloat = DateUtils.getFloatDate(now);
                Date tommorrow = DateUtils.addDay(now);
                String tommorrowFloat = DateUtils.getFloatDate(tommorrow);
                String url = ServerConsts.URL_CHANNEL_EPG + mCHannel.getEpgID() + "&start=" + nowFloat + "&end=" + tommorrowFloat;
                try {
                    List<EpgEntry> result = null;
                    EpgEntryHandler handler = new EpgEntryHandler();
                    String xml = ServerRequest.getRSString(url);
                    result = handler.parse(xml);
                    if (result != null && !result.isEmpty()) {
                        String[] columnNames = new String[]{EpgTbl._ID, EpgTbl.EPG_ID, EpgTbl.TITLE, EpgTbl.SUBTITLE, EpgTbl.DESC, EpgTbl.START, EpgTbl.END};
                        cursor = new MatrixCursor(columnNames);
                        for (EpgEntry entry : result) {
                            cursor.addRow(new Object[]{entry.getId(), entry.getEpgID(), entry.getTitle(), entry.getSubTitle(), entry.getDescription(), entry.getStart().getTime(), entry.getEnd().getTime()});
                        }
                    }

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
        position = (TextView) v.findViewById(R.id.position);
        channelLogo = (ImageView) v.findViewById(R.id.icon);
        channelName = (TextView) v.findViewById(R.id.title);
        dayIndicator = (TextView) v.findViewById(R.id.dayIndicator);
        return v;
    }

    /**
     * The Class ViewHolder.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private class ViewHolder {
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
     * @author RayBa
     * @date 13.05.2012
     */
    private IEPG cursorToEpgEntry(Cursor c) {
        IEPG entry = new EpgEntry();
        entry.setChannel(mCHannel.getName());
        entry.setDescription(c.getString(c.getColumnIndex(EpgTbl.DESC)));
        entry.setEnd(new Date(c.getLong(c.getColumnIndex(EpgTbl.END))));
        entry.setEpgID(mCHannel.getEpgID());
        entry.setStart(new Date(c.getLong(c.getColumnIndex(EpgTbl.START))));
        entry.setSubTitle(c.getString(c.getColumnIndex(EpgTbl.SUBTITLE)));
        entry.setTitle(c.getString(c.getColumnIndex(EpgTbl.TITLE)));
        return entry;
    }

    /**
     * Sets the channel.
     *
     * @param channel the new channel
     * @author RayBa
     * @date 07.04.2013
     */
    public void setChannel(Channel channel) {
        this.mCHannel = channel;
    }

    /**
     * The Class ChannelEPGAdapter.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    public class ChannelEPGAdapter extends CursorAdapter {

        Context mContext;

        /**
         * Instantiates a new channel epg adapter.
         *
         * @param context the context
         * @author RayBa
         * @date 07.04.2013
         */
        public ChannelEPGAdapter(Context context) {
            super(context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            mContext = context;
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
            LayoutInflater vi = getActivity().getLayoutInflater();
            View view = vi.inflate(R.layout.list_row_epg, null);
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

    /**
     * Refresh.
     *
     * @param force the force
     * @author RayBa
     * @date 07.04.2013
     */
    public void refresh(boolean force) {
        setListShown(false);
        getLoaderManager().restartLoader(mCHannel.getPosition(), getArguments(), this);
    }

    /**
     * Refresh date.
     *
     * @author RayBa
     * @date 07.04.2013
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
        outState.putParcelable(Channel.class.getName(), mCHannel);
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
     * @see android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getMenuInfo() != null) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            selectedPosition = info.position;
        }
        Cursor c;
        c = mAdapter.getCursor();
        c.moveToPosition(selectedPosition);
        Timer timer;
        if (getUserVisibleHint()) {
            switch (item.getItemId()) {
                case R.id.menuRecord:
                    timer = cursorToTimer(c);
                    StringBuffer url = new StringBuffer();
                    url.append(timer.getId() < 0l ? ServerConsts.URL_TIMER_CREATE : ServerConsts.URL_TIMER_EDIT);
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

                    return true;
                case R.id.menuTimer:
                    timer = cursorToTimer(c);
                    Intent timerIntent = new Intent(getActivity(), TimerDetailsActivity.class);
                    timerIntent.putExtra(TimerDetails.EXTRA_TITLE, timer.getTitle());
                    timerIntent.putExtra(TimerDetails.EXTRA_CHANNEL_NAME, timer.getChannelName());
                    timerIntent.putExtra(TimerDetails.EXTRA_CHANNEL_ID, timer.getChannelId());
                    timerIntent.putExtra(TimerDetails.EXTRA_START, timer.getStart().getTime());
                    timerIntent.putExtra(TimerDetails.EXTRA_END, timer.getEnd().getTime());
                    timerIntent.putExtra(TimerDetails.EXTRA_ACTIVE, true);
                    startActivity(timerIntent);
                    return true;
                case R.id.menuDetails:
                    Intent details = new Intent(getActivity(), IEpgDetailsActivity.class);
                    c = mAdapter.getCursor();
                    c.moveToPosition(selectedPosition);
                    IEPG entry = cursorToEpgEntry(c);
                    details.putExtra(IEPG.class.getSimpleName(), entry);
                    startActivity(details);
                    return true;
                case R.id.menuSwitch:
                    DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
                    String switchRequest = MessageFormat.format(ServerConsts.URL_SWITCH_COMMAND, prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT), mCHannel.getPosition());
                    DVBViewerCommand command = new DVBViewerCommand(switchRequest);
                    Thread exexuterTHread = new Thread(command);
                    exexuterTHread.start();
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (getUserVisibleHint()) {
            super.onCreateContextMenu(menu, v, menuInfo);
            getActivity().getMenuInflater().inflate(R.menu.context_menu_epg, menu);
        }
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contextMenu:
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
        Cursor c;
        c = mAdapter.getCursor();
        c.moveToPosition(selectedPosition);
        Timer timer;
        if (getUserVisibleHint()) {
            switch (item.getItemId()) {
                case R.id.menuRecord:
                    timer = cursorToTimer(c);
                    String url = timer.getId() <= 0l ? ServerConsts.URL_TIMER_CREATE : ServerConsts.URL_TIMER_EDIT;
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
                    if (timer.getId() > 0) {
                        params.add(new BasicNameValuePair("id", String.valueOf(timer.getId())));
                    }

                    String query = URLEncodedUtils.format(params, "utf-8");
                    String request = url + query;
                    RecordingServiceGet rsGet = new RecordingServiceGet(request);
                    Thread executionThread = new Thread(rsGet);
                    executionThread.start();
                    return true;
                case R.id.menuTimer:
                    timer = cursorToTimer(c);
                    if (!UIUtils.isTablet(getActivity())) {
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
                    c = mAdapter.getCursor();
                    c.moveToPosition(selectedPosition);
                    IEPG entry = cursorToEpgEntry(c);
                    details.putExtra(IEPG.class.getSimpleName(), entry);
                    startActivity(details);
                    return true;
                case R.id.menuSwitch:
                    DVBViewerPreferences prefs = new DVBViewerPreferences(getActivity());
                    String switchRequest = MessageFormat.format(ServerConsts.URL_SWITCH_COMMAND, prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT), mCHannel.getPosition());
                    DVBViewerCommand command = new DVBViewerCommand(switchRequest);
                    Thread exexuterTHread = new Thread(command);
                    exexuterTHread.start();
                    return true;
                default:
                    break;
            }
        }
        return false;
    }


    /**
     * The Interface EpgDateInfo.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    public static interface EpgDateInfo {

        /**
         * Sets the epg date.
         *
         * @param date the new epg date
         * @author RayBa
         * @date 07.04.2013
         */
        public void setEpgDate(Date date);

        /**
         * Gets the epg date.
         *
         * @return the epg date
         * @author RayBa
         * @date 07.04.2013
         */
        public Date getEpgDate();

    }

    /**
     * Sets the date info.
     *
     * @param mDateInfo the new date info
     * @author RayBa
     * @date 07.04.2013
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
     * @author RayBa
     * @date 07.04.2013
     */
    private Timer cursorToTimer(Cursor c) {
        String name = mCHannel.getName();
        long channelID = mCHannel.getChannelID();
        String epgTitle = !c.isNull(c.getColumnIndex(EpgTbl.TITLE)) ? c.getString(c.getColumnIndex(EpgTbl.TITLE)) : name;
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
        timer.setChannelId(channelID);
        timer.setChannelName(name);
        timer.setStart(start);
        timer.setEnd(end);
        timer.setTimerAction(prefs.getPrefs().getInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, 0));
        return timer;
    }

}
