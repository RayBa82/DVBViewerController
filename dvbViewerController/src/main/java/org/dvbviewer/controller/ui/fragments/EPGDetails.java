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
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.IEPG;
import org.dvbviewer.controller.utils.DateUtils;

/**
 * The Class EPGDetails.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class EPGDetails extends Fragment  {
	

	IEPG epg;


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setTitle(R.string.details);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		epg = getActivity().getIntent().getParcelableExtra(IEPG.class.getSimpleName());
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_epg_details, null);
		TextView channel = (TextView) v.findViewById(R.id.channel);
		TextView date = (TextView) v.findViewById(R.id.date);
		TextView title = (TextView) v.findViewById(R.id.title);
		TextView subTitle = (TextView) v.findViewById(R.id.subTitle);
		TextView desc = (TextView) v.findViewById(R.id.desc);
		String dateString = DateUtils.getDateInLocalFormat(epg.getStart());
		if (DateUtils.isToday(epg.getStart().getTime())) {
			dateString = getResources().getString(R.string.today);
		} else if (DateUtils.isTomorrow(epg.getStart().getTime())) {
			dateString = getResources().getString(R.string.tomorrow);
		}
		String start = DateUtils.getTimeInLocalFormat(getActivity(), epg.getStart());
		String end = DateUtils.getTimeInLocalFormat(getActivity(), epg.getEnd());
		date.setText(dateString + "  " + start + " - " + end);
		channel.setText(epg.getChannel());
		title.setText(epg.getTitle());
		if (TextUtils.isEmpty(epg.getSubTitle())) {
			subTitle.setVisibility(View.GONE);
		}else {
			subTitle.setText(epg.getSubTitle());
		}
		desc.setText(epg.getDescription());
		return v;
	}

	/**
	 * Gets the epg.
	 *
	 * @return the epg
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public IEPG getEpg() {
		return epg;
	}




}
