package org.dvbviewer.controller.io;


import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RecordingServiceTest {

    private final String expectedVersion    = "1.32.0.0";
    private final String unExpectedVersion  = "1.32.0.0.99";
    private final String version1           = "DVBViewer Recording Service "+expectedVersion+" (beta) (HOMESERVER)";
    private final String version2           = "DVBViewer Recording Service"+expectedVersion+"(beta)(HOMESERVER)";
    private final String version3           = "会意字 / 會意字 huìyìzì "+expectedVersion+" 会意字 / 會意字 huìyìzì";
    private final String version4           = "会意字 / 會意字 huìyìzì"+expectedVersion+"会意字 / 會意字 huìyìzì";
    private final String version5           = "会意字 / 會意字 huìyìzì"+expectedVersion+"会意字1.99.0.0 / 會意字 huìyìzì";
    private final String version6           = "DVBViewer Recording Service "+unExpectedVersion+" (beta)(HOMESERVER)";

    @Test
    public void extractVersion1() {
        Matcher matcher = RecordingService.getVersionMatcher(version1);
        assertEquals(true, matcher.find());
        assertEquals(expectedVersion, RecordingService.getVersionFromMatcher(matcher));
    }

    @Test
    public void extractVersion2() {
        Matcher matcher = RecordingService.getVersionMatcher(version2);
        assertEquals(true, matcher.find());
        assertEquals(expectedVersion, RecordingService.getVersionFromMatcher(matcher));
    }

    @Test
    public void extractVersion3() {
        Matcher matcher = RecordingService.getVersionMatcher(version3);
        assertEquals(true, matcher.find());
        assertEquals(expectedVersion, RecordingService.getVersionFromMatcher(matcher));
    }

    @Test
    public void extractVersion4() {
        Matcher matcher = RecordingService.getVersionMatcher(version4);
        assertEquals(true, matcher.find());
        assertEquals(expectedVersion, RecordingService.getVersionFromMatcher(matcher));
    }

    @Test
    public void extractVersion5() {
        Matcher matcher = RecordingService.getVersionMatcher(version5);
        assertEquals(true, matcher.find());
        assertEquals(expectedVersion, RecordingService.getVersionFromMatcher(matcher));
    }

    @Test
    public void extractVersion6() {
        Matcher matcher = RecordingService.getVersionMatcher(version6);
        assertEquals(true, matcher.find());
        assertNotEquals(expectedVersion, RecordingService.getVersionFromMatcher(matcher));
    }


}
