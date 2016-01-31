package org.dvbviewer.controller.suite;

import org.dvbviewer.controller.io.data.ChannelTest;
import org.dvbviewer.controller.io.data.SynchronizationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ChannelTest.class,
        SynchronizationTest.class})
/**
 * TestSuite for all tests
 */
public class RunAllSuite {}