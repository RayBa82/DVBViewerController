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

package org.dvbviewer.controller.ui.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.App
import org.dvbviewer.controller.BuildConfig
import org.dvbviewer.controller.R
import org.dvbviewer.controller.utils.Config

/**
 * A base activity that defers common functionality across app activities to an.
 *
 * This class shouldn't be used directly; instead,
 * activities should inherit from [BaseSinglePaneActivity] or
 * [BaseMultiPaneActivity].
 */
abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            var message = intent.getStringExtra(BaseFragment.MESSAGE_STRING)
            if (StringUtils.isBlank(message)) {
                val messageId = intent.getIntExtra(BaseFragment.MESSAGE_ID, -1)
                if (messageId > -1) {
                    message = getString(messageId)
                }
            }
            Log.d("receiver", "Got message: " + message!!)
            val view = window.decorView.findViewById<View>(android.R.id.content)
            val snackbar = Snackbar
                    .make(view, message, Snackbar.LENGTH_LONG)
            val snackBarView = snackbar.view
            snackBarView.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
            snackbar.show()
        }
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (!BuildConfig.DEBUG) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
            (application as App).getTracker()
        }
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStart()
	 */
    public override fun onStart() {
        super.onStart()
        if (!BuildConfig.DEBUG) {
            GoogleAnalytics.getInstance(this).reportActivityStart(this)
        }
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStop()
	 */
    public override fun onStop() {
        super.onStop()
        if (!BuildConfig.DEBUG) {
            GoogleAnalytics.getInstance(this).reportActivityStop(this)
        }
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                IntentFilter(BaseFragment.MESSAGE_EVENT))
        if (!TextUtils.isEmpty(Config.CURRENT_RS_PROFILE) && supportActionBar != null) {
            supportActionBar!!.title = title
            supportActionBar!!.subtitle = Config.CURRENT_RS_PROFILE
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    /**
     * Takes a given intent and either starts a new activity to handle it (the
     * default behavior), or creates/updates a fragment (in the case of a
     * multi-pane activity) that can handle the intent.
     *
     * Must be called from the main (UI) thread.
     *
     * @param intent the intent
     * @author RayBa
     * @date 07.04.2013
     */
    open fun openActivityOrFragment(intent: Intent) {
        // Default implementation simply calls startActivity
        startActivity(intent)
    }

    /**
     * The Interface AsyncCallback.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    interface AsyncCallback {

        /**
         * On async action start.
         *
         * @author RayBa
         * @date 07.04.2013
         */
        fun onAsyncActionStart()

        /**
         * On async action stop.
         *
         * @author RayBa
         * @date 07.04.2013
         */
        fun onAsyncActionStop()

    }

    /* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {
            }
        }
        return false
    }

    /**
     * The Class ErrorToastRunnable.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    class ErrorToastRunnable
    /**
     * Instantiates a new error toast runnable.
     *
     * @param context the context
     * @param errorString the error string
     * @author RayBa
     * @date 07.04.2013
     */
    (internal var mContext: Context, internal var errorString: String) : Runnable {

        /* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
        override fun run() {
            Toast.makeText(mContext, errorString, Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Change fragment.
     *
     * @param container the container
     * @param f the f
     * @author RayBa
     * @date 07.04.2013
     */
    fun changeFragment(container: Int, f: Fragment) {
        val trans = supportFragmentManager.beginTransaction()
        trans.replace(container, f, f.javaClass.name)
        trans.addToBackStack(f.javaClass.name)
        trans.commit()
    }

    override fun setTitle(title: CharSequence) {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        if (toolbar != null) {
            setSupportActionBarTitle(title)
            toolbar.title = title
        } else {
            setSupportActionBarTitle(title)
        }
    }


    override fun setTitle(titleId: Int) {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        if (toolbar != null) {
            toolbar.setTitle(titleId)
        } else {
            setSupportActionBarTitle(titleId)
        }
    }

    fun setSubTitle(title: CharSequence) {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        if (toolbar != null) {
            setSupportActionBarSubtitleTitle(R.string.app_name)
            toolbar.title = title
        } else {
            setSupportActionBarSubtitleTitle(title)
        }
    }


    fun setSubTitle(titleId: Int) {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        if (toolbar != null) {
            setSupportActionBarSubtitleTitle(R.string.app_name)
            toolbar.setTitle(titleId)
        } else {
            setSupportActionBarSubtitleTitle(titleId)
        }
    }

    protected fun setDisplayHomeAsUpEnabled(showHomeAsUp: Boolean) {
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(showHomeAsUp)
        }
    }

    private fun setSupportActionBarTitle(titleId: Int) {
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(titleId)
        }
    }

    private fun setSupportActionBarTitle(title: CharSequence) {
        if (supportActionBar != null) {
            supportActionBar!!.title = title
        }
    }

    private fun setSupportActionBarSubtitleTitle(titleId: Int) {
        if (supportActionBar != null) {
            supportActionBar!!.setSubtitle(titleId)
        }
    }

    private fun setSupportActionBarSubtitleTitle(title: CharSequence) {
        if (supportActionBar != null) {
            supportActionBar!!.subtitle = title
        }
    }

    companion object {

        val DATA = "_uri"

        /**
         * Converts an intent into a [Bundle] suitable for use as fragment
         * arguments.
         *
         * @param intent the intent
         * @return the bundle©
         * @author RayBa
         * @date 07.04.2013
         */
        fun intentToFragmentArguments(intent: Intent?): Bundle {
            val arguments = Bundle()
            if (intent == null) {
                return arguments
            }

            val data = intent.data
            if (data != null) {
                arguments.putParcelable(DATA, data)
            }

            val extras = intent.extras
            if (extras != null) {
                arguments.putAll(intent.extras)
            }

            return arguments
        }

        /**
         * Converts a fragment arguments bundle into an intent.
         *
         * @param arguments the arguments
         * @return the intent©
         * @author RayBa
         * @date 07.04.2013
         */
        fun fragmentArgumentsToIntent(arguments: Bundle?): Intent {
            val intent = Intent()
            if (arguments == null) {
                return intent
            }

            val data = arguments.getParcelable<Uri>("_uri")
            if (data != null) {
                intent.data = data
            }

            intent.putExtras(arguments)
            intent.removeExtra("_uri")
            return intent
        }
    }

}
