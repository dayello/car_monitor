package org.carm.commons.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;

/**
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
public class DateUtils {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter yyMMddHHmmss = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    public static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final TemporalQuery<LocalDate> dateQuery = TemporalQueries.localDate();

    private static final TemporalQuery<LocalTime> timeQuery = TemporalQueries.localTime();

    public static long currentTimeSecond() {
        return System.currentTimeMillis() / 1000L;
    }

    public static long toEpochMilli(LocalDateTime dateTime) {
        Instant instant = dateTime.toInstant(getZoneOffset(dateTime));
        return instant.toEpochMilli();
    }

    public static long toEpochSecond(LocalDateTime dateTime) {
        return dateTime.toEpochSecond(getZoneOffset(dateTime));
    }

    public static ZoneOffset getZoneOffset(LocalDateTime dateTime) {
        return ZoneId.systemDefault().getRules().getOffset(dateTime);
    }

    public static LocalDateTime getDateTime(Long millis) {
        return getDateTime(Instant.ofEpochMilli(millis));
    }

    public static LocalDateTime getDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static LocalDateTime parse(String str) {
        return parse(str, yyMMddHHmmss);
    }

    public static LocalDateTime parse(String str, DateTimeFormatter df) {
        try {
            TemporalAccessor temporal = df.parse(str);
            LocalDate date = temporal.query(dateQuery);
            LocalTime time = temporal.query(timeQuery);
            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            return null;
        }
    }
}