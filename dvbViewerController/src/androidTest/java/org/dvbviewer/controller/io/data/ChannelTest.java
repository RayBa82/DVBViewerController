package org.dvbviewer.controller.io.data;


import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.util.Log;

import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.util.TestUtils;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ChannelTest extends InstrumentationTestCase {

    private static final String TAG = ChannelTest.class.getSimpleName();

    @Test
    public void parseChannels() throws JSONException, SAXException {
        List<ChannelRoot> tmp = null;
        int successCount = 0;
        for (int i = 1; i <= 911; i++){
            try {
                final String resName = "user_"+i;
                int resId = TestUtils.getResourceId(InstrumentationRegistry.getContext(), resName, "raw");
                Log.d(TAG, "parsing File " + resName);
                tmp = TestUtils.getChannels(resId);
                Log.d(TAG, "parsed " + tmp.size() + " channel groups");
                assertEquals(true, tmp != null);
                successCount++;
            }catch (JSONException | SAXException e){
                e.printStackTrace();
                assertEquals(true, tmp == null || tmp.size() == 0);
            }finally {
                tmp = null;
            }
        }
        assertTrue(successCount > 0);
    }


}
