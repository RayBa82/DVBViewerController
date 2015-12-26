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
import org.dvbviewer.controller.ui.base.AbstractRemote;
import org.dvbviewer.controller.utils.ActionID;

/**
 * The Class Remote.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class RemoteControl extends AbstractRemote {

    // Controller Buttons
    private Button btnMoveUp = null;
    private Button btnMoveDown = null;
    private Button btnMoveRight = null;
    private Button btnMoveLeft = null;
    private Button btnOk = null;
    private Button btnBack = null;
    private Button btnMenu = null;
    private Button btnVideos = null;
    private Button btnStepFoward = null;
    private Button btnStepBack = null;
    private Button btnText = null;
    private Button btnPause = null;
    private Button btnStop = null;
    private Button btnRed = null;
    private Button btnGreen = null;
    private Button btnYellow = null;
    private Button btnBlue = null;

    // Number Buttons


    private View content;
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
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        content = inflater.inflate(R.layout.fragment_remote_control, null);
        return content;
    }

    /**
     * Inititalize.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private void inititalize() {
        btnMoveDown = (Button) content.findViewById(R.id.ButtonMoveDown);
        btnMoveDown.setOnClickListener(this);
        btnMoveUp = (Button) content.findViewById(R.id.ButtonMoveUp);
        btnMoveUp.setOnClickListener(this);
        btnOk = (Button) content.findViewById(R.id.ButtonOK);
        btnOk.setOnClickListener(this);
        btnMoveLeft = (Button) content.findViewById(R.id.ButtonMoveLeft);
        btnMoveLeft.setOnClickListener(this);
        btnMoveRight = (Button) content.findViewById(R.id.ButtonMoveRight);
        btnMoveRight.setOnClickListener(this);
        btnBack = (Button) content.findViewById(R.id.ButtonBack);
        btnBack.setOnClickListener(this);
        btnMenu = (Button) content.findViewById(R.id.ButtonMenu);
        btnMenu.setOnClickListener(this);
        btnVideos = (Button) content.findViewById(R.id.ButtonVideos);
        btnVideos.setOnClickListener(this);
        btnStepFoward = (Button) content.findViewById(R.id.ButtonStepForward);
        btnStepFoward.setOnClickListener(this);
        btnStepBack = (Button) content.findViewById(R.id.ButtonStepBack);
        btnStepBack.setOnClickListener(this);
        btnText = (Button) content.findViewById(R.id.ButtonText);
        btnText.setOnClickListener(this);
        btnPause = (Button) content.findViewById(R.id.ButtonPause);
        btnPause.setOnClickListener(this);
        btnStop = (Button) content.findViewById(R.id.ButtonStop);
        btnStop.setOnClickListener(this);
        btnRed = (Button) content.findViewById(R.id.ButtonRed);
        btnRed.setOnClickListener(this);
        btnGreen = (Button) content.findViewById(R.id.ButtonGreen);
        btnGreen.setOnClickListener(this);
        btnYellow = (Button) content.findViewById(R.id.ButtonYellow);
        btnYellow.setOnClickListener(this);
        btnBlue = (Button) content.findViewById(R.id.ButtonBlue);
        btnBlue.setOnClickListener(this);
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
            case R.id.ButtonMoveUp:
                command = ActionID.CMD_MOVE_UP;
                break;
            case R.id.ButtonMoveDown:
                command = ActionID.CMD_MOVE_DOWN;
                break;
            case R.id.ButtonMoveRight:
                command = ActionID.CMD_MOVE_RIGHT;
                break;
            case R.id.ButtonMoveLeft:
                command = ActionID.CMD_MOVE_LEFT;
                break;
            case R.id.ButtonOK:
                command = ActionID.CMD_SELECT_ITEM;
                break;
            case R.id.ButtonBack:
                command = ActionID.CMD_PREVIOUS_MENU;
                break;
            case R.id.ButtonMenu:
                command = ActionID.CMD_SHOW_OSD;
                break;
            case R.id.ButtonVideos:
                command = ActionID.CMD_SHOW_VIDEO;
                break;
            case R.id.ButtonStepForward:
                command = ActionID.CMD_STEP_FORWARD;
                break;
            case R.id.ButtonStepBack:
                command = ActionID.CMD_STEP_BACK;
                break;
            case R.id.ButtonText:
                command = ActionID.CMD_SHOW_TELETEXT;
                break;
            case R.id.ButtonPause:
                command = ActionID.CMD_PAUSE;
                break;
            case R.id.ButtonStop:
                command = ActionID.CMD_STOP;
                break;
            case R.id.ButtonRed:
                command = ActionID.CMD_RED;
                break;
            case R.id.ButtonGreen:
                command = ActionID.CMD_GREEN;
                break;
            case R.id.ButtonYellow:
                command = ActionID.CMD_YELLOW;
                break;
            case R.id.ButtonBlue:
                command = ActionID.CMD_BLUE;
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
