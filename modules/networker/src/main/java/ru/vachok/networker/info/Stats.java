// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;

import java.time.DayOfWeek;
import java.time.LocalDate;


/**
 @since 19.05.2019 (23:04) */
public abstract class Stats implements InformationFactory {
    
    
    @Contract(value = " -> new", pure = true)
    public static @NotNull Stats getPCStats() {
        return new ComputerUserResolvedStats();
    }
    
    public static @NotNull Stats getInetStats() {
        WeeklyInternetStats weeklyInternetStats = new WeeklyInternetStats();
        if (isSunday()) {
            weeklyInternetStats.run();
        }
        return weeklyInternetStats;
    }
    
    public static boolean isSunday() {
        return LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }
    
    @Contract(" -> new")
    public static @NotNull InformationFactory getLogStats() {
        return new SaveLogsToDB();
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        setClassOption(aboutWhat);
        return getInfo();
    }
    
    @Override
    public abstract void setClassOption(Object classOption);
}
