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
import android.view.MenuItem;

import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.TimerList;

/**
 * The Class TimerlistActivity.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class TimerlistActivity extends BaseSinglePaneActivity {
	
	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDisplayHomeAsUpEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			onBackPressed();
			return true;

		default:
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreatePane()
	 */
	@Override
	protected Fragment onCreatePane() {
		return new TimerList();
	}

}