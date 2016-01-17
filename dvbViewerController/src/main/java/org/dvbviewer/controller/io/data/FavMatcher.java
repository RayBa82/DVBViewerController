package org.dvbviewer.controller.io.data;

import android.support.annotation.Nullable;

import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rbaun on 08.01.16.
 */
public class FavMatcher {

    public List<ChannelGroup> matchFavs(List<ChannelRoot> channelRoots, @Nullable List<ChannelGroup> favList){
        List<ChannelGroup> result = new ArrayList<>();
        if (favList != null){
            for (ChannelGroup favGroup : favList){
                ChannelGroup tmp = getCurrentFavGroup(result, favGroup);
                for (Channel.Fav fav : favGroup.getFavs()){
                    Channel chan = getMatchedChannel(channelRoots, fav.id);
                    if (chan != null){
                        chan.setFavPosition(fav.position);
                        chan.setFlag(Channel.FLAG_FAV);
                        tmp.getChannels().add(chan);
                    }
                }
            }
        }
        return result;
    }

    @Nullable
    private Channel getMatchedChannel(@Nullable List<ChannelRoot> channelRoots, long favId) {
        if (channelRoots != null){
            for(ChannelRoot roots : channelRoots){
                for( ChannelGroup group : roots.getGroups()){
                    for(Channel chan : group.getChannels()){
                        if (chan.getChannelID() == favId){
                            return chan;
                        }
                    }
                }
            }
        }
        return null;
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
