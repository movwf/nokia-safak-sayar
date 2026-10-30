package com.safaksayar;

import java.util.Calendar;

/**
 * Small, timezone-safe date helper.
 *
 * Dates are represented as "days since 1970-01-01" so that countdown math
 * never depends on daylight saving time or the device timezone.
 */
public final class DateUtil {

    public static final String[] MONTHS = {
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    };

    public static final String[] MONTHS_UPPER = {
        "OCAK", "ŞUBAT", "MART", "NİSAN", "MAYIS", "HAZİRAN",
        "TEMMUZ", "AĞUSTOS", "EYLÜL", "EKİM", "KASIM", "ARALIK"
    };

    public static final String[] DAYS_UPPER = {
        "PAZARTESİ", "SALI", "ÇARŞAMBA", "PERŞEMBE",
        "CUMA", "CUMARTESİ", "PAZAR"
    };

    /** Monday first. */
    public static final String[] DAY_INITIALS = {
        "P", "S", "Ç", "P", "C", "C", "P"
    };

    private DateUtil() {
    }

    /** Days since 1970-01-01 for a Gregorian date. */
    public static long daysFromCivil(int y, int m, int d) {
        y -= m <= 2 ? 1 : 0;
        long era = (y >= 0 ? y : y - 399) / 400;
        int yoe = (int) (y - era * 400);
        int doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
        int doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
        return era * 146097 + doe - 719468;
    }

    /** Returns {year, month (1-12), day}. */
    public static int[] civilFromDays(long days) {
        long z = days + 719468;
        long era = (z >= 0 ? z : z - 146096) / 146097;
        int doe = (int) (z - era * 146097);
        int yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;
        int y = (int) (yoe + era * 400);
        int doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
        int mp = (5 * doy + 2) / 153;
        int d = doy - (153 * mp + 2) / 5 + 1;
        int m = mp + (mp < 10 ? 3 : -9);
        y += m <= 2 ? 1 : 0;
        return new int[] { y, m, d };
    }

    /** Monday = 0 ... Sunday = 6. */
    public static int weekday(long days) {
        int w = (int) ((days + 3) % 7);
        if (w < 0) {
            w += 7;
        }
        return w;
    }

    public static boolean isLeapYear(int y) {
        return (y % 4 == 0) && ((y % 100 != 0) || (y % 400 == 0));
    }

    public static int daysInMonth(int y, int m) {
        if (m == 2) {
            return isLeapYear(y) ? 29 : 28;
        }
        if (m == 4 || m == 6 || m == 9 || m == 11) {
            return 30;
        }
        return 31;
    }

    /** Local date on the device, as days since 1970-01-01. */
    public static long today() {
        Calendar c = Calendar.getInstance();
        return daysFromCivil(
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH));
    }

    public static int[] todayYmd() {
        return civilFromDays(today());
    }

    public static String formatShort(int y, int m, int d) {
        StringBuffer sb = new StringBuffer();
        if (d < 10) {
            sb.append('0');
        }
        sb.append(d).append('.');
        if (m < 10) {
            sb.append('0');
        }
        sb.append(m).append('.').append(y);
        return sb.toString();
    }

    public static String formatShort(long days) {
        int[] ymd = civilFromDays(days);
        return formatShort(ymd[0], ymd[1], ymd[2]);
    }

    public static String formatLong(int y, int m, int d) {
        long days = daysFromCivil(y, m, d);
        return d + " " + MONTHS[m - 1] + " " + y + " " + DAYS_UPPER[weekday(days)];
    }

    public static String formatLong(long days) {
        int[] ymd = civilFromDays(days);
        return formatLong(ymd[0], ymd[1], ymd[2]);
    }

    public static String pad2(int n) {
        if (n < 10) {
            return "0" + n;
        }
        return String.valueOf(n);
    }
}
