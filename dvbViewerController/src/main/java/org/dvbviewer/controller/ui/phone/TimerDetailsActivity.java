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

import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.TimerDetails;
import org.dvbviewer.controller.ui.fragments.TimerDetails.OnTimerEditedListener;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * The Class TimerDetailsActivity.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class TimerDetailsActivity extends BaseSinglePaneActivity implements OnTimerEditedListener {
	
	
	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}



	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreatePane()
	 */
	@Override
	protected Fragment onCreatePane() {
		return  new TimerDetails();
	}



	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.fragments.TimerDetails.OnTimerEditedListener#timerEdited(boolean)
	 */
	@Override
	public void timerEdited(boolean edited) {
		if (edited) {
			setResult(TimerDetails.RESULT_CHANGED);
		}else {
			setResult(TimerDetails.RESULT_NO_CHANGE);
		}
		finish();
	}

}
