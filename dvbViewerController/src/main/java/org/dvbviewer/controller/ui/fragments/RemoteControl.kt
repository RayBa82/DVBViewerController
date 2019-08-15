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
import android.widget.Button

import org.dvbviewer.controller.R
import org.dvbviewer.controller.ui.base.AbstractRemote
import org.dvbviewer.controller.utils.ActionID

class RemoteControl : AbstractRemote() {

    // Controller Buttons
    private var btnMoveUp: Button? = null
    private var btnMoveDown: Button? = null
    private var btnMoveRight: Button? = null
    private var btnMoveLeft: Button? = null
    private var btnOk: Button? = null
    private var btnBack: Button? = null
    private var btnMenu: Button? = null
    private var btnVideos: Button? = null
    private var btnStepFoward: Button? = null
    private var btnStepBack: Button? = null
    private var btnText: Button? = null
    private var btnPause: Button? = null
    private var btnStop: Button? = null
    private var btnRed: Button? = null
    private var btnGreen: Button? = null
    private var btnYellow: Button? = null
    private var btnBlue: Button? = null

    // Number Buttons


    private var content: View? = null

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
        content = inflater.inflate(R.layout.fragment_remote_control, container, false)
        return content
    }

    /**
     * Inititalize.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private fun inititalize() {
        btnMoveDown = content!!.findViewById<View>(R.id.ButtonMoveDown) as Button
        btnMoveDown!!.setOnClickListener(this)
        btnMoveUp = content!!.findViewById<View>(R.id.ButtonMoveUp) as Button
        btnMoveUp!!.setOnClickListener(this)
        btnOk = content!!.findViewById<View>(R.id.ButtonOK) as Button
        btnOk!!.setOnClickListener(this)
        btnMoveLeft = content!!.findViewById<View>(R.id.ButtonMoveLeft) as Button
        btnMoveLeft!!.setOnClickListener(this)
        btnMoveRight = content!!.findViewById<View>(R.id.ButtonMoveRight) as Button
        btnMoveRight!!.setOnClickListener(this)
        btnBack = content!!.findViewById<View>(R.id.ButtonBack) as Button
        btnBack!!.setOnClickListener(this)
        btnMenu = content!!.findViewById<View>(R.id.ButtonMenu) as Button
        btnMenu!!.setOnClickListener(this)
        btnVideos = content!!.findViewById<View>(R.id.ButtonVideos) as Button
        btnVideos!!.setOnClickListener(this)
        btnStepFoward = content!!.findViewById<View>(R.id.ButtonStepForward) as Button
        btnStepFoward!!.setOnClickListener(this)
        btnStepBack = content!!.findViewById<View>(R.id.ButtonStepBack) as Button
        btnStepBack!!.setOnClickListener(this)
        btnText = content!!.findViewById<View>(R.id.ButtonText) as Button
        btnText!!.setOnClickListener(this)
        btnPause = content!!.findViewById<View>(R.id.ButtonPause) as Button
        btnPause!!.setOnClickListener(this)
        btnStop = content!!.findViewById<View>(R.id.ButtonStop) as Button
        btnStop!!.setOnClickListener(this)
        btnRed = content!!.findViewById<View>(R.id.ButtonRed) as Button
        btnRed!!.setOnClickListener(this)
        btnGreen = content!!.findViewById<View>(R.id.ButtonGreen) as Button
        btnGreen!!.setOnClickListener(this)
        btnYellow = content!!.findViewById<View>(R.id.ButtonYellow) as Button
        btnYellow!!.setOnClickListener(this)
        btnBlue = content!!.findViewById<View>(R.id.ButtonBlue) as Button
        btnBlue!!.setOnClickListener(this)
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#toString()
     */
    override fun toString(): String {
        return "Remote"
    }

    override fun getCommand(v: View): String {
        var command = ""
        when (v.id) {
            R.id.ButtonMoveUp -> command = ActionID.CMD_MOVE_UP
            R.id.ButtonMoveDown -> command = ActionID.CMD_MOVE_DOWN
            R.id.ButtonMoveRight -> command = ActionID.CMD_MOVE_RIGHT
            R.id.ButtonMoveLeft -> command = ActionID.CMD_MOVE_LEFT
            R.id.ButtonOK -> command = ActionID.CMD_SELECT_ITEM
            R.id.ButtonBack -> command = ActionID.CMD_PREVIOUS_MENU
            R.id.ButtonMenu -> command = ActionID.CMD_SHOW_OSD
            R.id.ButtonVideos -> command = ActionID.CMD_SHOW_VIDEO
            R.id.ButtonStepForward -> command = ActionID.CMD_STEP_FORWARD
            R.id.ButtonStepBack -> command = ActionID.CMD_STEP_BACK
            R.id.ButtonText -> command = ActionID.CMD_SHOW_TELETEXT
            R.id.ButtonPause -> command = ActionID.CMD_PAUSE
            R.id.ButtonStop -> command = ActionID.CMD_STOP
            R.id.ButtonRed -> command = ActionID.CMD_RED
            R.id.ButtonGreen -> command = ActionID.CMD_GREEN
            R.id.ButtonYellow -> command = ActionID.CMD_YELLOW
            R.id.ButtonBlue -> command = ActionID.CMD_BLUE
            R.id.ButtonStepBack_Numbers -> command = ActionID.CMD_MOVE_LEFT
            R.id.ButtonStepForward_Numbers -> command = ActionID.CMD_MOVE_RIGHT

            else -> {
            }
        }
        return command
    }

}
