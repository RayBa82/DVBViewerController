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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.Status;
import org.dvbviewer.controller.entities.Status.Folder;
import org.dvbviewer.controller.entities.Status.StatusItem;
import org.dvbviewer.controller.io.RecordingService;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.data.Status2Handler;
import org.dvbviewer.controller.io.data.StatusHandler;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.BaseListFragment;
import org.dvbviewer.controller.utils.AnalyticsTracker;
import org.dvbviewer.controller.utils.ArrayListAdapter;
import org.dvbviewer.controller.utils.CategoryAdapter;
import org.dvbviewer.controller.utils.Config;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.FileUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.json.JSONObject;

import java.text.MessageFormat;

/**
 * The Class StatusList.
 *
 * @author RayBa
 * @date 05.07.2012
 */
public class StatusList extends BaseListFragment implements LoaderCallbacks<Status> {

    CategoryAdapter mAdapter;
    Resources mRes;
    private StatusAdapter mStatusAdapter;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mStatusAdapter = new StatusAdapter(getActivity());
        mAdapter = new CategoryAdapter(getActivity());
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRes = getResources();
        Loader<Status> loader = getLoaderManager().initLoader(0, savedInstanceState, this);
        setListShown(!(!isResumed() || loader.isStarted()));
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
     */
    @Override
    public Loader<Status> onCreateLoader(int arg0, Bundle arg1) {
        AsyncLoader<Status> loader = new AsyncLoader<Status>(getActivity().getApplicationContext()) {

            @Override
            public Status loadInBackground() {
                Status result = null;
                try {
                    String version = RecordingService.getVersionString();
                    if (!Config.isRSVersionSupported(version)){
                        showToast(getContext(), MessageFormat.format(getStringSafely(R.string.version_unsupported_text), Config.SUPPORTED_RS_VERSION));
                        return result;
                    }
                    result = getStatus(new DVBViewerPreferences(getActivity()), version, null);
                } catch (Exception e) {
                    catchException(getClass().getSimpleName(), e);
                }
                return result;
            }
        };
        return loader;
    }

    @Nullable
    public static Status getStatus(DVBViewerPreferences prefs, String version, @Nullable JSONObject trackingData) throws Exception {
        Status result;
        String status2Xml = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_STATUS2);
        trackingData = AnalyticsTracker.addData(trackingData, "status2", status2Xml);
        Status2Handler status2Handler = new Status2Handler();
        result = status2Handler.parse(status2Xml);
        StatusHandler statusHandler = new StatusHandler();
        String statusXml = ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_STATUS);
        AnalyticsTracker.addData(trackingData, "status", statusXml);
        Status oldStatus = statusHandler.parse(statusXml);
        StatusItem versionItem = new StatusItem();
        versionItem.setNameRessource(R.string.status_rs_version);
        versionItem.setValue(version);
        result.getItems().add(0, versionItem);
        String jsonClients = RecordingService.getDVBViewerTargets();
        AnalyticsTracker.addData(trackingData, "dvbviewerTargets", jsonClients);
        String ffmpegPrefsIni =  ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_FFMPEGPREFS);
        AnalyticsTracker.addData(trackingData, "ffmpegPrefsIni", ffmpegPrefsIni);
        result.getItems().addAll(oldStatus.getItems());
        SharedPreferences.Editor prefEditor = prefs.getPrefs().edit();
        if (jsonClients != null) {
            prefEditor.putString(DVBViewerPreferences.KEY_RS_CLIENTS, jsonClients);
        }
        prefEditor.putString(DVBViewerPreferences.KEY_RS_VERSION, version);
        prefEditor.putInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, oldStatus.getEpgBefore());
        prefEditor.putInt(DVBViewerPreferences.KEY_TIMER_TIME_AFTER, oldStatus.getEpgAfter());
        prefEditor.putInt(DVBViewerPreferences.KEY_TIMER_DEF_AFTER_RECORD, oldStatus.getDefAfterRecord());
        prefEditor.commit();
        return result;
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<Status> loader, Status status) {
        if (status != null) {
            mStatusAdapter.setItems(status.getItems());
            FolderAdapter folderAdapter = new FolderAdapter(getActivity());
            folderAdapter.setItems(status.getFolders());
            mAdapter.addSection(getString(R.string.status), mStatusAdapter);
            mAdapter.addSection(getString(R.string.recording_folder), folderAdapter);
            mAdapter.notifyDataSetChanged();
        }
        setListAdapter(mAdapter);
        setListShown(true);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Status> arg0) {
        if (isVisible()) {
            setListShown(true);
        }
    }

    /**
     * The Class StatusHolder.
     *
     * @author RayBa
     * @date 05.07.2012
     */
    private class StatusHolder {
        TextView title;
        TextView statusText;
        TextView free;
        TextView size;
    }


    /**
     * The Class FolderAdapter.
     *
     * @author RayBa
     * @date 05.07.2012
     */
    public class FolderAdapter extends ArrayListAdapter<Folder> {

        /**
         * Instantiates a new folder adapter.
         *
         * @param context the context
         * @author RayBa
         * @date 05.07.2012
         */
        public FolderAdapter(Context context) {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.ArrayAdapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            StatusHolder holder;
            if (convertView == null) {
                LayoutInflater vi = getActivity().getLayoutInflater();
                convertView = vi.inflate(R.layout.list_item_status, parent, false);
                holder = new StatusHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.statusText = (TextView) convertView.findViewById(R.id.statusText);
                holder.size = (TextView) convertView.findViewById(R.id.size);
                holder.free = (TextView) convertView.findViewById(R.id.free);
                convertView.setTag(holder);
            } else {
                holder = (StatusHolder) convertView.getTag();
            }
            holder.title.setText(mItems.get(position).getPath());
            holder.statusText.setVisibility(View.GONE);
            holder.size.setVisibility(View.VISIBLE);
            holder.free.setVisibility(View.VISIBLE);
            holder.size.setText(mRes.getString(R.string.status_folder_total) + mRes.getString(R.string.common_colon) + FileUtils.byteToHumanString(mItems.get(position).getSize()));
            holder.free.setText(mRes.getString(R.string.status_folder_free) + mRes.getString(R.string.common_colon) + FileUtils.byteToHumanString(mItems.get(position).getFree()));
            super.getViewTypeCount();
            return convertView;
        }


    }

    /**
     * The Class StatusAdapter.
     *
     * @author RayBa
     * @date 05.07.2012
     */
    public class StatusAdapter extends ArrayListAdapter<StatusItem> {

        /**
         * Instantiates a new status adapter.
         *
         * @param context the context
         * @author RayBa
         * @date 05.07.2012
         */
        public StatusAdapter(Context context) {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.ArrayAdapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            StatusHolder holder;
            if (convertView == null || !(convertView.getTag() instanceof StatusHolder)) {
                LayoutInflater vi = getActivity().getLayoutInflater();
                convertView = vi.inflate(R.layout.list_item_status, parent, false);
                holder = new StatusHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.statusText = (TextView) convertView.findViewById(R.id.statusText);
                holder.size = (TextView) convertView.findViewById(R.id.size);
                holder.free = (TextView) convertView.findViewById(R.id.free);
                convertView.setTag(holder);
            } else {
                holder = (StatusHolder) convertView.getTag();
            }
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(getResources().getString(mItems.get(position).getNameRessource()));
            holder.statusText.setVisibility(View.VISIBLE);
            switch (mItems.get(position).getNameRessource()) {
                case R.string.status_epg_update_running:
                case R.string.status_standby_blocked:
                    holder.statusText.setText(Integer.valueOf(mItems.get(position).getValue()) == 0 ? R.string.no : R.string.yes);
                    break;
                case R.string.status_epg_before:
                case R.string.status_epg_after:
                    holder.statusText.setText(mItems.get(position).getValue() + " " + mRes.getString(R.string.minutes));
                    break;
                case R.string.status_timezone:
                    int timezone = Integer.valueOf(mItems.get(position).getValue()) / 60;
                    holder.statusText.setText(mRes.getString(R.string.gmt) + (timezone > 0 ? " +" : "") + timezone);
                    break;
                case R.string.status_def_after_record:
                    holder.statusText.setText(mRes.getStringArray(R.array.postRecoridngActions)[Integer.valueOf(mItems.get(position).getValue())]);
                    break;
                case R.string.status_last_ui_access:
                case R.string.status_next_Rec:
                case R.string.status_next_timer:
                    holder.statusText.setText(DateUtils.secondsToReadableFormat(Long.valueOf(mItems.get(position).getValue())));
                    break;
                default:
                    holder.statusText.setText(mItems.get(position).getValue());
                    break;
            }
            holder.size.setVisibility(View.GONE);
            holder.free.setVisibility(View.GONE);
            return convertView;
        }


    }

    /**
     * Refresh.
     *
     * @author RayBa
     * @date 05.07.2012
     */
    private void refresh() {
        getLoaderManager().restartLoader(0, getArguments(), this);
        setListShown(false);
    }

}
