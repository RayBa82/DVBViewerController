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
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nostra13.universalimageloader.utils.IoUtils;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts;
import org.dvbviewer.controller.data.DbHelper;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.entities.MediaFile;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.data.MediaHandler;
import org.dvbviewer.controller.ui.adapter.MediaAdapter;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.RecyclerViewFragment;
import org.dvbviewer.controller.utils.ServerConsts;

import java.io.InputStream;
import java.util.List;

/**
 * Fragment for EPG details or Timer details.
 */
public class MediaList extends RecyclerViewFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String KEY_PARENT_ID 	= MediaList.class.getSimpleName() + "KEY_PARENT_ID";

	private final int SYNC_LOADER_ID 	= 0;
	private final int MEDIA_LOADER_ID 	= 1;

	private MediaAdapter mAdapter;
	private long parentId = 0l;
	private MediaAdapter.OnMediaClickListener mediaClickListener;
	private boolean mediasSynced = false;


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setTitle(R.string.details);
		setHasOptionsMenu(true);
		mAdapter = new MediaAdapter(mediaClickListener);
		DVBViewerPreferences preferences = new DVBViewerPreferences(getContext());
		mediasSynced = preferences.getBoolean(DVBViewerPreferences.KEY_MEDIAS_SYNCED, false);
		if(getArguments() != null) {
			parentId = getArguments().getLong(KEY_PARENT_ID, 0);
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		recyclerView.setAdapter(mAdapter);
		if(!mediasSynced) {
			getLoaderManager().initLoader(SYNC_LOADER_ID, savedInstanceState, this);
		}else {
			getLoaderManager().initLoader(MEDIA_LOADER_ID, savedInstanceState, this);
		}
		setListShown(false);
	}

	@Override
	public void onAttach(Context activity) {
		super.onAttach(activity);
		if(getParentFragment() != null && getParentFragment() instanceof MediaAdapter.OnMediaClickListener) {
			mediaClickListener = (MediaAdapter.OnMediaClickListener) getParentFragment();
		}
		if (activity instanceof MediaAdapter.OnMediaClickListener) {
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
        parentId = 0l;
        getLoaderManager().restartLoader(SYNC_LOADER_ID, null, this);
        return true;
    }

    @Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case SYNC_LOADER_ID:
				AsyncLoader<Cursor> loader = new AsyncLoader<Cursor>(getContext()) {

					@Override
					public Cursor loadInBackground() {
						final List<MediaFile> result;
						InputStream xml = null;
						try {
							xml = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_MEDIA_LIST);
							final MediaHandler handler = new MediaHandler();
							result = handler.parse(xml);
							DbHelper helper = new DbHelper(getContext());
							helper.saveMediaFiles(result);
						} catch (Exception e) {
							catchException(getClass().getSimpleName(), e);
						} finally {
							IoUtils.closeSilently(xml);
						}
						return new MatrixCursor(new String[0]);
					}
				};

				return loader;
			case MEDIA_LOADER_ID:
				StringBuilder selection = new StringBuilder(DbConsts.MediaTbl.PARENT);
				if (parentId > 0) {
					selection.append(" = " + parentId);
				} else {
					selection.append(" is null");
				}
				return new CursorLoader(getContext().getApplicationContext(), DbConsts.MediaTbl.CONTENT_URI, null, selection.toString(), null, null);
			default:
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()){
			case SYNC_LOADER_ID:
				getLoaderManager().restartLoader(MEDIA_LOADER_ID, getArguments(), this);
				break;
			case MEDIA_LOADER_ID:
				mAdapter.setCursor(data);
				mAdapter.notifyDataSetChanged();
				setListShown(true);
				break;
			default:
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

}
