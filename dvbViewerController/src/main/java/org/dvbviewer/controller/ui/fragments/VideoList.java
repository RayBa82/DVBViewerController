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
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.nostra13.universalimageloader.utils.IoUtils;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.VideoFile;
import org.dvbviewer.controller.io.HTTPUtil;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.data.VideoHandler;
import org.dvbviewer.controller.ui.adapter.VideoAdapter;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.RecyclerViewFragment;
import org.dvbviewer.controller.utils.ServerConsts;

import java.io.InputStream;
import java.util.List;

/**
 * Fragment for EPG details or Timer details.
 */
public class VideoList extends RecyclerViewFragment implements LoaderManager.LoaderCallbacks<List<VideoFile>>, VideoAdapter.OnVideoClickListener {

	public static final String KEY_DIR_ID 	= VideoList.class.getSimpleName() + "KEY_DIR_ID";

	private VideoAdapter mAdapter;
	private long dirId = 0l;
	private VideoAdapter.OnVideoClickListener videoClickListener;


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setTitle(R.string.details);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		mAdapter = new VideoAdapter(this);
		dirId = getArguments().getLong(KEY_DIR_ID, 0);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		recList.setAdapter(mAdapter);
			getLoaderManager().initLoader(0, savedInstanceState, this);
		setListShown(false);
	}

	@Override
	public void onAttach(Context activity) {
		super.onAttach(activity);
		if(getParentFragment() != null && getParentFragment() instanceof VideoAdapter.OnVideoClickListener) {
			videoClickListener = (VideoAdapter.OnVideoClickListener) getParentFragment();
		}
		if (activity instanceof VideoAdapter.OnVideoClickListener) {
			videoClickListener = (VideoAdapter.OnVideoClickListener) activity;
		}
	}

	@Override
	public Loader<List<VideoFile>> onCreateLoader(int id, Bundle args) {
		AsyncLoader<List<VideoFile>> loader = new AsyncLoader<List<VideoFile>>(getContext()) {

			@Override
			public List<VideoFile> loadInBackground() {
				List<VideoFile> result = null;
				InputStream xml = null;
				try {
					HTTPUtil.UrlBuilder builder = new HTTPUtil.UrlBuilder(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_MEDIA_LIST);
					builder.addQueryParameter("dirid", String.valueOf(dirId));
					xml = ServerRequest.getInputStream(builder.build().toString());
					final VideoHandler handler = new VideoHandler();
					result = handler.parse(xml);
				} catch (Exception e) {
					catchException(getClass().getSimpleName(), e);
				} finally {
					IoUtils.closeSilently(xml);
				}
				return result;
			}
		};

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<VideoFile>> loader, List<VideoFile> data) {
		mAdapter.setFiles(data);
		mAdapter.notifyDataSetChanged();
		setListShown(true);

	}

	@Override
	public void onLoaderReset(Loader<List<VideoFile>> loader) {

	}

	@Override
	public void onVideoClick(VideoFile videoFile) {
		if(videoClickListener != null) {
			videoClickListener.onVideoClick(videoFile);
		}
	}
}
