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
package org.dvbviewer.controller.ui.base;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.fragment.app.Fragment;

/**
 * The Class Remote.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public abstract class AbstractRemote extends Fragment implements OnClickListener {

    protected OnRemoteButtonClickListener remoteButtonClickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRemoteButtonClickListener){
            remoteButtonClickListener = (OnRemoteButtonClickListener)context;
        }else if(getParentFragment() instanceof OnRemoteButtonClickListener){
            remoteButtonClickListener = (OnRemoteButtonClickListener)getParentFragment();
        }
    }


    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        String command = getCommand(v);
        if (remoteButtonClickListener != null){
            remoteButtonClickListener.OnRemoteButtonClick(command);
        }
    }

    public abstract String getCommand(View v);

    // Container Activity or Fragment must implement this interface
    public interface OnRemoteButtonClickListener
    {
        void OnRemoteButtonClick(String action);
    }

}
