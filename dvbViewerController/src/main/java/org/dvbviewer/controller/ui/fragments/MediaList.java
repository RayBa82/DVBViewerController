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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.media.MediaFile;
import org.dvbviewer.controller.data.media.MediaViewModel;
import org.dvbviewer.controller.data.version.VersionViewModel;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.ui.adapter.MediaAdapter;
import org.dvbviewer.controller.ui.base.RecyclerViewFragment;
import org.dvbviewer.controller.utils.Config;

import java.util.List;

/**
 * Fragment for EPG details or Timer details.
 */
public class MediaList extends RecyclerViewFragment {

	public static final String KEY_PARENT_ID 	= MediaList.class.getSimpleName() + "KEY_PARENT_ID";
	public static final String MINIMUM_VERSION = "2.0.4.16";

	private MediaAdapter mAdapter;
	private long parentId = 0l;
	private MediaAdapter.OnMediaClickListener mediaClickListener;

	VersionViewModel versionViewModel;
	MediaViewModel mediaViewModel;
	private boolean isFeatureSupported;


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setTitle(R.string.details);
		setHasOptionsMenu(true);
		mAdapter = new MediaAdapter(getContext(), mediaClickListener);
		DVBViewerPreferences preferences = new DVBViewerPreferences(getContext());
		if(getArguments() != null) {
			parentId = getArguments().getLong(KEY_PARENT_ID, 1);
		}else {
			parentId = 1;
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		recyclerView.setAdapter(mAdapter);
		setListShown(false);
		versionViewModel = ViewModelProviders.of(this).get(VersionViewModel.class);
		final Observer<List<MediaFile>> mediaObserver = new Observer<List<MediaFile>>() {
			@Override
			public void onChanged(@Nullable final List<MediaFile> mediaFiles) {
				mAdapter.setCursor(mediaFiles);
				mAdapter.notifyDataSetChanged();
				setListShown(true);
			}
		};
		isFeatureSupported = parentId != 1l;
		mediaViewModel = ViewModelProviders.of(this).get(MediaViewModel.class);
		final Observer<String> versionObserver = new Observer<String>() {
			@Override
			public void onChanged(@Nullable final String version) {
				if(Config.isRSVersionSupported(version, MINIMUM_VERSION)) {
					isFeatureSupported = true;
					mediaViewModel.getMedias(parentId).observe(MediaList.this, mediaObserver);
				}else {
					setListShown(true);
				}
			}
		};
		if(isFeatureSupported) {
			setListShown(false);
			mediaViewModel.getMedias(parentId).observe(MediaList.this, mediaObserver);
		} else {
			setListShown(false);
			versionViewModel.getVersion().observe(this, versionObserver);
		}
	}

	@Override
	public void onAttach(Context activity) {
		super.onAttach(activity);
		if(getParentFragment() != null && getParentFragment() instanceof MediaAdapter.OnMediaClickListener) {
			mediaClickListener = (MediaAdapter.OnMediaClickListener) getParentFragment();
		} else if (activity instanceof MediaAdapter.OnMediaClickListener) {
			mediaClickListener = (MediaAdapter.OnMediaClickListener) activity;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.media_fragment, menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		setListShown(false);
		if(isFeatureSupported) {
			mediaViewModel.fetchMedias(parentId);
		} else {
			versionViewModel.getVersion();
		}
        return true;
    }

}
