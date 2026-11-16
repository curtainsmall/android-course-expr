package com.curtainsmall.expr2.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类。
 * <p>
 * minSdk 为 24，未开启 core library desugaring，因此全部使用 {@link Calendar} 与
 * {@link SimpleDateFormat}，不使用 java.time。
 * <p>
 * 这里的 SimpleDateFormat 只在主线程使用，故未做同步处理。
 */
public final class TimeUtils {

    private static final Locale LOCALE = Locale.CHINA;

    private static final SimpleDateFormat DATE_TIME =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", LOCALE);
    private static final SimpleDateFormat DATE =
            new SimpleDateFormat("yyyy-MM-dd", LOCALE);
    private static final SimpleDateFormat TIME =
            new SimpleDateFormat("HH:mm", LOCALE);
    private static final SimpleDateFormat DAY_LABEL =
            new SimpleDateFormat("yyyy年M月d日 EEEE", LOCALE);

    private TimeUtils() {
    }

    /** 2026-11-16 14:30 */
    public static String formatDateTime(long millis) {
        return DATE_TIME.format(new Date(millis));
    }

    /** 2026-11-16 */
    public static String formatDate(long millis) {
        return DATE.format(new Date(millis));
    }

    /** 14:30 */
    public static String formatTime(long millis) {
        return TIME.format(new Date(millis));
    }

    /** 2026年11月16日 星期一 */
    public static String formatDayLabel(long millis) {
        return DAY_LABEL.format(new Date(millis));
    }

    public static Calendar calendarOf(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c;
    }

    /** 当天 00:00:00.000 */
    public static long startOfDay(long millis) {
        Calendar c = calendarOf(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /** 次日 00:00:00.000，用作 [start, end) 区间的开区间上界 */
    public static long nextDayStart(long millis) {
        Calendar c = calendarOf(startOfDay(millis));
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }

    /** 把时间戳折算成 yyyyMMdd 形式的整数，便于按天匹配 */
    public static int dayKey(long millis) {
        Calendar c = calendarOf(millis);
        return dayKey(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    /** month 为 0 基（与 Calendar 一致） */
    public static int dayKey(int year, int month, int dayOfMonth) {
        return year * 10000 + (month + 1) * 100 + dayOfMonth;
    }

    public static long millisOf(int year, int month, int dayOfMonth, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth, hour, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
