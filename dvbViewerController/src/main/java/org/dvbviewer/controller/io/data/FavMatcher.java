package org.dvbviewer.controller.io.data;

import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rbaun on 08.01.16.
 */
public class FavMatcher {

    public List<ChannelGroup> matchFavs(List<ChannelRoot> channelRoots, List<ChannelGroup> favList){
        List<ChannelGroup> result = new ArrayList<>();
        for (ChannelGroup favGroup : favList){
            for (Channel.Fav fav : favGroup.getFavs()){
                for(ChannelRoot roots : channelRoots){
                    for( ChannelGroup group : roots.getGroups()){
                        ChannelGroup tmp = getCurrentFavGroup(result, group);
                        for(Channel chan : group.getChannels()){
                            if (chan.getChannelID() == fav.id && !tmp.getChannels().contains(chan)){
                                chan.setFavPosition(fav.position);
                                chan.setFlag(Channel.FLAG_FAV);
                                tmp.getChannels().add(chan);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private ChannelGroup getCurrentFavGroup(List<ChannelGroup> result, ChannelGroup group) {
        int groupIndex = result.indexOf(group);
        ChannelGroup tmp;
        if (groupIndex >= 0){
            tmp = result.get(groupIndex);
        }else{
            tmp = new ChannelGroup();
            tmp.setName(group.getName());
            tmp.setType(ChannelGroup.TYPE_FAV);
            result.add(tmp);
        }
        return tmp;
    }

}
