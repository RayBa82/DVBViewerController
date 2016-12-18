/*
 * Copyright � 2013 dvbviewer-controller Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dvbviewer.controller.io.data;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.support.annotation.NonNull;
import android.util.Xml;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dvbviewer.controller.entities.Channel;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.ChannelRoot;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class ChannelHandler.
 *
 * @author RayBa
 */
public class ChannelHandler extends DefaultHandler {

    List<ChannelRoot> rootElements;
    ChannelRoot currentRoot;
    ChannelGroup currentGroup;
    Channel currentChannel = null;
    private int favPosition;
    private String favRootName;


    /**
     * Parses the.
     *
     * @param xml the xml
     * @return the list�
     * @throws org.xml.sax.SAXException
     */
    public List<ChannelRoot> parse(String xml, boolean fav) throws SAXException {
        Xml.parse(xml, getContentHandler(fav));
        return rootElements;

    }

    public List<ChannelRoot> parse(InputStream inputStream, boolean fav) throws SAXException, IOException {
        Xml.parse(inputStream, Xml.Encoding.UTF_8, getContentHandler(fav));
        return rootElements;
    }

    @NonNull
    private ContentHandler getContentHandler(final boolean fav) {
        favPosition = 1;
        RootElement channels = new RootElement("channels");
        Element rootElement = channels.getChild("root");
        Element groupElement = rootElement.getChild("group");
        Element channelElement = groupElement.getChild("channel");
        Element subChanElement = channelElement.getChild("subchannel");
        Element logoElement = channelElement.getChild("logo");

        channels.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                rootElements = new ArrayList<ChannelRoot>();
            }
        });

        rootElement.setStartElementListener(new StartElementListener() {
            public void start(Attributes attributes) {
                currentRoot = new ChannelRoot();
                currentRoot.setName(attributes.getValue("name"));
                favRootName = fav ? currentRoot.getName() : StringUtils.EMPTY;
                rootElements.add(currentRoot);
            }
        });

        groupElement.setStartElementListener(new StartElementListener() {
            public void start(Attributes attributes) {
                currentGroup = new ChannelGroup();
                currentGroup.setName(attributes.getValue("name").replaceFirst(favRootName, StringUtils.EMPTY));
                currentRoot.getGroups().add(currentGroup);
                currentGroup.setType(fav ? ChannelGroup.TYPE_FAV : ChannelGroup.TYPE_CHAN);
            }
        });

        channelElement.setStartElementListener(new StartElementListener() {
            public void start(Attributes attributes) {
                currentChannel = new Channel();
                currentChannel.setChannelID(NumberUtils.toLong(attributes.getValue("ID")));
                currentChannel.setPosition(fav ? favPosition : NumberUtils.toInt(attributes.getValue("nr")));
                currentChannel.setName(attributes.getValue("name"));
                currentChannel.setEpgID(NumberUtils.toLong(attributes.getValue("EPGID")));
                currentGroup.getChannels().add(currentChannel);
                favPosition++;
            }
        });

        logoElement.setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                currentChannel.setLogoUrl(body);
            }

        });

        subChanElement.setStartElementListener(new StartElementListener() {
            public void start(Attributes attributes) {
                Channel c = new Channel();
                c.setChannelID(NumberUtils.toLong(attributes.getValue("ID")));
                c.setPosition(currentChannel.getPosition());
                c.setName(attributes.getValue("name"));
                c.setEpgID(currentChannel.getEpgID());
                c.setLogoUrl(currentChannel.getLogoUrl());
                c.setFlag(Channel.FLAG_ADDITIONAL_AUDIO);
                currentGroup.getChannels().add(c);
            }
        });
        return channels.getContentHandler();
    }

}
