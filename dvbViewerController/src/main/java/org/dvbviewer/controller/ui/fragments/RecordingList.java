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

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.IEPG;
import org.dvbviewer.controller.entities.Recording;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.data.RecordingHandler;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.BaseListFragment;
import org.dvbviewer.controller.ui.phone.IEpgDetailsActivity;
import org.dvbviewer.controller.ui.phone.StreamConfigActivity;
import org.dvbviewer.controller.utils.AnalyticsTracker;
import org.dvbviewer.controller.utils.ArrayListAdapter;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.FileType;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.StreamUtils;
import org.dvbviewer.controller.utils.UIUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * The Class RecordingList.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class RecordingList extends BaseListFragment implements LoaderCallbacks<List<Recording>>, OnClickListener, ActionMode.Callback, OnCheckedChangeListener, View.OnClickListener, AdapterView.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {

	public static final String ACTION_MODE        = "action_mode";
	public static final String CHECKED_ITEM_COUNT = "checked_item_count";

	private RecordingAdapter    mAdapter;
	private ProgressDialog      progressDialog;
	private ActionMode          mode;
	private int                 selectedPosition;
	private boolean             actionMode;
	private IEpgDetailsActivity.OnIEPGClickListener clickListener;
	private List<Recording> recordings;


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new RecordingAdapter(getContext());
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(mAdapter);
		getListView().setItemsCanFocus(false);
		Loader<List<Recording>> loader = getLoaderManager().initLoader(0, savedInstanceState, this);
		setListShown(!(!isResumed() || loader.isStarted()));
		setEmptyText(getResources().getString(R.string.no_recordings));
		getListView().setOnItemLongClickListener(this);
		if (savedInstanceState != null && savedInstanceState.getBoolean(ACTION_MODE, false)) {
			AppCompatActivity activity = (AppCompatActivity) getActivity();
			mode = activity.startSupportActionMode(this);
			updateActionModeTitle(savedInstanceState.getInt(CHECKED_ITEM_COUNT));
		}
		getActivity().setTitle(R.string.recordings);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
	 */
	@Override
	public Loader<List<Recording>> onCreateLoader(int arg0, Bundle arg1) {
		return new AsyncLoader<List<Recording>>(getContext()) {

			@Override
			public List<Recording> loadInBackground() {
				List<Recording> result = null;
				InputStream is = null;
				try {
					is = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_RECORIDNGS);
					RecordingHandler hanler = new RecordingHandler();
					result = hanler.parse(is);
					Collections.sort(result);
				} catch (Exception e) {
					catchException(getClass().getSimpleName(), e);
				}finally {
					IOUtils.closeQuietly(is);
				}
				return result;
			}
		};
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
	 */
	@Override
	public void onLoadFinished(Loader<List<Recording>> arg0, List<Recording> arg1) {
		mAdapter.setItems(arg1);
		setListShown(true);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
	 */
	@Override
	public void onLoaderReset(Loader<List<Recording>> arg0) {
		if (isVisible()) {
			setListShown(true);
		}
	}


	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setItemChecked(position, true);
		int count = getCheckedItemCount();
		if (actionMode == false) {
			actionMode = true;
			AppCompatActivity activty = (AppCompatActivity) getActivity();
			mode = activty.startSupportActionMode(RecordingList.this);
		}
		updateActionModeTitle(count);
		return true;
	}

	private void updateActionModeTitle(int count) {
		mode.setTitle(count + " " + getResources().getString(R.string.selected));
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menuStreamDirect:
				streamDirect(mAdapter.getItem(selectedPosition));
				return true;
			case R.id.menuStreamTranscoded:
				streamTranscoded(mAdapter.getItem(selectedPosition));
				return true;
			case R.id.menuStreamConfig:
				if (UIUtils.isTablet(getContext())) {
					Bundle arguments = getIntentExtras(mAdapter.getItem(selectedPosition));
					StreamConfig cfg = StreamConfig.Companion.newInstance();
					cfg.setArguments(arguments);
					cfg.show(getActivity().getSupportFragmentManager(), StreamConfig.class.getName());
				} else {
					Bundle arguments = getIntentExtras(mAdapter.getItem(selectedPosition));
					Intent streamConfig = new Intent(getContext(), StreamConfigActivity.class);
					streamConfig.putExtras(arguments);
					startActivity(streamConfig);
				}
				return true;
			case R.id.menuDelete:
				recordings = Collections.singletonList(mAdapter.getItem(selectedPosition));
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setMessage(getResources().getString(R.string.confirmDelete)).setPositiveButton(getResources().getString(R.string.yes), this).setNegativeButton(getResources().getString(R.string.no), this).show();
				return true;

			default:
				break;
		}
		return false;
	}

	private void streamDirect(IEPG recording) {
		try {
			final Intent videoIntent = StreamUtils.getDirectUrl(getContext(), recording.getId(), recording.getTitle(), FileType.RECORDING);
			getActivity().startActivity(videoIntent);
			AnalyticsTracker.trackQuickRecordingStream(getActivity().getApplication());
		} catch (ActivityNotFoundException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setMessage(getResources().getString(R.string.noFlashPlayerFound)).setPositiveButton(getResources().getString(R.string.yes), null).setNegativeButton(getResources().getString(R.string.no), null).show();
			e.printStackTrace();
		}
	}

	private void streamTranscoded(IEPG recording) {
		try {
			final Intent videoIntent = StreamUtils.getTranscodedUrl(getContext(), recording.getId(), recording.getTitle(), FileType.RECORDING);
			getActivity().startActivity(videoIntent);
			AnalyticsTracker.trackQuickRecordingStream(getActivity().getApplication());
		} catch (ActivityNotFoundException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setMessage(getResources().getString(R.string.noFlashPlayerFound)).setPositiveButton(getResources().getString(R.string.yes), null).setNegativeButton(getResources().getString(R.string.no), null).show();
			e.printStackTrace();
		}
	}

	@NonNull
	private Bundle getIntentExtras(IEPG recording) {
		Bundle arguments = new Bundle();
		arguments.putLong(StreamConfig.Companion.getEXTRA_FILE_ID(), recording.getId());
		arguments.putParcelable(StreamConfig.Companion.getEXTRA_FILE_TYPE(), FileType.RECORDING);
		arguments.putInt(StreamConfig.Companion.getEXTRA_DIALOG_TITLE_RES(), R.string.streamConfig);
		arguments.putString(StreamConfig.Companion.getEXTRA_TITLE(), recording.getTitle());
		return arguments;
	}

	/**
	 * The Class ViewHolder.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private static class ViewHolder {
		ImageView 				thumbNail;
		View 					thumbNailContainer;
		TextView				title;
		TextView				subTitle;
		TextView				channelName;
		TextView				date;
		ImageView				contextMenu;
	}

	/**
	 * The Class RecordingAdapter.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public class RecordingAdapter extends ArrayListAdapter<Recording> {

		final Drawable placeHolder;

		/**
		 * The Constructor.
		 *
		 * @param context the context
		 * @author RayBa
		 * @date 04.06.2010
		 * @description Instantiates a new recording adapter.
		 */
		public RecordingAdapter(Context context) {
			super();
			placeHolder = AppCompatResources.getDrawable(context, R.drawable.ic_play_white_40dp);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_recording, parent, false);
				holder = new ViewHolder();
				holder.thumbNail = convertView.findViewById(R.id.thumbNail);
				holder.title = convertView.findViewById(R.id.title);
				holder.subTitle = convertView.findViewById(R.id.subTitle);
				holder.channelName = convertView.findViewById(R.id.channelName);
				holder.date = convertView.findViewById(R.id.date);
				holder.contextMenu = convertView.findViewById(R.id.contextMenu);
				holder.contextMenu.setOnClickListener(RecordingList.this);
				holder.thumbNailContainer = convertView.findViewById(R.id.thumbNailContainer);
				holder.thumbNailContainer.setOnClickListener(RecordingList.this);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Recording o = getItem(position);
			if (o != null) {
//				holder.layout.setChecked(getListView().isItemChecked(position));
				holder.title.setText(o.getTitle());
				if (TextUtils.isEmpty(o.getSubTitle())) {
					holder.subTitle.setVisibility(View.GONE);
				} else {
					holder.subTitle.setVisibility(View.VISIBLE);
					holder.subTitle.setText(o.getSubTitle());
				}
				holder.thumbNail.setImageDrawable(null);
				if (TextUtils.isEmpty(o.getThumbNail())){
					holder.thumbNailContainer.setVisibility(View.GONE);
				}else{
					holder.thumbNailContainer.setVisibility(View.VISIBLE);
                    holder.thumbNail.setImageDrawable(null);
                    Picasso.get()
                            .load(ServerConsts.REC_SERVICE_URL + ServerConsts.THUMBNAILS_VIDEO_URL + o.getThumbNail())
                            .placeholder(placeHolder)
                            .fit()
                            .centerInside()
                            .into(holder.thumbNail);
				}
				holder.thumbNailContainer.setTag(position);
				holder.date.setText(DateUtils.formatDateTime(getContext(), o.getStart().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
				holder.channelName.setText(o.getChannel());
				holder.contextMenu.setTag(position);
			}
			return convertView;
		}
	}


	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("onListItemClick", "onListItemClick ");
		if (actionMode) {
			v.setSelected(!v.isSelected());
			int count = getCheckedItemCount();
			updateActionModeTitle(count);
			if (getCheckedItemCount() == 0) {
				mode.finish();
			}
		} else {
			IEPG entry = mAdapter.getItem(position);
			if(clickListener != null) {
				clickListener.onIEPGClick(entry);
				return;
			}
			Intent details = new Intent(getContext(), IEpgDetailsActivity.class);
			details.putExtra(IEPG.class.getSimpleName(), entry);
			startActivity(details);
			clearSelection();
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(ACTION_MODE, actionMode);
		outState.putInt(CHECKED_ITEM_COUNT, getCheckedItemCount());
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onCreateActionMode(com.actionbarsherlock.view.ActionMode, com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		actionMode = true;
		getActivity().getMenuInflater().inflate(R.menu.actionmode_recording, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onPrepareActionMode(com.actionbarsherlock.view.ActionMode, com.actionbarsherlock.view.Menu)
	 */
	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onActionItemClicked(com.actionbarsherlock.view.ActionMode, com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menuDelete:
				SparseBooleanArray checkedPositions = getListView().getCheckedItemPositions();
				if (checkedPositions != null && checkedPositions.size() > 0) {

					int size = checkedPositions.size();
					recordings = new ArrayList<>();
					for (int i = 0; i < size; i++) {
						if (checkedPositions.valueAt(i)) {
							recordings.add(mAdapter.getItem(checkedPositions.keyAt(i)));
						}
					}
					/**
					 * Alertdialog to confirm the delete of Recordings
					 */
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setMessage(getResources().getString(R.string.confirmDelete)).setPositiveButton(getResources().getString(R.string.yes), this).setNegativeButton(getResources().getString(R.string.no), this).show();
				}
				break;

			default:
				break;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onDestroyActionMode(com.actionbarsherlock.view.ActionMode)
	 */
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		clearSelection();
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		actionMode = false;
	}

	/**
	 * Clear selection.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private void clearSelection() {
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			getListView().setItemChecked(i, false);
		}
		mAdapter.notifyDataSetChanged();
	}


	/* (non-Javadoc)
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.context_menu_recordinglist, menu);
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.recording_list, menu);
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menuRefresh:
				refresh();
				return true;

			default:
				return false;
		}
	}

	/**
	 * Refresh.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private void refresh() {
		getLoaderManager().restartLoader(0, getArguments(), this);
		setListShown(false);
	}


	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
			    final int size = recordings.size();
                int i = 0;
                for(final Recording recording : recordings) {
                    i++;
                    final boolean last = i == recordings.size();
                    Call <ResponseBody>call = getDmsInterface().deleteRecording(recording.getId(), 1);
                    call.enqueue(new Callback<ResponseBody>() {
						@Override
						public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
							if(size == 1) {
								sendMessage(getString(R.string.recording_deleted, recording.getTitle()));
                                refresh();
                            }else if (last){
                                sendMessage(getString(R.string.recordings_deleted, size));
                                refresh();
                            }
						}

						@Override
						public void onFailure(Call<ResponseBody> call, Throwable t) {
							sendMessage(R.string.error_common);
						}
					});
				}
				if(mode != null) {
					mode.finish();
				}
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				// No button clicked
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseListFragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		if (mode != null) {
			mode.finish();
		}
		super.onDestroyView();
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.contextMenu:
				selectedPosition = (Integer) v.getTag();
				PopupMenu popup = new PopupMenu(getContext(), v);
				popup.inflate(R.menu.context_menu_stream);
				popup.inflate(R.menu.context_menu_recordinglist);
				popup.setOnMenuItemClickListener(this);
				popup.show();
				break;
			case R.id.thumbNailContainer:
				try {
					selectedPosition = (Integer) v.getTag();
					final IEPG recording = mAdapter.getItem(selectedPosition);
					final Intent videoIntent = StreamUtils.buildQuickUrl(getContext(), recording.getId(), recording.getTitle(), FileType.RECORDING);
					getActivity().startActivity(videoIntent);
					AnalyticsTracker.trackQuickRecordingStream(getActivity().getApplication());
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


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof IEpgDetailsActivity.OnIEPGClickListener) {
			clickListener = (IEpgDetailsActivity.OnIEPGClickListener) context;
		}
	}

}
