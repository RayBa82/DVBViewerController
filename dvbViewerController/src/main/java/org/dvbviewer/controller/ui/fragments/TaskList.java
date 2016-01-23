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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.Task;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.ui.base.BaseListFragment;
import org.dvbviewer.controller.utils.ArrayListAdapter;
import org.dvbviewer.controller.utils.CategoryAdapter;
import org.dvbviewer.controller.utils.NetUtils;
import org.dvbviewer.controller.utils.ServerConsts;

import java.text.MessageFormat;

/**
 * The Class TaskList.
 *
 * @author RayBa
 * @date 01.07.2012
 */
public class TaskList extends BaseListFragment implements OnClickListener {

	private static final String WOL_COMMAND = "WOL";
	// TaskAdapter mAdapter;
	CategoryAdapter	sAdapter;
	Task selectedTask;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		Resources r = getResources();
		sAdapter = new CategoryAdapter(getActivity());

		TaskAdapter system = new TaskAdapter(getActivity());
		system.addItem(new Task(r.getString(R.string.WOL), WOL_COMMAND));
		system.addItem(new Task(r.getString(R.string.Standby), "Standby"));
		system.addItem(new Task(r.getString(R.string.Hibernate), "Hibernate"));
		system.addItem(new Task(r.getString(R.string.Shutdown), "Shutdown"));

		TaskAdapter epg = new TaskAdapter(getActivity());
		epg.addItem(new Task(r.getString(R.string.EPGStart), "EPGStart"));
		epg.addItem(new Task(r.getString(R.string.EPGStop), "EPGStop"));
		epg.addItem(new Task(r.getString(R.string.AutoTimer), "AutoTimer"));

		TaskAdapter rec = new TaskAdapter(getActivity());
		rec.addItem(new Task(r.getString(R.string.RefreshDB), "RefreshDB"));
		rec.addItem(new Task(r.getString(R.string.CleanupDB), "CleanupDB"));
		rec.addItem(new Task(r.getString(R.string.RebuildRecordedHistory), "RebuildRecordedHistory"));
		rec.addItem(new Task(r.getString(R.string.ClearRecordingHistory), "ClearRecordingHistory"));
		rec.addItem(new Task(r.getString(R.string.ClearRecordingStats), "ClearRecordingStats"));

		TaskAdapter media = new TaskAdapter(getActivity());
		media.addItem(new Task(r.getString(R.string.UpdateMediaLibrary), "UpdateVideoDB"));
		media.addItem(new Task(r.getString(R.string.CleanupPhotoDB), "CleanupPhotoDB"));
		media.addItem(new Task(r.getString(R.string.CleanupVideoDB), "CleanupVideoDB"));
		media.addItem(new Task(r.getString(R.string.RebuildVideoDB), "RebuildVideoDB"));
		media.addItem(new Task(r.getString(R.string.RebuildAudioDB), "RebuildAudioDB"));
		media.addItem(new Task(r.getString(R.string.RebuildPhotoDB), "RebuildPhotoDB"));
		media.addItem(new Task(r.getString(R.string.ClearAudioStats), "ClearAudioStats"));
		media.addItem(new Task(r.getString(R.string.ClearVideoStats), "ClearVideoStats"));
		media.addItem(new Task(r.getString(R.string.ClearPhotoStats), "ClearPhotoStats"));

		sAdapter.addSection("System", system);
		sAdapter.addSection("EPG", epg);
		sAdapter.addSection("Aufnahmen", rec);
		sAdapter.addSection("Mediadateien", media);
		// mAdapter = new TaskAdapter(getActivity());

	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(sAdapter);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
		selectedTask = (Task) sAdapter.getItem(position);
		if (selectedTask.getCommand().equals(WOL_COMMAND)) {
			/**
			 * Try to wake Recording Service
			 */
			Runnable wakeOnLanRunnabel = new Runnable() {
				
				@Override
				public void run() {
					NetUtils.sendWakeOnLan(ServerConsts.REC_SERVICE_MAC_ADDRESS, ServerConsts.REC_SERVICE_WOL_PORT);
				}
			};
			
				Thread wakeOnLanThread = new Thread(wakeOnLanRunnabel);
				wakeOnLanThread.start();
		}else {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			String question = MessageFormat.format(getResources().getString(R.string.task_execute_security_question), selectedTask.getTitle());
			builder.setMessage(question).setPositiveButton("Yes", this).setTitle(R.string.dialog_confirmation_title).setNegativeButton("No", this).show();
		}
		
	}

	/**
	 * The Class TaskAdapter.
	 *
	 * @author RayBa
	 * @date 01.07.2012
	 */
	public class TaskAdapter extends ArrayListAdapter<Task> {

		/**
		 * The Constructor.
		 *
		 * @param context the context
		 * @author RayBa
		 * @date 04.06.2010
		 * @description Instantiates a new recording adapter.
		 */
		public TaskAdapter(Context context) {
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
			if (convertView == null) {
				LayoutInflater vi = getActivity().getLayoutInflater();
				convertView = vi.inflate(R.layout.list_item_tasks, parent, false);
			}
			TextView title = (TextView) convertView.findViewById(android.R.id.title);
			title.setText(mItems.get(position).getTitle());
			return convertView;
		}
	}

	/**
	 * Runnable to execute the HTTPRequest
	 * 
	 * Expects the taskname in the Constructor.
	 *
	 * @author RayBa
	 * @date 13.04.2012
	 */
	class CommandExecutor implements Runnable {

		private String	task;

		/**
		 * Instantiates a new command executor.
		 *
		 * @param task the task
		 * @author RayBa
		 * @date 01.07.2012
		 */
		public CommandExecutor(String task) {
			this.task = task;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				ServerRequest.getRSString(ServerConsts.REC_SERVICE_URL + ServerConsts.URL_EXECUTE_TASK + task);
			} catch (Exception e) {
				catchException(getClass().getSimpleName(), e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			String command = selectedTask.getCommand();
			CommandExecutor commandExecutor = new CommandExecutor(command);
			Thread executionThread = new Thread(commandExecutor);
			executionThread.start();
			break;

		case DialogInterface.BUTTON_NEGATIVE:
			// No button clicked
			break;
		}
	}

}
