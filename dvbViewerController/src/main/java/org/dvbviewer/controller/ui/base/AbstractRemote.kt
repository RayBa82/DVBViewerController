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

import android.content.Context
import android.view.View
import android.view.View.OnClickListener

import androidx.fragment.app.Fragment

/**
 * The Class Remote.
 *
 * @author RayBa
 * @date 07.04.2013
 */
abstract class AbstractRemote : Fragment(), OnClickListener {

    protected var remoteButtonClickListener: OnRemoteButtonClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRemoteButtonClickListener) {
            remoteButtonClickListener = context
        } else if (parentFragment is OnRemoteButtonClickListener) {
            remoteButtonClickListener = parentFragment as OnRemoteButtonClickListener?
        }
    }


    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    override fun onClick(v: View) {
        val command = getCommand(v)
        if (remoteButtonClickListener != null) {
            remoteButtonClickListener!!.OnRemoteButtonClick(command)
        }
    }

    abstract fun getCommand(v: View): String

    // Container Activity or Fragment must implement this interface
    interface OnRemoteButtonClickListener {
        fun OnRemoteButtonClick(action: String)
    }

}
