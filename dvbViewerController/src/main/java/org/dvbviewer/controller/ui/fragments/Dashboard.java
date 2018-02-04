/*
 * Copyright © 2012 dvbviewer-controller Project
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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.dvbviewer.controller.R;

/**
 * The Class Dashboard.
 *
 * @author RayBa
 * @date 01.07.2012
 */
public class Dashboard extends Fragment implements OnClickListener{
	
	private OnDashboardButtonClickListener mDashboardButtonClickListener;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
		 // Attach event handlers
        root.findViewById(R.id.home_btn_remote).setOnClickListener(this);
        root.findViewById(R.id.home_btn_channels).setOnClickListener(this);
        root.findViewById(R.id.home_btn_timers).setOnClickListener(this);
        root.findViewById(R.id.home_btn_recordings).setOnClickListener(this);
        root.findViewById(R.id.home_btn_settings).setOnClickListener(this);
        root.findViewById(R.id.home_btn_tasks).setOnClickListener(this);
        root.findViewById(R.id.home_btn_status).setOnClickListener(this);
		root.findViewById(R.id.home_btn_medias).setOnClickListener(this);
		return root;
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnDashboardButtonClickListener) {
			mDashboardButtonClickListener = (OnDashboardButtonClickListener) activity;
		}
	}
	
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (mDashboardButtonClickListener != null) {
			mDashboardButtonClickListener.onDashboarButtonClick(v);
		}
	}
	
	/**
	 * The listener interface for receiving onDashboardButtonClick events.
	 * The class that is interested in processing a onDashboardButtonClick
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addOnDashboardButtonClickListener<code> method. When
	 * the onDashboardButtonClick event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @author RayBa
	 */
	public interface OnDashboardButtonClickListener {

		/**
		 * On dashboar button click.
		 *
		 * @param view the clicked View
		 */
		void onDashboarButtonClick(View view);

	}

}
