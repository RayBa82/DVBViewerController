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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.ApiResponse;
import org.dvbviewer.controller.data.Status;
import org.dvbviewer.controller.data.task.TaskViewModel;
import org.dvbviewer.controller.data.task.xml.Task;
import org.dvbviewer.controller.data.task.xml.TaskGroup;
import org.dvbviewer.controller.data.task.xml.TaskList;
import org.dvbviewer.controller.ui.base.BaseListFragment;
import org.dvbviewer.controller.utils.ArrayListAdapter;
import org.dvbviewer.controller.utils.CategoryAdapter;

import java.text.MessageFormat;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Used to display the list of DMS tasks
 */
public class TaskListFragment extends BaseListFragment implements OnClickListener {

	CategoryAdapter	sAdapter;
	Task selectedTask;
	TaskViewModel mViewModel;

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		sAdapter = new CategoryAdapter(getContext());
		mViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
		final Observer<ApiResponse<TaskList>> mediaObserver = new Observer<ApiResponse<TaskList>>() {
			@Override
			public void onChanged(@Nullable final ApiResponse<TaskList> response) {
				if(response.status == Status.SUCCESS) {
					for(TaskGroup taskGroup : response.data.getGroups()) {
						final TaskAdapter adapter = new TaskAdapter();
						for(Task task : taskGroup.getTasks()) {
							adapter.addItem(task);
						}
						sAdapter.addSection(taskGroup.getName(), adapter);
					}
					setListAdapter(sAdapter);
				}else if(response.status == Status.NOT_SUPPORTED){
					setEmptyText(response.message);
				}else {
					sendMessage(response.message);
				}
				setListShown(true);
			}
		};
		mViewModel.getTaskList().observe(this, mediaObserver);
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
        if(getContext() == null) {
            return;
        }
        selectedTask = (Task) sAdapter.getItem(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		String question = MessageFormat.format(getResources().getString(R.string.task_execute_security_question), selectedTask.getName());
		builder.setMessage(question).setPositiveButton("Yes", this).setTitle(R.string.dialog_confirmation_title).setNegativeButton("No", this).show();
	}

	public class TaskAdapter extends ArrayListAdapter<Task> {

        TaskAdapter() {
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
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_tasks, parent, false);
			}
			TextView title = convertView.findViewById(android.R.id.title);
			title.setText(mItems.get(position).getName());
			return convertView;
		}
	}

	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			final String action = selectedTask.getAction();
			final String name = selectedTask.getName();
			Call<ResponseBody> call = getDmsInterface().executeTask(action);
			call.enqueue(new Callback<ResponseBody>() {
				@Override
				public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					final String resMsg = getString(R.string.task_executed);
					final String msg = MessageFormat.format(resMsg, name);
					sendMessage(msg);
				}

				@Override
				public void onFailure(Call<ResponseBody> call, Throwable t) {
					sendMessage(R.string.error_common);
				}
			});

			break;

		case DialogInterface.BUTTON_NEGATIVE:
			// No button clicked
			break;
		}

	}

}
