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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.utils.IoUtils;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.io.AuthenticationException;
import org.dvbviewer.controller.io.DefaultHttpException;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.data.TimerHandler;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.BaseActivity.AsyncCallback;
import org.dvbviewer.controller.ui.base.BaseListFragment;
import org.dvbviewer.controller.ui.phone.TimerDetailsActivity;
import org.dvbviewer.controller.ui.widget.ClickableRelativeLayout;
import org.dvbviewer.controller.utils.ArrayListAdapter;
import org.dvbviewer.controller.utils.DateUtils;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * The Class TimerList.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class TimerList extends BaseListFragment implements AsyncCallback, LoaderCallbacks<List<Timer>>, Callback, OnClickListener, TimerDetails.OnTimerEditedListener, DialogInterface.OnDismissListener, AdapterView.OnItemLongClickListener {

	public static final String ACTION_MODE        = "action_mode";
	public static final String CHECKED_ITEM_COUNT = "checked_item_count";
	TimerAdapter	mAdapter;
	ActionMode		mode;
	ProgressDialog	progressDialog;
	private boolean actionMode;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new TimerAdapter(getActivity());
		setHasOptionsMenu(true);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(mAdapter);
		Loader<List<Timer>> loader = getLoaderManager().initLoader(0, savedInstanceState, this);
		setListShown(!(!isResumed() || loader.isStarted()));
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getListView().setOnItemLongClickListener(this);
		setEmptyText(getResources().getString(R.string.no_timer));
		if (savedInstanceState != null && savedInstanceState.getBoolean(ACTION_MODE, false)) {
			AppCompatActivity activity = (AppCompatActivity) getActivity();
			mode = activity.startSupportActionMode(this);
			updateActionModeTitle(savedInstanceState.getInt(CHECKED_ITEM_COUNT));
		}else{
			getActivity().setTitle(R.string.timer);
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
	 */
	@Override
	public Loader<List<Timer>> onCreateLoader(int arg0, Bundle arg1) {
		AsyncLoader<List<Timer>> loader = new AsyncLoader<List<Timer>>(getContext()) {

			@Override
			public List<Timer> loadInBackground() {
				List<Timer> result = null;
				InputStream xml = null;
				try {
					xml = ServerRequest.getInputStream(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_TIMER_LIST);
					TimerHandler hanler = new TimerHandler();
					result = hanler.parse(xml);
					Collections.sort(result);
				} catch (Exception e) {
					catchException(getClass().getSimpleName(), e);
				}finally {
					IoUtils.closeSilently(xml);
				}
				return result;
			}
		};
		return loader;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
	 */
	@Override
	public void onLoadFinished(Loader<List<Timer>> arg0, List<Timer> arg1) {
		mAdapter.setItems(arg1);
		setListShown(true);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
	 */
	@Override
	public void onLoaderReset(Loader<List<Timer>> arg0) {
        arg0.reset();
		if (isVisible()) {
			setListShown(true);
		}
	}

    @Override
    public void timerEdited(boolean edited) {
        if(edited){
            timerSavedAction();
        }
    }

	@Override
	public void onDismiss(DialogInterface dialog) {
		getActivity().setTitle(R.string.timer);
	}

	/**
	 * The Class ViewHolder.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private static class ViewHolder {
		ClickableRelativeLayout	layout;
		ImageView				recIndicator;
		TextView				title;
		TextView				channelName;
		TextView				date;
	}

	/**
	 * The Class TimerAdapter.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public class TimerAdapter extends ArrayListAdapter<Timer> {


		/**
		 * The Constructor.
		 *
		 * @param context the context
		 * @author RayBa
		 * @date 04.06.2010
		 * @description Instantiates a new recording adapter.
		 */
		public TimerAdapter(Context context) {
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
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_timer, parent, false);
				holder = new ViewHolder();
				holder.layout = (ClickableRelativeLayout) convertView;
				holder.recIndicator = (ImageView) convertView.findViewById(R.id.recIndicator);
				holder.recIndicator.setImageResource(R.drawable.ic_record);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.channelName = (TextView) convertView.findViewById(R.id.channelName);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Timer o = getItem(position);
			if (o != null) {
				holder.title.setText(o.getTitle());
				holder.channelName.setText(o.getChannelName());
				String date = DateUtils.getDateInLocalFormat(o.getStart());
				if (DateUtils.isToday(o.getStart().getTime())) {
					date = getResources().getString(R.string.today);
				} else if (DateUtils.isTomorrow(o.getStart().getTime())) {
					date = getResources().getString(R.string.tomorrow);
				}
				holder.layout.setError(o.isFlagSet(Timer.FLAG_EXECUTABLE));
				holder.layout.setDisabled(o.isFlagSet(Timer.FLAG_DISABLED));
				String start = DateUtils.getTimeInLocalFormat(getActivity(), o.getStart());
				String end = DateUtils.getTimeInLocalFormat(getActivity(), o.getEnd());
				holder.date.setText(date + "  " + start + " - " + end);
				holder.recIndicator.setVisibility(o.isFlagSet(Timer.FLAG_RECORDING) ? View.VISIBLE : View.GONE);
			}

			return convertView;
		}
	}

	/* (non-Javadoc)
     * @see org.dvbviewer.controller.ui.base.BaseListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
		if (actionMode) {
			view.setSelected(!view.isSelected());
			int count = getCheckedItemCount();
			updateActionModeTitle(count);
			if (getCheckedItemCount() == 0) {
				mode.finish();
			}
		} else {
			if (UIUtils.isTablet(getActivity())) {
				onDestroyActionMode(mode);
				Timer timer = mAdapter.getItem(position);
				TimerDetails timerdetails = TimerDetails.newInstance();
				Bundle args = TimerDetails.buildBundle(timer);
				timerdetails.setArguments(args);
				timerdetails.setTargetFragment(this, 0);
				timerdetails.setOnTimerEditedListener(this);
				timerdetails.show(getActivity().getSupportFragmentManager(), TimerDetails.class.getName());
			} else {
				getListView().setItemChecked(position, !getListView().isItemChecked(position));
				Timer timer = mAdapter.getItem(position);
				Intent timerIntent = new Intent(getActivity(), TimerDetailsActivity.class);
				Bundle extras = TimerDetails.buildBundle(timer);
				timerIntent.putExtras(extras);
				startActivityForResult(timerIntent, TimerDetails.TIMER_RESULT);
			}
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
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TimerDetails.TIMER_RESULT && resultCode == TimerDetails.RESULT_CHANGED) {
            timerSavedAction();
		}
	}

    private void timerSavedAction() {
        getView().post(new Runnable() {
            @Override
            public void run() {
                Snackbar snackbar = Snackbar
                        .make(getListView(), R.string.timer_saved, Snackbar.LENGTH_LONG);
                snackbar.show();
                refresh();
            }
        });
    }

    /* (non-Javadoc)
     * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.timer_list, menu);
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
			/**
			 * Alertdialog to confirm the delete of Recordings
			 */
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(getResources().getString(R.string.confirmDelete)).setPositiveButton(getResources().getString(R.string.yes), this).setNegativeButton(getResources().getString(R.string.no), this).show();
			break;

		default:
			break;
		}
		return true;
	}

	/**
	 * The Class TimerDeleter.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class TimerDeleter extends AsyncTask<Timer, Void, Boolean> {

		AsyncCallback	callback;

		/**
		 * Instantiates a new timer deleter.
		 *
		 * @param callback the callback
		 * @author RayBa
		 * @date 07.04.2013
		 */
		public TimerDeleter(AsyncCallback callback) {
			this.callback = callback;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			callback.onAsyncActionStart();
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				callback.onAsyncActionStop();
			}

		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Boolean doInBackground(Timer... params) {
			int count = params.length;
			if (count <= 0) {
				return false;
			}
			for (int i = 0; i < count; i++) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_TIMER_DELETE + params[i].getId());
				} catch (AuthenticationException e) {
					e.printStackTrace();
				} catch (DefaultHttpException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		}

	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.view.ActionMode.Callback#onDestroyActionMode(com.actionbarsherlock.view.ActionMode)
	 */
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		actionMode = false;
		clearSelection();
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
	}

	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			SparseBooleanArray checkedPositions = getListView().getCheckedItemPositions();
			if (checkedPositions != null && checkedPositions.size() > 0) {
				int size = checkedPositions.size();
				TimerDeleter deleter = new TimerDeleter(TimerList.this);
				List<Timer> timers = new ArrayList<Timer>();
				for (int i = 0; i < size; i++) {
					if (checkedPositions.valueAt(i)) {
						timers.add(mAdapter.getItem(checkedPositions.keyAt(i)));
					}
				}
				Timer[] array = new Timer[timers.size()];
				deleter.execute(timers.toArray(array));
			}
			mode.finish();
			break;

		case DialogInterface.BUTTON_NEGATIVE:
			// No button clicked
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		getListView().setItemChecked(position, true);
		int count = getCheckedItemCount();
		if (actionMode == false) {
			actionMode = true;
			AppCompatActivity activty = (AppCompatActivity) getActivity();
			mode = activty.startSupportActionMode(TimerList.this);
		}
		updateActionModeTitle(count);
		return true;
	}

	private void updateActionModeTitle(int count) {
		mode.setTitle(count + " " + getResources().getString(R.string.selected));
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
	 * @see org.dvbviewer.controller.ui.base.BaseActivity.AsyncCallback#onAsyncActionStart()
	 */
	@Override
	public void onAsyncActionStart() {
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(R.string.busyDeleteTimer));
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseActivity.AsyncCallback#onAsyncActionStop()
	 */
	@Override
	public void onAsyncActionStop() {
		progressDialog.dismiss();
		refresh();
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
}
