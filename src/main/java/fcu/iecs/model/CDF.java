package fcu.iecs.model;

import java.time.LocalDate;

// CDF stands for CustomDateFormat
public class CDF {
    private static final String[] WEEKDAYS = {"一", "二", "三", "四", "五", "六", "日"};

    public static String of(LocalDate date) {
        // for ex: 1991/07/31(三)
        return "%d/%02d/%02d(%s)".formatted(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                WEEKDAYS[date.getDayOfWeek().getValue() - 1]
        );
    }

}
