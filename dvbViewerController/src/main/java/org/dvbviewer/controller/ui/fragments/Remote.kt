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
package org.dvbviewer.controller.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import okhttp3.ResponseBody
import org.apache.commons.collections4.CollectionUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.api.ApiResponse
import org.dvbviewer.controller.data.api.ApiStatus
import org.dvbviewer.controller.data.entities.DVBTarget
import org.dvbviewer.controller.data.entities.DVBViewerPreferences
import org.dvbviewer.controller.data.remote.RemoteRepository
import org.dvbviewer.controller.data.remote.RemoteViewModel
import org.dvbviewer.controller.data.remote.RemoteViewModelFactory
import org.dvbviewer.controller.ui.base.AbstractRemote
import org.dvbviewer.controller.ui.base.BaseFragment
import org.dvbviewer.controller.utils.UIUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * The Class Remote.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class Remote : BaseFragment(), AbstractRemote.OnRemoteButtonClickListener {

    private var mToolbar: Toolbar? = null
    private var mSpinnerAdapter: ArrayAdapter<*>? = null
    private var mClientSpinner: AppCompatSpinner? = null
    private var spinnerPosition: Int = 0
    private var onTargetsChangedListener: OnTargetsChangedListener? = null
    private lateinit var prefs: DVBViewerPreferences
    private lateinit var mPager: ViewPager
    private lateinit var repository: RemoteRepository
    private lateinit var viewModel: RemoteViewModel

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = RemoteRepository(getDmsInterface())
        prefs = DVBViewerPreferences(activity?.applicationContext)
        val vFac = RemoteViewModelFactory(repository, prefs)
        viewModel = ViewModelProvider(this, vFac)
                .get(RemoteViewModel::class.java)
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!UIUtils.isTablet(context!!)) {
            (activity as AppCompatActivity).setSupportActionBar(mToolbar)
        }
        initActionBar()
        if (savedInstanceState != null) {
            spinnerPosition = savedInstanceState.getInt(KEY_SPINNER_POS, 0)
        }
        mPager.adapter = PagerAdapter(childFragmentManager)
        val targetObserver = Observer<ApiResponse<List<DVBTarget>>> { response -> onTargetsLoaded(response) }
        viewModel.getTargets().observe(this, targetObserver)
    }

    private fun onTargetsLoaded(observable: ApiResponse<List<DVBTarget>>?) {
        if (observable?.status == ApiStatus.SUCCESS) {
            if (onTargetsChangedListener != null) {
                onTargetsChangedListener?.targetsChanged(getString(R.string.remote), observable.data)
            } else if (CollectionUtils.isNotEmpty(observable.data)) {
                val clients = LinkedList<String>()
                observable.data?.map { it.name }?.let { clients.addAll(it) }
                mSpinnerAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, clients.toTypedArray())
                mSpinnerAdapter!!.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                mClientSpinner?.adapter = mSpinnerAdapter
                val activeClient = prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT)
                val index = clients.indexOf(activeClient)
                spinnerPosition = if (index > Spinner.INVALID_POSITION) index else Spinner.INVALID_POSITION
                mClientSpinner?.setSelection(spinnerPosition)
                mClientSpinner?.visibility = View.VISIBLE
            }
        } else if (observable?.status == ApiStatus.ERROR) {
            catchException(TimerList.TAG, observable.e)
        }
    }


    private fun initActionBar() {
        val ab = (activity as AppCompatActivity).supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
        mToolbar?.visibility = if (onTargetsChangedListener == null) View.VISIBLE else View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTargetsChangedListener) {
            onTargetsChangedListener = context
        }
    }

    /* (non-Javadoc)
         * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
         */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_remote, container, false)
        mToolbar = v.findViewById(R.id.toolbar)

        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar?.setOnMenuItemClickListener {
            // Handle the menu item
            true
        }

        mToolbar?.setTitle(R.string.remote)
        mClientSpinner = v.findViewById(R.id.clientSpinner)
        mClientSpinner?.visibility = View.GONE
        mClientSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedClient = mSpinnerAdapter!!.getItem(position) as String?
                prefs.prefs.edit()
                        .putString(DVBViewerPreferences.KEY_SELECTED_CLIENT, selectedClient)
                        .apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        return v
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see android.support.v4.app.Fragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPager = view.findViewById<View>(R.id.pager) as ViewPager
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mClientSpinner?.selectedItemPosition?.let { outState.putInt(KEY_SPINNER_POS, it) }
        super.onSaveInstanceState(outState)
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#toString()
     */
    override fun toString(): String {
        return "Remote"
    }

    override fun OnRemoteButtonClick(action: String) {
        val target = if (onTargetsChangedListener != null) onTargetsChangedListener!!.selectedTarget else mClientSpinner!!.selectedItem
        if (target == null) {
            sendMessage(R.string.no_remote_target)
            return
        }
        repository.sendCommand(target.toString(), action).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.i(Remote::class.java.simpleName, "Send command to target")
            }

            override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                catchException(Remote::class.java.simpleName, throwable)
            }
        })
    }

    internal inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        /*
         * (non-Javadoc)
         *
         * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
         */
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    fragmentManager!!.fragmentFactory.instantiate(javaClass.classLoader!!, RemoteControl::class.java.name)
                }
                else -> {
                    fragmentManager!!.fragmentFactory.instantiate(javaClass.classLoader!!, RemoteNumbers::class.java.name)
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> getString(R.string.remote_control)
                1 -> getString(R.string.remote_numbers)
                else -> ""
            }
        }

        /*
                 * (non-Javadoc)
                 *
                 * @see android.support.v4.view.PagerAdapter#getCount()
                 */
        override fun getCount(): Int {
            return 2
        }

    }

    interface OnTargetsChangedListener {

        var selectedTarget: DVBTarget?

        fun targetsChanged(title: String, tragets: List<DVBTarget>?)

    }

    companion object {
        private const val KEY_SPINNER_POS = "spinnerPosition"
    }


}
