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
package org.dvbviewer.controller.ui.phone

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.dvbviewer.controller.data.entities.IEPG
import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity
import org.dvbviewer.controller.ui.fragments.EPGDetails

class IEpgDetailsActivity : BaseSinglePaneActivity() {


    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDisplayHomeAsUpEnabled(true)

    }


    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreatePane()
	 */
    override fun onCreatePane(): Fragment {
        val bundle = intentToFragmentArguments(intent)
        val details = EPGDetails()
        details.arguments = bundle
        return details
    }


    interface OnIEPGClickListener {

        fun onIEPGClick(iepg: IEPG)

    }

}
