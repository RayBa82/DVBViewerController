package org.dvbviewer.controller.ui;

import android.app.Activity;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.rules.MinimalSyncRule;
import org.dvbviewer.controller.utils.Config;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.anything;


/**
 * Basic tests showcasing simple view matchers and actions like {@link ViewMatchers#withId},
 * {@link ViewActions#click} and {@link ViewActions#typeText}.
 * <p/>
 * Note that there is no need to tell Espresso that a view is in a different {@link Activity}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DefaultDvbViewerTest {

	@Rule
	public MinimalSyncRule<HomeActivity> mActivityRule = new MinimalSyncRule<>(
			HomeActivity.class);


	@Test
	public void showEpg() {
		onView(withId(R.id.home_btn_channels)).perform(click());
		assertEquals(true, Config.CHANNELS_SYNCED);
		onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).perform(click());
	}

}