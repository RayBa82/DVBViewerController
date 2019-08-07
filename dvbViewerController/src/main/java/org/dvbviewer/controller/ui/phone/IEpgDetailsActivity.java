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

import androidx.fragment.app.Fragment;

import org.dvbviewer.controller.entities.IEPG;
import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.EPGDetails;

/**
 * The Class IEpgDetailsActivity.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class IEpgDetailsActivity extends BaseSinglePaneActivity {
	
	
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
		Bundle bundle = Companion.intentToFragmentArguments(getIntent());
		EPGDetails details = new EPGDetails();
		details.setArguments(bundle);
		return details ;
	}


	public interface OnIEPGClickListener {

		void onIEPGClick(final IEPG iepg);

	}

}
