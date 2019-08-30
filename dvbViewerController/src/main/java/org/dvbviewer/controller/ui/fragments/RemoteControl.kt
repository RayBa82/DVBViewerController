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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_remote_control.*
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.ui.base.AbstractRemote
import org.dvbviewer.controller.utils.ActionID

class RemoteControl : AbstractRemote() {


    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inititalize()
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_remote_control, container, false)
    }

    private fun inititalize() {
        btnMoveDown!!.setOnClickListener(this)
        btnMoveUp!!.setOnClickListener(this)
        btnOK!!.setOnClickListener(this)
        btnMoveLeft!!.setOnClickListener(this)
        btnMoveRight!!.setOnClickListener(this)
        btnBack!!.setOnClickListener(this)
        btnMenu!!.setOnClickListener(this)
        btnVideos!!.setOnClickListener(this)
        btnStepForward!!.setOnClickListener(this)
        btnStepBack!!.setOnClickListener(this)
        btnText!!.setOnClickListener(this)
        btnPause!!.setOnClickListener(this)
        btnStop!!.setOnClickListener(this)
        btnRed!!.setOnClickListener(this)
        btnGreen!!.setOnClickListener(this)
        btnYellow!!.setOnClickListener(this)
        btnBlue!!.setOnClickListener(this)
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#toString()
     */
    override fun toString(): String {
        return "Remote"
    }

    override fun getCommand(v: View): String {
        return when (v.id) {
            R.id.btnMoveUp -> ActionID.CMD_MOVE_UP
            R.id.btnMoveDown -> ActionID.CMD_MOVE_DOWN
            R.id.btnMoveRight -> ActionID.CMD_MOVE_RIGHT
            R.id.btnMoveLeft -> ActionID.CMD_MOVE_LEFT
            R.id.btnOK -> ActionID.CMD_SELECT_ITEM
            R.id.btnBack -> ActionID.CMD_PREVIOUS_MENU
            R.id.btnMenu -> ActionID.CMD_SHOW_OSD
            R.id.btnVideos -> ActionID.CMD_SHOW_VIDEO
            R.id.btnStepForward -> ActionID.CMD_STEP_FORWARD
            R.id.btnStepBack -> ActionID.CMD_STEP_BACK
            R.id.btnText -> ActionID.CMD_SHOW_TELETEXT
            R.id.btnPause -> ActionID.CMD_PAUSE
            R.id.btnStop -> ActionID.CMD_STOP
            R.id.btnRed -> ActionID.CMD_RED
            R.id.btnGreen -> ActionID.CMD_GREEN
            R.id.btnYellow -> ActionID.CMD_YELLOW
            R.id.btnBlue -> ActionID.CMD_BLUE
            else -> {
                StringUtils.EMPTY
            }
        }
    }

}
