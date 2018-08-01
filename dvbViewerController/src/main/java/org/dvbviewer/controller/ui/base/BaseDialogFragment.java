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


import android.content.Context;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.widget.Toast;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.io.exception.AuthenticationException;
import org.dvbviewer.controller.io.exception.DefaultHttpException;
import org.xml.sax.SAXException;

/**
 * @author RayBa82 on 24.01.2016
 *
 * Base class for Fragments
 */
public class BaseDialogFragment extends AppCompatDialogFragment {

    /**
     * Generic method to catch an Exception.
     * It shows a toast to inform the user.
     * This method is safe to be called from non UI threads.
     *
     * @param e the Excetpion to catch
     */
    protected void catchException(Exception e) {
        if (e instanceof AuthenticationException) {
            showToast(getContext(), getStringSafely(R.string.error_invalid_credentials));
        } else if (e instanceof DefaultHttpException) {
            showToast(getContext(), e.getMessage());
        } else if (e instanceof SAXException) {
            showToast(getContext(), getStringSafely(R.string.error_parsing_xml));
        } else {
            showToast(getContext(), getStringSafely(R.string.error_common) + "\n\n" + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
        }
    }

	/**
	 * Possibility to show a Toastmessage from non UI threads.
     *
     * @param context the context to show the toast
	 * @param message the message to display
	 */
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
     *
     * @return the String for the resource id
     */
    protected String getStringSafely(int resId){
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

}