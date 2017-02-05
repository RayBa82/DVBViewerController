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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.io.DefaultHttpException;
import org.dvbviewer.controller.io.RecordingService;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class Remote.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class Remote extends Fragment implements LoaderCallbacks<List<String>>, RemoteControl.OnRemoteButtonClickListener {

    private Toolbar mToolbar;
    private ArrayAdapter mSpinnerAdapter;
    private Spinner mClientSpinner;
    private int spinnerPosition;
    private static final String KEY_SPINNER_POS = "spinnerPosition";
    private DVBViewerPreferences prefs;
    private final Gson gson = new Gson();
    private final Type type = new TypeToken<List<String>>() {
    }.getType();
    private ViewPager mPager;
    private OnTargetsChangedListener onTargetsChangedListener;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!UIUtils.isTablet(getActivity())) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }
        initActionBar();
        prefs = new DVBViewerPreferences(getActivity());
        if (savedInstanceState != null) {
            spinnerPosition = savedInstanceState.getInt(KEY_SPINNER_POS, 0);
        }
        mPager.setAdapter(new PagerAdapter(getChildFragmentManager()));
    }

    private void initActionBar() {
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setVisibility(onTargetsChangedListener == null ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTargetsChangedListener){
            onTargetsChangedListener = (OnTargetsChangedListener) context;
        }
    }

    /* (non-Javadoc)
         * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
         */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_remote, container, false);
        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);

        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                return true;
            }
        });

        mToolbar.setTitle(R.string.remote);
        mClientSpinner = (Spinner) v.findViewById(R.id.clientSpinner);
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
        return v;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SPINNER_POS, mClientSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#toString()
     */
    @Override
    public String toString() {
        return "Remote";
    }



    @Override
    public Loader<List<String>> onCreateLoader(int id, Bundle args) {
        AsyncLoader<List<String>> loader = new AsyncLoader<List<String>>(getContext()) {

            @Override
            public List<String> loadInBackground() {
                List<String> result = null;
                try {
                    result = getDVBViewerClients();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }
        };
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
        if (onTargetsChangedListener != null) {
            onTargetsChangedListener.targetsChanged(getString(R.string.remote), data);
        } else if (data != null && !data.isEmpty()) {
            String[] arr = new String[data.size()];
            mSpinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, data.toArray(arr));
            mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            mClientSpinner.setAdapter(mSpinnerAdapter);
            String activeClient = prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT);
            int index = data.indexOf(activeClient);
            spinnerPosition = index > Spinner.INVALID_POSITION ? index : Spinner.INVALID_POSITION;
            mClientSpinner.setSelection(spinnerPosition);
            mClientSpinner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {
        loader.reset();
    }

    private List<String> getDVBViewerClients() throws DefaultHttpException {
        List<String> result = new LinkedList<>();
        String jsonClients = RecordingService.getDVBViewerTargets();
        if (!TextUtils.isEmpty(jsonClients)) {
            result = gson.fromJson(jsonClients, type);
            SharedPreferences.Editor prefEditor = prefs.getPrefs().edit();
            prefEditor.putString(DVBViewerPreferences.KEY_RS_CLIENTS, jsonClients);
            prefEditor.commit();
        }
        return result;
    }

    @Override
    public void OnRemoteButtonClick(String action) {
        Object target = onTargetsChangedListener != null ? onTargetsChangedListener.getSelectedTarget() : mClientSpinner.getSelectedItem();
        String request = MessageFormat.format(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_SEND_COMMAND, target, action);
        ServerRequest.DVBViewerCommand httpCommand = new ServerRequest.DVBViewerCommand(request);
        Thread executionThread = new Thread(httpCommand);
        executionThread.start();
    }

    /**
     * The Class PagerAdapter.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    class PagerAdapter extends FragmentPagerAdapter {

        /**
         * Instantiates a new pager adapter.
         *
         * @param fm the fm
         * @author RayBa
         * @date 07.04.2013
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
            switch (position) {
                case 0:
                    RemoteControl ctl = (RemoteControl) Fragment.instantiate(getActivity(), RemoteControl.class.getName());
                    return ctl;
                case 1:
                    RemoteNumbers numbers = (RemoteNumbers) Fragment.instantiate(getActivity(), RemoteNumbers.class.getName());
                    return numbers;
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.remote_control);
                case 1:
                    return getString(R.string.remote_numbers);
                default:
                    return "";
            }
        }

        /*
                 * (non-Javadoc)
                 *
                 * @see android.support.v4.view.PagerAdapter#getCount()
                 */
        @Override
        public int getCount() {
            return 2;
        }

    }

    public interface OnTargetsChangedListener {

        void targetsChanged(String title, List<String> tragets);

        Object getSelectedTarget();

    }


}
