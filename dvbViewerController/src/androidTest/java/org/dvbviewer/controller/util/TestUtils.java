package org.dvbviewer.controller.util;

import android.content.Context;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

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
}
