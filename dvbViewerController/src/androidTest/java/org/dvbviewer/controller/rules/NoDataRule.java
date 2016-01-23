package org.dvbviewer.controller.rules;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import org.dvbviewer.controller.utils.Config;

/**
 * Created by r.baun on 14.06.2015.
 */
public class NoDataRule<T extends Activity> extends ActivityTestRule {


	public NoDataRule(Class<T> activityClass) {
		super(activityClass);
	}

	@Override
	protected void beforeActivityLaunched() {
		super.beforeActivityLaunched();
		Config.IS_FIRST_START = false;
		Config.CHANNELS_SYNCED = false;
	}


}
