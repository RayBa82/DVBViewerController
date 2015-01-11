package org.dvbviewer.controller.ui.phone;

import org.dvbviewer.controller.ui.base.BaseSinglePaneActivity;
import org.dvbviewer.controller.ui.fragments.AboutFragment;

import android.support.v4.app.Fragment;

public class AboutActivity extends BaseSinglePaneActivity{

	@Override
	protected Fragment onCreatePane() {
		AboutFragment frag = new AboutFragment();
		return frag;
	}

}
