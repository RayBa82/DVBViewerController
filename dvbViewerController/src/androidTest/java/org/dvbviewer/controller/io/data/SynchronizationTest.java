package org.dvbviewer.controller.io.data;


import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.test.R;
import org.dvbviewer.controller.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SynchronizationTest extends InstrumentationTestCase {


    @Test
    public void parseChannels() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.chan_list_user_1);
        assertNotNull(channelRoots);
        assertEquals(1, channelRoots.size());
        ChannelRoot root = channelRoots.get(0);
        assertNotNull(root.getGroups());
        assertEquals(34, root.getGroups().size());
    }

    @Test
    public void parseFavourites() {
        List<ChannelGroup> channelGroups = getFavs(R.raw.fav_list_user_1);
        assertNotNull(channelGroups);
        assertEquals(false, channelGroups.isEmpty());
    }

    @Test
    public void matchFavourites() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.chan_list_user_1);
        List<ChannelGroup> channelGroups = getFavs(R.raw.fav_list_user_1);
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        List<Channel> favs = new ArrayList<>();
        for (ChannelGroup favGroup : favGroups){
            favs.addAll(favGroup.getChannels());
        }
        assertEquals(30, favs.size());
    }

    @Nullable
    private List<ChannelRoot> getChannels(int resId) {
        String chanXml = TestUtils.getStringFromFile(InstrumentationRegistry.getContext(), resId);
        ChannelHandler chanHandler = new ChannelHandler();
        List<ChannelRoot> channelRoots = null;
        try {
            channelRoots = chanHandler.parse(chanXml);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return channelRoots;
    }

    @Nullable
    private List<ChannelGroup> getFavs(int resId) {
        String favXml = TestUtils.getStringFromFile(InstrumentationRegistry.getContext(), resId);
        FavouriteHandler favHandler = new FavouriteHandler();
        List<ChannelGroup> channelGroups = null;
        try {
            channelGroups = favHandler.parse(InstrumentationRegistry.getContext(), favXml);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return channelGroups;
    }

}
