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
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.remote.RemoteRepository
import org.dvbviewer.controller.entities.DVBTarget
import org.dvbviewer.controller.entities.DVBViewerPreferences
import org.dvbviewer.controller.ui.base.AbstractRemote
import org.dvbviewer.controller.ui.base.AsyncLoader
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
class Remote : BaseFragment(), LoaderCallbacks<List<DVBTarget>>, AbstractRemote.OnRemoteButtonClickListener {

    private var mToolbar: Toolbar? = null
    private var mSpinnerAdapter: ArrayAdapter<*>? = null
    private var mClientSpinner: AppCompatSpinner? = null
    private var spinnerPosition: Int = 0
    private var prefs: DVBViewerPreferences? = null
    private val gson = Gson()
    private val type = object : TypeToken<List<DVBTarget>>() {

    }.type
    private var mPager: ViewPager? = null
    private var onTargetsChangedListener: OnTargetsChangedListener? = null
    private var repository: RemoteRepository? = null

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        repository = RemoteRepository(getDmsInterface())
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
        prefs = DVBViewerPreferences(context!!)
        if (savedInstanceState != null) {
            spinnerPosition = savedInstanceState.getInt(KEY_SPINNER_POS, 0)
        }
        mPager!!.adapter = PagerAdapter(childFragmentManager)
    }

    private fun initActionBar() {
        val ab = (activity as AppCompatActivity).supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
        mToolbar!!.visibility = if (onTargetsChangedListener == null) View.VISIBLE else View.GONE
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
        mToolbar!!.setOnMenuItemClickListener {
            // Handle the menu item
            true
        }

        mToolbar!!.setTitle(R.string.remote)
        mClientSpinner = v.findViewById(R.id.clientSpinner)
        mClientSpinner!!.visibility = View.GONE
        mClientSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedClient = mSpinnerAdapter!!.getItem(position) as String?
                prefs!!.prefs.edit().putString(DVBViewerPreferences.KEY_SELECTED_CLIENT, selectedClient).commit()
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
        outState.putInt(KEY_SPINNER_POS, mClientSpinner!!.selectedItemPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        loaderManager.initLoader(0, null, this)
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#toString()
     */
    override fun toString(): String {
        return "Remote"
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<DVBTarget>> {
        return object : AsyncLoader<List<DVBTarget>>(context!!) {

            override fun loadInBackground(): List<DVBTarget>? {
                var result: List<DVBTarget>? = null
                try {
                    result = repository!!.getTargets()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (CollectionUtils.isNotEmpty(result)) {
                    val prefEditor = prefs!!.prefs.edit()
                    prefEditor.putString(DVBViewerPreferences.KEY_RS_CLIENTS, gson.toJson(result))
                    prefEditor.apply()
                } else {
                    val prefValue = prefs!!.prefs.getString(DVBViewerPreferences.KEY_RS_CLIENTS, "")
                    if (StringUtils.isNotBlank(prefValue)) {
                        result = gson.fromJson<List<DVBTarget>>(prefValue, type)
                    }
                }
                return result
            }
        }
    }

    override fun onLoadFinished(loader: Loader<List<DVBTarget>>, data: List<DVBTarget>?) {
        if (onTargetsChangedListener != null) {
            onTargetsChangedListener!!.targetsChanged(getString(R.string.remote), data)
        } else if (data != null && !data.isEmpty()) {
            val clients = LinkedList<String>()
            clients.addAll(data.map { it.name })
            mSpinnerAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, clients.toTypedArray())
            mSpinnerAdapter!!.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            mClientSpinner!!.adapter = mSpinnerAdapter
            val activeClient = prefs!!.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT)
            val index = clients.indexOf(activeClient)
            spinnerPosition = if (index > Spinner.INVALID_POSITION) index else Spinner.INVALID_POSITION
            mClientSpinner!!.setSelection(spinnerPosition)
            mClientSpinner!!.visibility = View.VISIBLE
        }
    }

    override fun onLoaderReset(loader: Loader<List<DVBTarget>>) {
        loader.reset()
    }

    override fun OnRemoteButtonClick(action: String) {
        val target = if (onTargetsChangedListener != null) onTargetsChangedListener!!.selectedTarget else mClientSpinner!!.selectedItem
        if (target == null) {
            sendMessage(R.string.no_remote_target)
            return
        }
        repository!!.sendCommand(target.toString(), action).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.i(Remote::class.java.simpleName, "Send command to target")
            }

            override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                catchException(Remote::class.java.simpleName, throwable)
            }
        })
    }

    /**
     * The Class PagerAdapter.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    internal inner class PagerAdapter
    /**
     * Instantiates a new pager adapter.
     *
     * @param fm the fm
     * @author RayBa
     * @date 07.04.2013
     */
    (fm: FragmentManager) : FragmentPagerAdapter(fm) {

        /*
         * (non-Javadoc)
         *
         * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
         */
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    instantiate(context!!, RemoteControl::class.java.name) as RemoteControl
                }
                else -> {
                    instantiate(context!!, RemoteNumbers::class.java.name) as RemoteNumbers
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.remote_control)
                1 -> return getString(R.string.remote_numbers)
                else -> return ""
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
