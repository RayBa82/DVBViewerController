package org.dvbviewer.controller.io.data;

import org.apache.commons.lang3.StringUtils;
import org.dvbviewer.controller.entities.FfMpegPrefs;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rayba on 17.04.17.
 */

public class FFMPEGPrefsHandlerTest {

    private FFMPEGPrefsHandler handler = new FFMPEGPrefsHandler();

    @Test
    public void testNullString() {
        FfMpegPrefs result = handler.parse(null);
        assertNotNull(result);
        assertNotNull(result.getPresets());
    }

    @Test
    public void testEmptyString() {
        FfMpegPrefs result = handler.parse(StringUtils.EMPTY);
        assertNotNull(result);
        assertNotNull(result.getPresets());
    }

}
