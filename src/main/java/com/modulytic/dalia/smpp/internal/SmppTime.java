package com.modulytic.dalia.smpp.internal;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class SmppTime {
    private SmppTime() {}

    /**
     * Format {@link LocalDateTime} as SMPP absolute timestamp
     * @param dt    LocalDateTime
     * @return      String as SMPP absolute timestamp
     */
    public static String formatAbsolute(LocalDateTime dt) {
        return formatAbsolute(dt, ZoneId.systemDefault());
    }

    public static String formatAbsolute(LocalDateTime dt, ZoneId zone) {
        return formatAbsolute(dt.atZone(zone));
    }

    /**
     * Format {@link ZonedDateTime} as SMPP absolute timestamp
     * @param zdt   ZonedDateTime
     * @return      String as SMPP absolute timestamp
     */
    public static String formatAbsolute(ZonedDateTime zdt) {
        StringBuilder sb = new StringBuilder();

        // year through tenth seconds
        String fractionSecs = zdt.format(DateTimeFormatter.ofPattern("S"));
        sb.append(zdt.format(DateTimeFormatter.ofPattern("uuMMddHHmmss")))
                .append(fractionSecs.charAt(0));

        // offset from UTC in quarter hours
        ZoneOffset offset = zdt.getOffset();
        int smppOffset    = offset.getTotalSeconds() / 900;     // number of seconds in 15 minutes
        sb.append(String.format("%02d", Math.abs(smppOffset)))
                .append((smppOffset < 0) ? '-' : '+');

        return sb.toString();
    }

    /**
     * Format {@link LocalDateTime} as SMPP relative timestamp
     * @param dt    LocalDateTime
     * @return      String as SMPP relative timestamp
     */
    public static String formatRelative(LocalDateTime dt) {
        LocalDateTime now = LocalDateTime.now();

        long years = ChronoUnit.YEARS.between(dt, now);
        now = now.minusYears(years);

        long months = ChronoUnit.MONTHS.between(dt, now);
        now = now.minusMonths(months);

        long days = ChronoUnit.DAYS.between(dt, now);
        now = now.minusDays(days);

        long hours = ChronoUnit.HOURS.between(dt, now);
        now = now.minusHours(hours);

        long minutes = ChronoUnit.MINUTES.between(dt, now);
        now = now.minusMinutes(minutes);

        long seconds = ChronoUnit.SECONDS.between(dt, now);

        return String.format("%02d%02d%02d%02d%02d%02d000R", years, months, days, hours, minutes, seconds);
    }

    /**
     * Format {@link ZonedDateTime} as SMPP relative timestamp
     * @param zdt   ZonedDateTime
     * @return      String as SMPP relative timestamp
     */
    public static String formatRelative(ZonedDateTime zdt) {
        return formatRelative(zdt.toLocalDateTime());
    }
}
