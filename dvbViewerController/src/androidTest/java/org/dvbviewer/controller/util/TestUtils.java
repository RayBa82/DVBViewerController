package org.dvbviewer.controller.util;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.io.data.ChannelHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by rbaun on 07.01.16.
 */
public class TestUtils {

    public static String getStringFromFile(Context context, int resId){
        String result = null;
        InputStream is = context.getResources().openRawResource(resId);
        try {
            result = IOUtils.toString(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtils.closeQuietly(is);
        }
        return result;
    }

    public static int getResourceId(Context context, String pVariableName, String pResourcename)
    {
        try {
            return context.getResources().getIdentifier(pVariableName, pResourcename, context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Nullable
    public static List<ChannelRoot> getChannels(int resId) throws JSONException, SAXException, IOException {
        JSONObject json = new JSONObject(TestUtils.getStringFromFile(InstrumentationRegistry.getContext(), resId));
        String chanXml = json.getString("channels");
        ChannelHandler chanHandler = new ChannelHandler();
        List<ChannelRoot> channelRoots = chanHandler.parse(IOUtils.toInputStream(chanXml), false);
        return channelRoots;
    }

}
