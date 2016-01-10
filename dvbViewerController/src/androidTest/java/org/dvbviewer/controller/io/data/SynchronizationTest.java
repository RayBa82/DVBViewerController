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
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SynchronizationTest extends InstrumentationTestCase {


    @Test
    public void parseChannels() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.user1);
        assertNotNull(channelRoots);
        assertEquals(1, channelRoots.size());
        ChannelRoot root = channelRoots.get(0);
        assertNotNull(root.getGroups());
        assertEquals(34, root.getGroups().size());
    }

    @Test
    public void parseFavourites() {
        List<ChannelGroup> channelGroups = getFavs(R.raw.user1);
        assertNotNull(channelGroups);
        assertEquals(false, channelGroups.isEmpty());
    }

    @Test
    public void matchFavouritesUser1() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.user1);
        List<ChannelGroup> channelGroups = getFavs(R.raw.user1);
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        assertEquals(30, countFavs(favGroups));
    }

    @Test
    public void matchFavouritesUser2() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.user2);
        List<ChannelGroup> channelGroups = getFavs(R.raw.user2);
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        assertEquals(1, favGroups.size());
        assertEquals(30, countFavs(favGroups));
    }

    @Test
    public void matchFavouritesUser3() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.user3);
        List<ChannelGroup> channelGroups = getFavs(R.raw.user3);
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        assertEquals(8, favGroups.size());
        assertEquals(82, countFavs(favGroups));
    }

    @Test
    public void matchFavouritesUser4() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.user4);
        List<ChannelGroup> channelGroups = getFavs(R.raw.user4);
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        assertEquals(15, favGroups.size());
        assertEquals(429, countFavs(favGroups));
    }

    @Test
    public void matchFavouritesUser5() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.user5);
        List<ChannelGroup> channelGroups = getFavs(R.raw.user5);
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        assertEquals(4, favGroups.size());
        assertEquals(79, countFavs(favGroups));
    }

    @Test
    public void matchFavouritesUser6() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.user6);
        List<ChannelGroup> channelGroups = getFavs(R.raw.user6);
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        assertEquals(18, favGroups.size());
        assertEquals(422, countFavs(favGroups));
    }

    @Test
    public void matchFavouritesUser7() {
        List<ChannelRoot> channelRoots = getChannels(R.raw.user7);
        List<ChannelGroup> channelGroups = getFavs(R.raw.user7);
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        assertEquals(4, favGroups.size());
        assertEquals(175, countFavs(favGroups));
    }

    @Test
    public void matchEmptyLists() {
        List<ChannelRoot> channelRoots = new LinkedList<>();
        List<ChannelGroup> channelGroups = new LinkedList<>();
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        assertEquals(0, favGroups.size());
        assertEquals(0, countFavs(favGroups));
    }

    @Test
    public void matchNullLists() {
        List<ChannelRoot> channelRoots = null;
        List<ChannelGroup> channelGroups = null;
        FavMatcher matcher = new FavMatcher();
        List<ChannelGroup> favGroups = matcher.matchFavs(channelRoots, channelGroups);
        assertEquals(0, favGroups.size());
        assertEquals(0, countFavs(favGroups));
    }


    private int countFavs( List<ChannelGroup> favGroups){
        List<Channel> favs = new ArrayList<>();
        for (ChannelGroup favGroup : favGroups){
            favs.addAll(favGroup.getChannels());
        }
        return favs.size();
    }

    @Nullable
    private List<ChannelRoot> getChannels(int resId) {
        List<ChannelRoot> channelRoots = null;
        try {
            JSONObject json = new JSONObject(TestUtils.getStringFromFile(InstrumentationRegistry.getContext(), resId));
            String chanXml = json.getString("channels");
            ChannelHandler chanHandler = new ChannelHandler();
            channelRoots = chanHandler.parse(chanXml);
        } catch (SAXException e) {
            e.printStackTrace();
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return channelRoots;
    }

    @Nullable
    private List<ChannelGroup> getFavs(int resId) {
        List<ChannelGroup> channelGroups = null;
        try {
            JSONObject json = new JSONObject(TestUtils.getStringFromFile(InstrumentationRegistry.getContext(), resId));
            String favXml = json.getString("favourites");
            FavouriteHandler favHandler = new FavouriteHandler();
            channelGroups = favHandler.parse(InstrumentationRegistry.getContext(), favXml);
        } catch (SAXException e) {
            e.printStackTrace();
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return channelGroups;
    }

}
