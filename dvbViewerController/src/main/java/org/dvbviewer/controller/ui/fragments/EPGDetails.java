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

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.IEPG;
import org.dvbviewer.controller.ui.base.BaseDialogFragment;
import org.dvbviewer.controller.utils.DateUtils;

/**
 * Fragment for EPG details or Timer details.
 */
public class EPGDetails extends BaseDialogFragment {
	

	IEPG epg;
	private TextView channel;
	private TextView date;
	private TextView title;
	private TextView subTitle;
	private TextView desc;


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		epg = getArguments().getParcelable(IEPG.class.getSimpleName());
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (epg != null){
			String dateString = DateUtils.getDateInLocalFormat(epg.getStart());
			if (DateUtils.isToday(epg.getStart().getTime())) {
				dateString = getResources().getString(R.string.today);
			} else if (DateUtils.isTomorrow(epg.getStart().getTime())) {
				dateString = getResources().getString(R.string.tomorrow);
			}
			String start = DateUtils.getTimeInLocalFormat(getContext(), epg.getStart());
			String end = DateUtils.getTimeInLocalFormat(getContext(), epg.getEnd());
			date.setText(dateString + "  " + start + " - " + end);
			channel.setText(epg.getChannel());
			title.setText(epg.getTitle());
			if (TextUtils.isEmpty(epg.getSubTitle())) {
				subTitle.setVisibility(View.GONE);
			}else {
				subTitle.setText(epg.getSubTitle());
			}
			desc.setText(epg.getDescription());
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(R.string.details);
		return dialog;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_epg_details, container, false);
		channel = (TextView) v.findViewById(R.id.channel);
		date = (TextView) v.findViewById(R.id.date);
		title = (TextView) v.findViewById(R.id.title);
		subTitle = (TextView) v.findViewById(R.id.subTitle);
		desc = (TextView) v.findViewById(R.id.desc);
		return v;
	}

	/**
	 * Gets the epg.
	 *
	 * @return the epg
	 */
	public IEPG getEpg() {
		return epg;
	}




}
