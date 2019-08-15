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

/**
 * The Class Remote.
 *
 * @author RayBa
 * @date 07.04.2013
 */
class RemoteNumbers : AbstractRemote() {

    // Number Buttons
    private var btnOne: Button? = null
    private var btnTwo: Button? = null
    private var btnThree: Button? = null
    private var btnFour: Button? = null
    private var btnFive: Button? = null
    private var btnSix: Button? = null
    private var btnSeven: Button? = null
    private var btnEight: Button? = null
    private var btnNine: Button? = null
    private var btnZero: Button? = null
    private var btnNumberBack: Button? = null
    private var btnNumberForward: Button? = null

    private var content: View? = null

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        content = inflater.inflate(R.layout.fragment_remote_numbers, container, false)
        return content
    }


    /**
     * Inititalize.
     *
     * @date 07.04.2013
     */
    private fun inititalize() {
        // Number Buttons
        btnOne = content!!.findViewById<View>(R.id.ButtonOne) as Button
        btnOne!!.setOnClickListener(this)
        btnTwo = content!!.findViewById<View>(R.id.ButtonTwo) as Button
        btnTwo!!.setOnClickListener(this)
        btnThree = content!!.findViewById<View>(R.id.ButtonThree) as Button
        btnThree!!.setOnClickListener(this)
        btnFour = content!!.findViewById<View>(R.id.ButtonFour) as Button
        btnFour!!.setOnClickListener(this)
        btnFive = content!!.findViewById<View>(R.id.ButtonFive) as Button
        btnFive!!.setOnClickListener(this)
        btnSix = content!!.findViewById<View>(R.id.ButtonSix) as Button
        btnSix!!.setOnClickListener(this)
        btnSeven = content!!.findViewById<View>(R.id.ButtonSeven) as Button
        btnSeven!!.setOnClickListener(this)
        btnEight = content!!.findViewById<View>(R.id.ButtonEight) as Button
        btnEight!!.setOnClickListener(this)
        btnNine = content!!.findViewById<View>(R.id.ButtonNine) as Button
        btnNine!!.setOnClickListener(this)
        btnZero = content!!.findViewById<View>(R.id.ButtonZero) as Button
        btnZero!!.setOnClickListener(this)
        btnNumberBack = content!!.findViewById<View>(R.id.ButtonStepBack_Numbers) as Button
        btnNumberBack!!.setOnClickListener(this)
        btnNumberForward = content!!.findViewById<View>(R.id.ButtonStepForward_Numbers) as Button
        btnNumberForward!!.setOnClickListener(this)
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
            R.id.ButtonOne -> command = ActionID.CMD_REMOTE_1
            R.id.ButtonTwo -> command = ActionID.CMD_REMOTE_2
            R.id.ButtonThree -> command = ActionID.CMD_REMOTE_3
            R.id.ButtonFour -> command = ActionID.CMD_REMOTE_4
            R.id.ButtonFive -> command = ActionID.CMD_REMOTE_5
            R.id.ButtonSix -> command = ActionID.CMD_REMOTE_6
            R.id.ButtonSeven -> command = ActionID.CMD_REMOTE_7
            R.id.ButtonEight -> command = ActionID.CMD_REMOTE_8
            R.id.ButtonNine -> command = ActionID.CMD_REMOTE_9
            R.id.ButtonZero -> command = ActionID.CMD_REMOTE_0
            R.id.ButtonStepBack_Numbers -> command = ActionID.CMD_MOVE_LEFT
            R.id.ButtonStepForward_Numbers -> command = ActionID.CMD_MOVE_RIGHT

            else -> {
            }
        }
        return command
    }

}
