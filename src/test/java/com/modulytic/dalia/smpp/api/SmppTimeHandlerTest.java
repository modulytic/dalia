package com.modulytic.dalia.smpp.api;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SmppTimeHandlerTest {

    @Test
    void formatAbsolute() {
        LocalDateTime dt = LocalDateTime.of(2001, 6, 27, 13, 24, 4, 900000000);
        String absoluteStamp = SmppTimeHandler.formatAbsolute(dt, ZoneId.of("America/Los_Angeles"));
        assertEquals("010627132404928-", absoluteStamp);
    }

    @Test
    void formatRelativeLocal() {
        LocalDateTime dt = LocalDateTime.now();
        dt = dt.minusYears(6);
        dt = dt.minusMonths(5);
        dt = dt.minusDays(4);
        dt = dt.minusHours(3);
        dt = dt.minusMinutes(2);
        dt = dt.minusSeconds(1);

        String relativeStamp = SmppTimeHandler.formatRelative(dt);
        assertEquals("060504030201000R", relativeStamp);
    }

    @Test
    void formatRelativeZoned() {
        ZonedDateTime dt = ZonedDateTime.now();
        dt = dt.minusYears(6);
        dt = dt.minusMonths(5);
        dt = dt.minusDays(4);
        dt = dt.minusHours(3);
        dt = dt.minusMinutes(2);
        dt = dt.minusSeconds(1);

        String relativeStamp = SmppTimeHandler.formatRelative(dt);
        assertEquals("060504030201000R", relativeStamp);
    }
}