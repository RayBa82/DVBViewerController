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
package org.dvbviewer.controller.ui.fragments

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import okhttp3.ResponseBody
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.Status
import org.dvbviewer.controller.data.task.TaskViewModel
import org.dvbviewer.controller.data.task.xml.Task
import org.dvbviewer.controller.data.task.xml.TaskList
import org.dvbviewer.controller.ui.base.BaseListFragment
import org.dvbviewer.controller.utils.ArrayListAdapter
import org.dvbviewer.controller.utils.CategoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.MessageFormat

/**
 * Used to display the list of DMS tasks
 */
class TaskListFragment : BaseListFragment(), OnClickListener {

    lateinit var sAdapter: CategoryAdapter
    private var selectedTask: Task? = null
    private lateinit var mViewModel: TaskViewModel

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sAdapter = CategoryAdapter(context)
        mViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        val mediaObserver = Observer<ApiResponse<TaskList>> { response ->
            when {
                response?.status == Status.SUCCESS -> {
                    response.data?.groups?.forEach {
                        val adapter = TaskAdapter()
                        it.tasks?.forEach { task ->
                            adapter.addItem(task)
                        }
                        sAdapter.addSection(it.name, adapter)
                    }
                    listAdapter = sAdapter
                }
                response.status == Status.NOT_SUPPORTED -> setEmptyText(response.message)
                response.status == Status.ERROR -> catchException(TaskListFragment::class.java.simpleName, response.e)
            }
            setListShown(true)
        }
        mViewModel.taskList.observe(this, mediaObserver)
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        if (context == null) {
            return
        }
        selectedTask = sAdapter.getItem(position) as Task
        val builder = AlertDialog.Builder(context!!)
        val question = MessageFormat.format(resources.getString(R.string.task_execute_security_question), selectedTask?.name)
        builder.setMessage(question).setPositiveButton("Yes", this).setTitle(R.string.dialog_confirmation_title).setNegativeButton("No", this).show()
    }

    inner class TaskAdapter internal constructor() : ArrayListAdapter<Task>() {

        /*
		 * (non-Javadoc)
		 *
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_tasks, parent, false)
            }
            val title = convertView!!.findViewById<TextView>(android.R.id.title)
            title.text = mItems[position].name
            return convertView
        }
    }

    /* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val action = selectedTask?.action
                val name = selectedTask?.name
                val call = action?.let { getDmsInterface().executeTask(it) }
                call?.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val resMsg = getString(R.string.task_executed)
                        val msg = MessageFormat.format(resMsg, name)
                        sendMessage(msg)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        sendMessage(R.string.error_common)
                    }
                })
            }

            DialogInterface.BUTTON_NEGATIVE -> {
            }
        }// No button clicked

    }

}
