package org.dvbviewer.controller.ui;

import android.support.test.runner.AndroidJUnit4;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.rules.NoDataRule;
import org.dvbviewer.controller.utils.Config;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertEquals;


/**
 * Basic tests with no data, just dont crash ;-)
 */
@RunWith(AndroidJUnit4.class)
public class NoDataTest {

	@Rule
	public NoDataRule<HomeActivity> mActivityRule = new NoDataRule<>(
			HomeActivity.class);


	@Test
	public void emptyStart() {
		onView(withId(R.id.home_btn_channels)).perform(click());
		assertEquals(false, Config.CHANNELS_SYNCED);
	}

}