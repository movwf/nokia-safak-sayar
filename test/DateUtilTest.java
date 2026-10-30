package com.safaksayar;

import java.time.LocalDate;

public final class DateUtilTest {

    private static int checks;

    public static void main(String[] args) {
        check(DateUtil.daysFromCivil(1970, 1, 1) == 0, "epoch");
        check(DateUtil.weekday(0) == 3, "1970-01-01 was Thursday");
        check(DateUtil.daysFromCivil(2000, 1, 1) == 10957, "2000-01-01");
        check(DateUtil.weekday(10957) == 5, "2000-01-01 was Saturday");
        check(DateUtil.daysFromCivil(2024, 2, 29) - DateUtil.daysFromCivil(2024, 2, 28) == 1,
                "leap day");
        check(DateUtil.daysInMonth(2023, 2) == 28, "non-leap February");
        check(DateUtil.daysInMonth(2024, 2) == 29, "leap February");
        check(DateUtil.daysInMonth(2024, 4) == 30, "April");

        int[] ymd = DateUtil.civilFromDays(DateUtil.daysFromCivil(2027, 10, 15));
        check(ymd[0] == 2027 && ymd[1] == 10 && ymd[2] == 15, "round trip");

        long a = DateUtil.daysFromCivil(2026, 10, 29);
        long b = DateUtil.daysFromCivil(2027, 10, 29);
        check(b - a == 365, "one year countdown");

        check("29.10.2026".equals(DateUtil.formatShort(2026, 10, 29)), "short format");
        check("29 Ekim 2026 PERŞEMBE".equals(DateUtil.formatLong(2026, 10, 29)),
                "long format: " + DateUtil.formatLong(2026, 10, 29));

        LocalDate d = LocalDate.of(1970, 1, 1);
        LocalDate end = LocalDate.of(2100, 12, 31);
        while (!d.isAfter(end)) {
            long expected = d.toEpochDay();
            long actual = DateUtil.daysFromCivil(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
            if (expected != actual) {
                throw new AssertionError("daysFromCivil mismatch at " + d
                        + ": " + actual + " != " + expected);
            }
            int[] back = DateUtil.civilFromDays(actual);
            if (back[0] != d.getYear() || back[1] != d.getMonthValue()
                    || back[2] != d.getDayOfMonth()) {
                throw new AssertionError("civilFromDays mismatch at " + d);
            }
            int weekday = DateUtil.weekday(actual);
            if (weekday != d.getDayOfWeek().getValue() - 1) {
                throw new AssertionError("weekday mismatch at " + d);
            }
            d = d.plusDays(1);
        }
        checks++;
        System.out.println("All " + checks + " date checks passed.");
    }

    private static void check(boolean condition, String name) {
        checks++;
        if (!condition) {
            throw new AssertionError("FAILED: " + name);
        }
    }
}
