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
package org.dvbviewer.controller.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.ui.base.AbstractRemote;
import org.dvbviewer.controller.utils.ActionID;

/**
 * The Class Remote.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class RemoteNumbers extends AbstractRemote {

    // Number Buttons
    private Button btnOne = null;
    private Button btnTwo = null;
    private Button btnThree = null;
    private Button btnFour = null;
    private Button btnFive = null;
    private Button btnSix = null;
    private Button btnSeven = null;
    private Button btnEight = null;
    private Button btnNine = null;
    private Button btnZero = null;
    private Button btnNumberBack = null;
    private Button btnNumberForward = null;

    private View content;
    private DVBViewerPreferences prefs;
    private boolean useFavs;
    private OnRemoteButtonClickListener remoteButtonClickListener;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inititalize();
        prefs = new DVBViewerPreferences(getActivity());
        useFavs = prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false);
    }

    /* (non-Javadoc)
         * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
         */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        content = inflater.inflate(R.layout.fragment_remote_numbers, null);
        return content;
    }


    /**
     * Inititalize.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private void inititalize() {
        // Number Buttons
        btnOne = (Button) content.findViewById(R.id.ButtonOne);
        btnOne.setOnClickListener(this);
        btnTwo = (Button) content.findViewById(R.id.ButtonTwo);
        btnTwo.setOnClickListener(this);
        btnThree = (Button) content.findViewById(R.id.ButtonThree);
        btnThree.setOnClickListener(this);
        btnFour = (Button) content.findViewById(R.id.ButtonFour);
        btnFour.setOnClickListener(this);
        btnFive = (Button) content.findViewById(R.id.ButtonFive);
        btnFive.setOnClickListener(this);
        btnSix = (Button) content.findViewById(R.id.ButtonSix);
        btnSix.setOnClickListener(this);
        btnSeven = (Button) content.findViewById(R.id.ButtonSeven);
        btnSeven.setOnClickListener(this);
        btnEight = (Button) content.findViewById(R.id.ButtonEight);
        btnEight.setOnClickListener(this);
        btnNine = (Button) content.findViewById(R.id.ButtonNine);
        btnNine.setOnClickListener(this);
        btnZero = (Button) content.findViewById(R.id.ButtonZero);
        btnZero.setOnClickListener(this);
        btnNumberBack = (Button) content.findViewById(R.id.ButtonStepBack_Numbers);
        btnNumberBack.setOnClickListener(this);
        btnNumberForward = (Button) content.findViewById(R.id.ButtonStepForward_Numbers);
        btnNumberForward.setOnClickListener(this);
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#toString()
     */
    @Override
    public String toString() {
        return "Remote";
    }


    @Override
    public String getCommand(View v) {
        String command = "";
        switch (v.getId()) {
            case R.id.ButtonOne:
                command = useFavs ? ActionID.CMD_FAV_1 : ActionID.CMD_REMOTE_1;
                break;
            case R.id.ButtonTwo:
                command = useFavs ? ActionID.CMD_FAV_2 : ActionID.CMD_REMOTE_2;
                break;
            case R.id.ButtonThree:
                command = useFavs ? ActionID.CMD_FAV_3 : ActionID.CMD_REMOTE_3;
                break;
            case R.id.ButtonFour:
                command = useFavs ? ActionID.CMD_FAV_4 : ActionID.CMD_REMOTE_4;
                break;
            case R.id.ButtonFive:
                command = useFavs ? ActionID.CMD_FAV_5 : ActionID.CMD_REMOTE_5;
                break;
            case R.id.ButtonSix:
                command = useFavs ? ActionID.CMD_FAV_6 : ActionID.CMD_REMOTE_6;
                break;
            case R.id.ButtonSeven:
                command = useFavs ? ActionID.CMD_FAV_7 : ActionID.CMD_REMOTE_7;
                break;
            case R.id.ButtonEight:
                command = useFavs ? ActionID.CMD_FAV_8 : ActionID.CMD_REMOTE_8;
                break;
            case R.id.ButtonNine:
                command = useFavs ? ActionID.CMD_FAV_9 : ActionID.CMD_REMOTE_9;
                break;
            case R.id.ButtonZero:
                command = ActionID.CMD_REMOTE_0;
                break;
            case R.id.ButtonStepBack_Numbers:
                command = ActionID.CMD_MOVE_LEFT;
                break;
            case R.id.ButtonStepForward_Numbers:
                command = ActionID.CMD_MOVE_RIGHT;
                break;

            default:
                break;
        }
        return command;
    }

}
