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

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.io.ServerRequest;
import org.dvbviewer.controller.io.ServerRequest.DVBViewerCommand;
import org.dvbviewer.controller.io.data.TargetHandler;
import org.dvbviewer.controller.io.data.VersionHandler;
import org.dvbviewer.controller.ui.base.AsyncLoader;
import org.dvbviewer.controller.ui.base.BaseActivity.ErrorToastRunnable;
import org.dvbviewer.controller.utils.ActionID;
import org.dvbviewer.controller.utils.Config;
import org.dvbviewer.controller.utils.ServerConsts;
import org.dvbviewer.controller.utils.UIUtils;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.boye.httpclientandroidlib.ParseException;
import ch.boye.httpclientandroidlib.auth.AuthenticationException;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;

/**
 * The Class Remote.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class Remote extends Fragment implements OnTouchListener, OnClickListener, OnLongClickListener, LoaderManager.LoaderCallbacks<List<String>> {

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

    private Animation inFromLeft;
    private Animation outToLeft;
    private Animation inFromRight;
    private Animation outToRight;

    SimpleOnGestureListener gestureListener;
    ViewFlipper flipper = null;
    private GestureDetector detector = null;
    private View content;
    private Toolbar mToolbar;
    private ArrayAdapter mSpinnerAdapter;
    private Spinner mClientSpinner;
    private String version;
    private int spinnerPosition;
    private static final String KEY_SPINNER_POS = "spinnerPosition";
    private DVBViewerPreferences prefs;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!UIUtils.isTablet(getActivity())){
            ((ActionBarActivity) getActivity()).setSupportActionBar(mToolbar);
        }
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        inititalize();
        prefs = new DVBViewerPreferences(getActivity());
        version = prefs.getString(DVBViewerPreferences.KEY_RS_VERSION);
        if (savedInstanceState != null) {
            spinnerPosition = savedInstanceState.getInt(KEY_SPINNER_POS, 0);
        }
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        content = inflater.inflate(R.layout.fragment_remote, null);
        mToolbar = (Toolbar) content.findViewById(R.id.my_awesome_toolbar);

        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                return true;
            }
        });

        mToolbar.setTitle(R.string.remote);
        mClientSpinner = (Spinner) content.findViewById(R.id.clientSpinner);
        mClientSpinner.setVisibility(View.GONE);
        mClientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedClient = (String) mSpinnerAdapter.getItem(position);
                prefs.getPrefs().edit().putString(DVBViewerPreferences.KEY_SELECTED_CLIENT, selectedClient).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Inflate a menu to be displayed in the toolbar
        return content;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SPINNER_POS, mClientSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Inititalize.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private void inititalize() {
        detector = new GestureDetector(getGestureListener());
        flipper = (ViewFlipper) content.findViewById(R.id.flipper);
        flipper.setOnTouchListener(this);
        btnMoveDown = (Button) content.findViewById(R.id.ButtonMoveDown);
        btnMoveDown.setOnTouchListener(this);
        btnMoveDown.setOnClickListener(this);
        btnMoveUp = (Button) content.findViewById(R.id.ButtonMoveUp);
        btnMoveUp.setOnTouchListener(this);
        btnMoveUp.setOnClickListener(this);
        btnOk = (Button) content.findViewById(R.id.ButtonOK);
        btnOk.setOnTouchListener(this);
        btnOk.setOnClickListener(this);
        btnMoveLeft = (Button) content.findViewById(R.id.ButtonMoveLeft);
        btnMoveLeft.setOnTouchListener(this);
        btnMoveLeft.setOnClickListener(this);
        btnMoveRight = (Button) content.findViewById(R.id.ButtonMoveRight);
        btnMoveRight.setOnTouchListener(this);
        btnMoveRight.setOnClickListener(this);
        btnBack = (Button) content.findViewById(R.id.ButtonBack);
        btnBack.setOnTouchListener(this);
        btnBack.setOnClickListener(this);
        btnMenu = (Button) content.findViewById(R.id.ButtonMenu);
        btnMenu.setOnTouchListener(this);
        btnMenu.setOnClickListener(this);
        btnMenu.setOnLongClickListener(this);
        btnVideos = (Button) content.findViewById(R.id.ButtonVideos);
        btnVideos.setOnTouchListener(this);
        btnVideos.setOnClickListener(this);
        btnStepFoward = (Button) content.findViewById(R.id.ButtonStepForward);
        btnStepFoward.setOnTouchListener(this);
        btnStepFoward.setOnClickListener(this);
        btnStepBack = (Button) content.findViewById(R.id.ButtonStepBack);
        btnStepBack.setOnTouchListener(this);
        btnStepBack.setOnClickListener(this);
        btnText = (Button) content.findViewById(R.id.ButtonText);
        btnText.setOnTouchListener(this);
        btnText.setOnClickListener(this);
        btnPause = (Button) content.findViewById(R.id.ButtonPause);
        btnPause.setOnTouchListener(this);
        btnPause.setOnClickListener(this);
        btnStop = (Button) content.findViewById(R.id.ButtonStop);
        btnStop.setOnTouchListener(this);
        btnStop.setOnClickListener(this);
        btnRed = (Button) content.findViewById(R.id.ButtonRed);
        btnRed.setOnTouchListener(this);
        btnRed.setOnClickListener(this);
        btnGreen = (Button) content.findViewById(R.id.ButtonGreen);
        btnGreen.setOnTouchListener(this);
        btnGreen.setOnClickListener(this);
        btnYellow = (Button) content.findViewById(R.id.ButtonYellow);
        btnYellow.setOnTouchListener(this);
        btnYellow.setOnClickListener(this);
        btnBlue = (Button) content.findViewById(R.id.ButtonBlue);
        btnBlue.setOnTouchListener(this);
        btnBlue.setOnClickListener(this);

        // Number Buttons
        btnOne = (Button) content.findViewById(R.id.ButtonOne);
        btnOne.setOnTouchListener(this);
        btnOne.setOnClickListener(this);
        btnTwo = (Button) content.findViewById(R.id.ButtonTwo);
        btnTwo.setOnTouchListener(this);
        btnTwo.setOnClickListener(this);
        btnThree = (Button) content.findViewById(R.id.ButtonThree);
        btnThree.setOnTouchListener(this);
        btnThree.setOnClickListener(this);
        btnFour = (Button) content.findViewById(R.id.ButtonFour);
        btnFour.setOnTouchListener(this);
        btnFour.setOnClickListener(this);
        btnFive = (Button) content.findViewById(R.id.ButtonFive);
        btnFive.setOnTouchListener(this);
        btnFive.setOnClickListener(this);
        btnSix = (Button) content.findViewById(R.id.ButtonSix);
        btnSix.setOnTouchListener(this);
        btnSix.setOnClickListener(this);
        btnSeven = (Button) content.findViewById(R.id.ButtonSeven);
        btnSeven.setOnTouchListener(this);
        btnSeven.setOnClickListener(this);
        btnEight = (Button) content.findViewById(R.id.ButtonEight);
        btnEight.setOnTouchListener(this);
        btnEight.setOnClickListener(this);
        btnNine = (Button) content.findViewById(R.id.ButtonNine);
        btnNine.setOnTouchListener(this);
        btnNine.setOnClickListener(this);
        btnZero = (Button) content.findViewById(R.id.ButtonZero);
        btnZero.setOnTouchListener(this);
        btnZero.setOnClickListener(this);
        btnNumberBack = (Button) content.findViewById(R.id.ButtonStepBack_Numbers);
        btnNumberBack.setOnTouchListener(this);
        btnNumberBack.setOnClickListener(this);
        btnNumberForward = (Button) content.findViewById(R.id.ButtonStepForward_Numbers);
        btnNumberForward.setOnTouchListener(this);
        btnNumberForward.setOnClickListener(this);

        inFromLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.in_from_left);
        outToLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.out_to_left);
        inFromRight = AnimationUtils.loadAnimation(getActivity(), R.anim.in_from_right);
        outToRight = AnimationUtils.loadAnimation(getActivity(), R.anim.out_to_right);
    }

    // @SuppressWarnings("unused")

    /**
     * Gets the gesture listener.
     *
     * @return the gesture listener
     * @author RayBa
     * @date 07.04.2013
     */
    private SimpleOnGestureListener getGestureListener() {
        if (gestureListener == null) {
            gestureListener = new SimpleOnGestureListener() {

                private static final int SWIPE_MIN_DISTANCE = 120;
                private static final int SWIPE_MAX_OFF_PATH = 250;
                private static final int SWIPE_THRESHOLD_VELOCITY = 200;

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    try {
                        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            // SWIPE LEFT
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                                return false;
                            }
                            flipper.setInAnimation(inFromRight);
                            flipper.setOutAnimation(outToLeft);
                            flipper.showPrevious();
                        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            // SWIPE RIGHT
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                                return false;
                            }
                            flipper.setInAnimation(inFromLeft);
                            flipper.setOutAnimation(outToRight);
                            flipper.showNext();
                        } else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                            // SWIPE UP
                            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) {
                            }
                            Toast.makeText(getActivity(), "Swipe up", Toast.LENGTH_SHORT).show();
                        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                            // SWIPE DOWN
                            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) {
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            };
        }
        return gestureListener;
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#toString()
     */
    @Override
    public String toString() {
        return "Remote";
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        boolean isSwitchCommand = false;
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
            case R.id.ButtonOne:
                command = ActionID.CMD_REMOTE_1;
                isSwitchCommand = true;
                break;
            case R.id.ButtonTwo:
                command = ActionID.CMD_REMOTE_2;
                isSwitchCommand = true;
                break;
            case R.id.ButtonThree:
                command = ActionID.CMD_REMOTE_3;
                isSwitchCommand = true;
                break;
            case R.id.ButtonFour:
                command = ActionID.CMD_REMOTE_4;
                isSwitchCommand = true;
                break;
            case R.id.ButtonFive:
                command = ActionID.CMD_REMOTE_5;
                isSwitchCommand = true;
                break;
            case R.id.ButtonSix:
                command = ActionID.CMD_REMOTE_6;
                isSwitchCommand = true;
                break;
            case R.id.ButtonSeven:
                command = ActionID.CMD_REMOTE_7;
                isSwitchCommand = true;
                break;
            case R.id.ButtonEight:
                command = ActionID.CMD_REMOTE_8;
                isSwitchCommand = true;
                break;
            case R.id.ButtonNine:
                command = ActionID.CMD_REMOTE_9;
                isSwitchCommand = true;
                break;
            case R.id.ButtonZero:
                command = ActionID.CMD_REMOTE_0;
                isSwitchCommand = true;
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
        String request = "";
        if (isSwitchCommand) {
            request = MessageFormat.format(ServerConsts.URL_SWITCH_COMMAND, mClientSpinner.getSelectedItem(), command);
        } else {
            request = MessageFormat.format(ServerConsts.URL_SEND_COMMAND, mClientSpinner.getSelectedItem(), command);
        }
        DVBViewerCommand httpCommand = new DVBViewerCommand(request);
        Thread executionThread = new Thread(httpCommand);
        executionThread.start();
    }


    /* (non-Javadoc)
     * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
     */
    @Override
    public boolean onLongClick(View v) {
        String command = "";
        switch (v.getId()) {
            case R.id.ButtonMenu:
                String request = MessageFormat.format(ServerConsts.URL_SEND_COMMAND, mClientSpinner.getSelectedItem(), command);
                DVBViewerCommand httpCommand = new DVBViewerCommand(request);
                Thread executionThread = new Thread(httpCommand);
                executionThread.start();
                return true;

            default:
                return false;
        }

    }

    /* (non-Javadoc)
     * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    /**
     * Show toast.
     *
     * @param message the message
     * @author RayBa
     * @date 07.04.2013
     */
    protected void showToast(String message) {
        if (getActivity() != null) {
            ErrorToastRunnable errorRunnable = new ErrorToastRunnable(getActivity(), message);
            getActivity().runOnUiThread(errorRunnable);
        }
    }

    @Override
    public Loader<List<String>> onCreateLoader(int id, Bundle args) {
        AsyncLoader<List<String>> loader = new AsyncLoader<List<String>>(getActivity().getApplicationContext()) {

            @Override
            public List<String> loadInBackground() {
                List<String> result = null;
                String jsonClients = prefs.getPrefs().getString(DVBViewerPreferences.KEY_RS_CLIENTS, "");
                Gson gson = new Gson();
                Type type = new TypeToken<List<String>>() {}.getType();
                result = gson.fromJson(jsonClients, type);
                if (result != null && !result.isEmpty()) {
                    String activeClient = prefs.getString(DVBViewerPreferences.KEY_SELECTED_CLIENT);
                    int index = result.indexOf(activeClient);
                    spinnerPosition = index > -1 ? index : 0;
                    return result;
                }
                try {
                    if (TextUtils.isEmpty(version)) {
                        Log.i(Remote.class.getSimpleName(), "version: " + version);
                        String versionXml = ServerRequest.getRSString(ServerConsts.URL_VERSION);
                        VersionHandler versionHandler = new VersionHandler();
                        version = versionHandler.parse(versionXml);
                        SharedPreferences.Editor prefEditor = prefs.getPrefs().edit();
                        prefEditor.putString(DVBViewerPreferences.KEY_RS_VERSION, version);
                        prefEditor.commit();
                    }
                    if (!Config.isRemoteSupported(version)) {
                        result = new ArrayList<>();
                        result.add(getString(R.string.version_unsupported_title));
                        return result;
                    }
                    String xml = ServerRequest.getRSString(ServerConsts.URL_TARGETS);
                    TargetHandler handler = new TargetHandler();
                    result = handler.parse(xml);
                    Collections.sort(result);
                    SharedPreferences.Editor prefEditor = prefs.getPrefs().edit();
                    jsonClients = gson.toJson(result, type);
                    prefEditor.putString(DVBViewerPreferences.KEY_RS_CLIENTS, jsonClients);
                    prefEditor.commit();
                } catch (AuthenticationException e) {
                    e.printStackTrace();
                    showToast(getString(R.string.error_invalid_credentials));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    showToast(getString(R.string.error_unknonwn_host) + "\n\n" + ServerConsts.REC_SERVICE_URL);
                } catch (ConnectTimeoutException e) {
                    e.printStackTrace();
                    showToast(getString(R.string.error_connection_timeout));
                } catch (SAXException e) {
                    e.printStackTrace();
                    showToast(getString(R.string.error_parsing_xml));
                } catch (ParseException e) {
                    e.printStackTrace();
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    e.printStackTrace(printWriter);
                    String s = writer.toString();
                    showToast(getString(R.string.error_common) + "\n\n" + s);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    e.printStackTrace(printWriter);
                    String s = writer.toString();
                    showToast(getString(R.string.error_common) + "\n\n" + s);
                } catch (IOException e) {
                    e.printStackTrace();
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    e.printStackTrace(printWriter);
                    String s = writer.toString();
                    showToast(getString(R.string.error_common) + "\n\n" + s);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    showToast(getString(R.string.error_invalid_url) + "\n\n" + ServerConsts.REC_SERVICE_URL);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    showToast(getString(R.string.error_invalid_url) + "\n\n" + ServerConsts.REC_SERVICE_URL);
                } catch (IllegalArgumentException e) {
                    showToast(getString(R.string.error_invalid_url) + "\n\n" + ServerConsts.REC_SERVICE_URL);
                } catch (Exception e) {
                    e.printStackTrace();
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    e.printStackTrace(printWriter);
                    String s = writer.toString();
                    showToast(getString(R.string.error_common) + "\n\n" + s);
                }
                return result;
            }
        };
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<String>> loader, List<String> data) {
        try {

            if (data != null && !data.isEmpty() && !data.get(0).equals(getString(R.string.version_unsupported_title))) {
                String[] arr = new String[data.size()];
                mSpinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, data.toArray(arr));
                mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                mClientSpinner.setAdapter(mSpinnerAdapter);
                mClientSpinner.setSelection(spinnerPosition);
                mClientSpinner.setVisibility(View.VISIBLE);
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.version_unsupported_title)
                        .setMessage(R.string.version_unsupported_text)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }catch (Exception e){

        }

    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {

    }

}
