package org.dvbviewer.controller.data.api.handler;

import org.apache.commons.lang3.StringUtils;
import org.dvbviewer.controller.data.entities.FFMpegPresetList;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rayba on 17.04.17.
 */

public class FFMPEGPrefsHandlerTest {

    private FFMPEGPrefsHandler handler = new FFMPEGPrefsHandler();

    @Test
    public void testNullString() {
        FFMpegPresetList result = null;
        try {
            result = handler.parse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull("result is null", result);
        assertNotNull("presets are null", result.getPresets());
    }

    @Test
    public void testEmptyString() {
        FFMpegPresetList result = null;
        try {
            result = handler.parse(StringUtils.EMPTY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        assertNotNull(result.getPresets());
    }

}
