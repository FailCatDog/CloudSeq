package cn.guet.soft_manage.biz.utils;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

/**
 * ISO 8601 周次工具
 */
public final class IsoWeekUtil {

    private static final WeekFields ISO = WeekFields.ISO;

    private IsoWeekUtil() {
    }

    public record IsoWeek(int year, int week, LocalDate weekStartDate) {
    }

    public static IsoWeek of(LocalDate date) {
        LocalDate normalized = date == null ? LocalDate.now() : date;
        int year = normalized.get(ISO.weekBasedYear());
        int week = normalized.get(ISO.weekOfWeekBasedYear());
        LocalDate monday = normalized.with(ISO.dayOfWeek(), 1);
        return new IsoWeek(year, week, monday);
    }

    public static IsoWeek of(int year, int week) {
        LocalDate date = LocalDate.of(year, 1, 4)
                .with(ISO.weekBasedYear(), year)
                .with(ISO.weekOfWeekBasedYear(), week)
                .with(ISO.dayOfWeek(), 1);
        return of(date);
    }

    public static IsoWeek current() {
        return of(LocalDate.now());
    }

    public static IsoWeek shift(IsoWeek isoWeek, int weeks) {
        return of(isoWeek.weekStartDate().plusWeeks(weeks));
    }
}
