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

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import org.dvbviewer.controller.entities.Timer;
import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.TimerDetails;
import org.dvbviewer.controller.ui.fragments.TimerDetails.OnTimerEditedListener;

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
		setDisplayHomeAsUpEnabled(true);
	}



	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreatePane()
	 */
	@Override
	protected Fragment onCreatePane() {
		final TimerDetails details = new TimerDetails();
		details.setArguments(Companion.intentToFragmentArguments(getIntent()));
		return details;
	}



	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.fragments.TimerDetails.OnTimerEditedListener#timerEdited(boolean)
	 */
	@Override
	public void timerEdited(Timer timer) {
		if (timer != null) {
			final Intent intent = new Intent();
			intent.putExtra("timer", timer);
			setResult(TimerDetails.Companion.getRESULT_CHANGED(), intent);
		}
		finish();
	}

}
