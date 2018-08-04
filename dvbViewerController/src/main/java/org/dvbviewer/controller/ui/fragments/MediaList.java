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

import org.apache.commons.lang3.BooleanUtils;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.ApiResponse;
import org.dvbviewer.controller.data.media.MediaFile;
import org.dvbviewer.controller.data.media.MediaRepository;
import org.dvbviewer.controller.data.media.MediaViewModel;
import org.dvbviewer.controller.data.media.MediaViewModelFactory;
import org.dvbviewer.controller.data.version.VersionRepository;
import org.dvbviewer.controller.data.version.VersionViewModel;
import org.dvbviewer.controller.data.version.VersionViewModelFactory;
import org.dvbviewer.controller.io.api.APIClient;
import org.dvbviewer.controller.io.api.DMSInterface;
import org.dvbviewer.controller.ui.adapter.MediaAdapter;
import org.dvbviewer.controller.ui.base.RecyclerViewFragment;

import java.text.MessageFormat;
import java.util.List;

import static org.dvbviewer.controller.data.Status.SUCCESS;

/**
 * Fragment for EPG details or Timer details.
 */
public class MediaList extends RecyclerViewFragment {

	public static final String KEY_PARENT_ID 	= MediaList.class.getSimpleName() + "KEY_PARENT_ID";
	private static final String MINIMUM_VERSION = "2.1.0.0";

	private MediaAdapter mAdapter;
	private long parentId = 0L;
	private MediaAdapter.OnMediaClickListener mediaClickListener;

	private VersionViewModel versionViewModel;
	private MediaViewModel mediaViewModel;
	private boolean isFeatureSupported;
	private DMSInterface dmsInterface;


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mAdapter = new MediaAdapter(getContext(), mediaClickListener);
		if(getArguments() != null) {
			parentId = getArguments().getLong(KEY_PARENT_ID, 1);
		}else {
			parentId = 1;
		}
		dmsInterface = APIClient.getClient().create(DMSInterface.class);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setTitle(R.string.medias);
		recyclerView.setAdapter(mAdapter);
		setListShown(false);
		boolean checkVersion = parentId == 1l;
		initViewModels();
		final Observer<ApiResponse<List<MediaFile>>> mediaObserver = new Observer<ApiResponse<List<MediaFile>>>() {
			@Override
			public void onChanged(@Nullable final ApiResponse<List<MediaFile>> response) {
				onMediaChanged(response);
			}
		};
		final Observer<ApiResponse<Boolean>> versionObserver = new Observer<ApiResponse<Boolean>>() {
			@Override
			public void onChanged(@Nullable final ApiResponse<Boolean> response) {
				onVersionChange(response, mediaObserver);
			}
		};
		if(checkVersion) {
			setListShown(false);
			versionViewModel.isSupported(MINIMUM_VERSION).observe(this, versionObserver);
		} else {
			setListShown(false);
			mediaViewModel.getMedias(parentId).observe(MediaList.this, mediaObserver);
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
			versionViewModel.fetchSupported(MINIMUM_VERSION);
		}
        return true;
    }

	private void initViewModels() {
		final VersionRepository repo = new VersionRepository(getContext(), dmsInterface);
		final VersionViewModelFactory vFac = new VersionViewModelFactory(getActivity().getApplication(), repo);
		versionViewModel = ViewModelProviders.of(this, vFac)
				.get(VersionViewModel.class);
		final MediaRepository mediaRepo = new MediaRepository(dmsInterface);
		final MediaViewModelFactory mediaFac = new MediaViewModelFactory(getActivity().getApplication(), mediaRepo);
		mediaViewModel = ViewModelProviders.of(this, mediaFac)
				.get(MediaViewModel.class);
	}

	private void onMediaChanged(ApiResponse<List<MediaFile>> response) {
		if(response.status == SUCCESS) {
			mAdapter.setCursor(response.data);
			mAdapter.notifyDataSetChanged();
		}else {
			sendMessage(response.message);
		}
		setListShown(true);
	}

	private void onVersionChange(ApiResponse<Boolean> response, Observer<ApiResponse<List<MediaFile>>> mediaObserver) {
		if(response.status == SUCCESS) {
			if(BooleanUtils.isTrue(response.data)) {
				isFeatureSupported = true;
				mediaViewModel.getMedias(parentId).observe(MediaList.this, mediaObserver);
			} else {
				final String text = MessageFormat.format(getString(R.string.version_unsupported_text), MINIMUM_VERSION);
				infoText.setText(text);
				setListShown(true);
			}
		}else {
			sendMessage(response.message);
			setListShown(true);
		}
	}

}
