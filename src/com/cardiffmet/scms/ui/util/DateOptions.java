package com.cardiffmet.scms.ui.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class DateOptions {
    private DateOptions() {
    }

    public static List<LocalDate> upcomingDays(int count) {
        LocalDate d = LocalDate.now();
        List<LocalDate> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(d.plusDays(i));
        }
        return list;
    }
}
