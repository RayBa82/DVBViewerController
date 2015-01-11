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
package org.dvbviewer.controller.ui.phone;

import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.ConnectionPreferenceFragment;

import android.support.v4.app.Fragment;

/**
 * The Class ConnectionPreferencesActivity.
 * 
 * @author RayBa
 * @date 13.04.2012
 */
public class ConnectionPreferencesActivity extends BaseSinglePaneActivity {
	


	@Override
	protected Fragment onCreatePane() {
		// TODO Auto-generated method stub
		return new ConnectionPreferenceFragment();
	}

}
