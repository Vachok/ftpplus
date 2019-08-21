// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.info.InformationFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;


/**
 @since 19.05.2019 (23:04) */
public interface Stats extends InformationFactory {
    
    
    @Contract(value = " -> new", pure = true)
    static @NotNull Stats getPCStats() {
        return new ComputerUserResolvedStats();
    }
    
    static @NotNull Stats getInetStats() {
        WeeklyInternetStats weeklyInternetStats = new WeeklyInternetStats();
        if (isSunday()) {
            weeklyInternetStats.run();
        }
        return weeklyInternetStats;
    }
    
    static boolean isSunday() {
        return LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }
    
    @Contract(" -> new")
    static @NotNull InformationFactory getLogStats() {
        return new SaveLogsToDB();
    }
    
    @Override
    default String getInfoAbout(String aboutWhat) {
        setClassOption(aboutWhat);
        return getInfo();
    }
    
    @Override
    void setClassOption(@NotNull Object classOption);
}
