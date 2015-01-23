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

package org.dvbviewer.controller.ui.base;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.dvbviewer.controller.R;

/**
 * A {@link BaseActivity} that simply contains a single fragment. The intent used to invoke this
 * activity is forwarded to the fragment as arguments during fragment instantiation. Derived
 * activities should only need to implement
 * {@link com.google.android.apps.iosched.ui.BaseSinglePaneActivity#onCreatePane()}.
 */
public abstract class BaseSinglePaneActivity extends BaseActivity {
    private Fragment mFragment;

    /* (non-Javadoc)
     * @see org.dvbviewer.controller.ui.base.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlepane_empty);

        final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        if (getSupportActionBar() != null) {
        	getSupportActionBar().setTitle(customTitle != null ? customTitle : getTitle());
		}
       
        if (savedInstanceState == null) {
            mFragment = onCreatePane();
            if (mFragment.getArguments() == null) {
            	mFragment.setArguments(intentToFragmentArguments(getIntent()));
			}

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root_container, mFragment, mFragment.getClass().getName())
                    .commit();
        }
    }
    
    

    /**
     * Called in <code>onCreate</code> when the fragment constituting this activity is needed.
     * The returned fragment's arguments will be set to the intent used to invoke this activity.
     *
     * @return the fragment©
     * @author RayBa
     * @date 07.04.2013
     */
    protected abstract Fragment onCreatePane();
    
   
}
