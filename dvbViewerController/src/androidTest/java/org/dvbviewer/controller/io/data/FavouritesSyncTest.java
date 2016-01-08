package org.dvbviewer.controller.io.data;


import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.test.R;
import org.dvbviewer.controller.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class FavouritesSyncTest extends InstrumentationTestCase {


    @Test
    public void parseChannels() {
        String chanXml = TestUtils.getStringFromFile(InstrumentationRegistry.getContext(), R.raw.chan_list_1);
        ChannelHandler chanHandler = new ChannelHandler();
        List<ChannelRoot> channelRoots = null;
        try {
            channelRoots = chanHandler.parse(chanXml);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        assertNotNull(channelRoots);
        assertEquals(1, channelRoots.size());
        ChannelRoot root = channelRoots.get(0);
        assertNotNull(root.getGroups());
        assertEquals(34, root.getGroups().size());
    }

    @Test
    public void parseFavourites() {
        String favXml = TestUtils.getStringFromFile(InstrumentationRegistry.getContext(), R.raw.fav_list_1);
        FavouriteHandler favHandler = new FavouriteHandler();
        List<ChannelGroup> channelGroups = null;
        try {
            channelGroups = favHandler.parse(InstrumentationRegistry.getContext(), favXml);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        assertNotNull(channelGroups);
        assertEquals(false, channelGroups.isEmpty());
    }

}
