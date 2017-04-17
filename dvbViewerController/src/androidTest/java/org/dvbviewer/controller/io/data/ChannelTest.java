package org.dvbviewer.controller.io.data;


import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.dvbviewer.controller.entities.ChannelRoot;
import org.dvbviewer.controller.util.TestUtils;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ChannelTest {

    private static final String TAG = ChannelTest.class.getSimpleName();

    @Test
    public void parseChannels() {
        int successCount = 0;
        for (int i = 1; i <= 911; i++){
            try {
                List<ChannelRoot> tmp;
                final String resName = "user_"+i;
                int resId = TestUtils.getResourceId(InstrumentationRegistry.getContext(), resName, "raw");
                Log.d(TAG, "parsing File " + resName);
                tmp = TestUtils.getChannels(resId);
                assertEquals(true, tmp != null);
                Log.d(TAG, "parsed " + (tmp != null ? tmp.size() : "0") + " channel groups");
                successCount++;
            }catch (IOException| JSONException | SAXException e){
                e.printStackTrace();
            }
        }
        assertTrue(successCount > 0);
    }


}
