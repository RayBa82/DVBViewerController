package org.dvbviewer.controller.ui.phone

import androidx.fragment.app.Fragment

import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity
import org.dvbviewer.controller.ui.fragments.AboutFragment

class AboutActivity : BaseSinglePaneActivity() {

    override fun onCreatePane(): Fragment {
        return AboutFragment()
    }

}
