// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;


/**
 @see StatsTest
 @since 19.05.2019 (23:04) */
public abstract class Stats implements InformationFactory {
    
    
    @Contract("_ -> new")
    public static @NotNull Stats getInstance(@NotNull String type) {
        switch (type) {
            case InformationFactory.STATS_REGULAR_LOGS_SAVER:
                return new SaveLogsToDB();
            case InformationFactory.STATS_WEEKLY_INTERNET:
                return new WeeklyInternetStats();
            case InformationFactory.STATS_SUDNAY_PC_SORT:
                return new ComputerUserResolvedStats();
            default:
                throw new InvokeIllegalException(MessageFormat.format("NOT CORRECT INSTANCE: {0} in {1}", type, Stats.class.getSimpleName()));
        }
    }
    
    public static boolean isSunday() {
        return LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract String getInfo();
    
    @Override
    public abstract void setClassOption(Object classOption);
}