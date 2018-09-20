package ru.vachok.money.services;


import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * @since 20.09.2018 (11:09)
 */
@Service("timeworks")
public class TimeWorks {

    public String timeLeft() {
        String timeLeftString;
        LocalTime parse = LocalTime.parse("17:30");
        LocalTime now = LocalTime.now();
        long until = now.until(parse, MINUTES);
        timeLeftString = "<b>"+(float) until / 60f + "</b> hrs left. Day of week now: <b>"+ LocalDate.now().getDayOfWeek()+"</b>";
        String dayOfWeekNow = LocalDate.now().getDayOfWeek().toString();
        if(dayOfWeekNow.equalsIgnoreCase("SUNDAY")||dayOfWeekNow.equalsIgnoreCase("SATURDAY"))
            timeLeftString = dayOfWeekNow;
        return timeLeftString;
    }

}
