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
package org.dvbviewer.controller.ui.phone;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.ChannelEpg;
import org.dvbviewer.controller.ui.fragments.ChannelEpg.EpgDateInfo;
import org.dvbviewer.controller.ui.fragments.EpgPager;

import java.util.Date;

/**
 * The Class EpgPagerActivity.
 *
 * @author RayBa
 */
public class EpgPagerActivity extends BaseSinglePaneActivity implements EpgDateInfo{

	private Date epgDate;
	
	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDisplayHomeAsUpEnabled(true);
		epgDate = savedInstanceState != null && savedInstanceState.containsKey(ChannelEpg.KEY_EPG_DAY) ? new Date(savedInstanceState.getLong(ChannelEpg.KEY_EPG_DAY)) : new Date();
	}
	
	
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(ChannelEpg.KEY_EPG_DAY, epgDate.getTime());
		super.onSaveInstanceState(outState);
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.fragments.ChannelEpg.EpgDateInfo#setEpgDate(java.util.Date)
	 */
	@Override
	public void setEpgDate(long epgDate) {
		this.epgDate = new Date(epgDate);
	}


	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.fragments.ChannelEpg.EpgDateInfo#getEpgDate()
	 */
	@Override
	public long getEpgDate() {
		return epgDate.getTime();
	}



	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreatePane()
	 */
	@Override
	protected Fragment onCreatePane() {
		return new EpgPager();
	}

}
