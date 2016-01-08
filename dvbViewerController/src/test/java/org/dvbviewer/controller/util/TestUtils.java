package org.dvbviewer.controller.util;

import android.content.Context;
import android.test.AndroidTestCase;

import org.junit.Test;

import java.io.InputStream;

/**
 * Created by rbaun on 07.01.16.
 */
public class TestUtils extends AndroidTestCase{

    @Test
    public static String getStringFromFile(Context context, int resId){
        String result = null;
        InputStream is = context.getResources().openRawResource(resId);
        return result;
    }
}
