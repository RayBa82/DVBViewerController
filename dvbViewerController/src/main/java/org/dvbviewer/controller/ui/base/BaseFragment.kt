/*
 * Copyright © 2016 dvbviewer-controller Project
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
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import org.dvbviewer.controller.R
import org.dvbviewer.controller.io.api.APIClient
import org.dvbviewer.controller.io.api.DMSInterface
import org.dvbviewer.controller.io.exception.AuthenticationException
import org.dvbviewer.controller.io.exception.DefaultHttpException
import org.xml.sax.SAXException

/**
 * @author RayBa82 on 24.01.2016
 *
 *
 * Base class for Fragments
 */
open class BaseFragment : Fragment() {

    protected val TAG = this.javaClass.name

    private lateinit var dmsInterface: DMSInterface

    private val mConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Got connection changed message")
            initializeDMSInterface()
        }
    }

    fun getDmsInterface(): DMSInterface {
        return dmsInterface
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeDMSInterface()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(mConnectionReceiver,
                IntentFilter(MESSAGE_EVENT))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(mConnectionReceiver)
    }

    /**
     * Generic method to catch an Exception.
     * It shows a toast to inform the user.
     * This method is safe to be called from non UI threads.
     *
     * @param tag for logging
     * @param e   the Excetpion to catch
     */
    protected fun catchException(tag: String, e: Exception?) {
        if (context == null) {
            return
        }
        Log.e(tag, "Error loading ListData", e)
        val message: String?
        if (e is AuthenticationException) {
            message = getString(R.string.error_invalid_credentials)
        } else if (e is DefaultHttpException) {
            message = e.message
        } else if (e is SAXException) {
            message = getString(R.string.error_parsing_xml)
        } else {
            message = (getStringSafely(R.string.error_common)
                    + "\n\n"
                    + if (e?.message != null) e.message else e?.javaClass?.name)
        }
        message?.let { showToast(context, it) }
    }

    /**
     * Possibility to show a Toastmessage from non UI threads.
     *
     * @param context the context to show the toast
     * @param message the message to display
     */
    protected fun showToast(context: Context?, message: String) {
        if (context != null && !isDetached) {
            val errorRunnable = Runnable {
                if (!TextUtils.isEmpty(message)) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
            activity!!.runOnUiThread(errorRunnable)
        }
    }

    /**
     * Possibility for sublasses to provide a LayouRessource
     * before constructor is called.
     *
     * @param resId the resource id
     * @return the String for the resource id
     */
    protected open fun getStringSafely(resId: Int): String {
        var result = ""
        if (!isDetached && isVisible && isAdded) {
            try {
                result = getString(resId)
            } catch (e: Exception) {
                // Dirty Exception Handling, because this keeps and keeps crashing...
                e.printStackTrace()
            }

        }
        return result
    }

    fun sendMessage(message: String) {
        Log.d(TAG, "Broadcasting message")
        val intent = Intent(MESSAGE_EVENT)
        intent.putExtra(MESSAGE_STRING, message)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    fun sendMessage(id: Int) {
        Log.d(TAG, "Broadcasting message")
        val intent = Intent(MESSAGE_EVENT)
        intent.putExtra(MESSAGE_ID, id)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    protected fun initializeDMSInterface() {
        dmsInterface = APIClient.client.create(DMSInterface::class.java)
    }

    companion object {
        const val MESSAGE_EVENT = "MESSAGE_EVENT"
        val CONNECTION_EVENT = "CONNECTION_EVENT"
        const val MESSAGE_STRING = "MESSAGE_STRING"
        const val MESSAGE_ID = "MESSAGE_ID"
    }

}