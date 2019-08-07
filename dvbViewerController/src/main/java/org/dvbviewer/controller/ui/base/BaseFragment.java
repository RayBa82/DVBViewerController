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
package org.dvbviewer.controller.ui.base;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.io.api.APIClient;
import org.dvbviewer.controller.io.api.DMSInterface;
import org.dvbviewer.controller.io.exception.AuthenticationException;
import org.dvbviewer.controller.io.exception.DefaultHttpException;
import org.xml.sax.SAXException;

/**
 * @author RayBa82 on 24.01.2016
 *         <p>
 *         Base class for Fragments
 */
public class BaseFragment extends Fragment {

    protected String TAG = this.getClass().getName();
    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String CONNECTION_EVENT = "CONNECTION_EVENT";
    public static final String MESSAGE_STRING = "MESSAGE_STRING";
    public static final String MESSAGE_ID = "MESSAGE_ID";

    private DMSInterface dmsInterface;

    public DMSInterface getDmsInterface() {
        if (dmsInterface == null) {
            initializeDMSInterface();
        }
        return dmsInterface;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mConnectionReceiver,
                new IntentFilter(BaseFragment.MESSAGE_EVENT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mConnectionReceiver);
    }

    /**
     * Generic method to catch an Exception.
     * It shows a toast to inform the user.
     * This method is safe to be called from non UI threads.
     *
     * @param tag for logging
     * @param e   the Excetpion to catch
     */
    protected void catchException(String tag, Exception e) {
        if(getContext() == null) {
            return;
        }
        Log.e(tag, "Error loading ListData", e);
        final String message;
        if (e instanceof AuthenticationException) {
            message = getString(R.string.error_invalid_credentials);
        } else if (e instanceof DefaultHttpException) {
            message = e.getMessage();
        } else if (e instanceof SAXException) {
            message = getString(R.string.error_parsing_xml);
        } else {
            message = getStringSafely(R.string.error_common)
                    + "\n\n"
                    + (e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        }
        showToast(getContext(), message);
    }

    /**
     * Possibility to show a Toastmessage from non UI threads.
     *
     * @param context the context to show the toast
     * @param message the message to display
     */
    protected void showToast(final Context context, final String message) {
        if (context != null && !isDetached()) {
            Runnable errorRunnable = new Runnable() {

                @Override
                public void run() {
                    if (!TextUtils.isEmpty(message)) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }
                }
            };
            getActivity().runOnUiThread(errorRunnable);
        }
    }

    /**
     * Possibility for sublasses to provide a LayouRessource
     * before constructor is called.
     *
     * @param resId the resource id
     * @return the String for the resource id
     */
    protected String getStringSafely(int resId) {
        String result = "";
        if (!isDetached() && isVisible() && isAdded()) {
            try {
                result = getString(resId);
            } catch (Exception e) {
                // Dirty Exception Handling, because this keeps and keeps crashing...
                e.printStackTrace();
            }
        }
        return result;
    }

    public void sendMessage(String message) {
        Log.d(TAG, "Broadcasting message");
        Intent intent = new Intent(MESSAGE_EVENT);
        intent.putExtra(MESSAGE_STRING, message);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    public void sendMessage(int id) {
        Log.d(TAG, "Broadcasting message");
        Intent intent = new Intent(MESSAGE_EVENT);
        intent.putExtra(MESSAGE_ID, id);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    protected void initializeDMSInterface() {
        dmsInterface = APIClient.INSTANCE.getClient().create(DMSInterface.class);
    }

    private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got connection changed message");
            initializeDMSInterface();
        }
    };

}