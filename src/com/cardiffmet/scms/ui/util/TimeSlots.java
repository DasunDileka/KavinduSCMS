package com.cardiffmet.scms.ui.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class TimeSlots {
    private TimeSlots() {
    }

    /** Hourly slots from 08:00 to 20:00 inclusive. */
    public static List<LocalTime> hourlySlots() {
        List<LocalTime> t = new ArrayList<>();
        for (int h = 8; h <= 20; h++) {
            t.add(LocalTime.of(h, 0));
        }
        return t;
    }
}
