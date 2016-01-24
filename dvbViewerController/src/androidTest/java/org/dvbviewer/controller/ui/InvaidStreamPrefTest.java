package org.dvbviewer.controller.ui;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.rules.InvalidStreamPrefRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;


/**
 * Basic tests with no data, just dont crash ;-)
 */
@RunWith(AndroidJUnit4.class)
public class InvaidStreamPrefTest {

	private static final String DEFAULT_ENCODING_SPEED = "ultrafast";

	@Rule
	public InvalidStreamPrefRule<HomeActivity> mActivityRule = new InvalidStreamPrefRule<>(
			HomeActivity.class);


	@Test
	public void testInvalidEncodingSpeed() {
		onView(withId(R.id.home_btn_channels)).perform(click());
		onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).onChildView(withId(R.id.contextMenu))
				.perform(click());
		onView(withText(InstrumentationRegistry.getTargetContext().getString(R.string.stream)))
				.perform(click());
		onView(withId(R.id.encodingSpeedSpinner)).check(matches(withSpinnerText(DEFAULT_ENCODING_SPEED)));
	}

}