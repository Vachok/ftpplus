// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;


/**
 @since 19.05.2019 (23:04) */
public abstract class Stats implements InformationFactory {
    
    
    protected String sql;
    
    protected String fileName;
    
    protected long start;
    
    private static DayOfWeek currentDayOfWeek = LocalDate.now().getDayOfWeek();
    
    @Contract(pure = true)
    public static DayOfWeek getCurrentDayOfWeek() {
        return currentDayOfWeek;
    }
    
    public static @NotNull PCStats getPCStats() {
        PCStats pcStats = new PCStats();
        pcStats.sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;
        return pcStats;
    }
    
    public static @NotNull Stats getInetStats() {
        WeeklyInternetStats weeklyInternetStats = new WeeklyInternetStats();
        if (currentDayOfWeek.equals(DayOfWeek.SUNDAY)) {
            weeklyInternetStats.run();
        }
        return weeklyInternetStats;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.fileName = aboutWhat;
        return toString();
    }
    
    @Override
    public void setClassOption(@NotNull Object classOption) {
        this.sql = classOption.toString();
    }
    
    @Override
    public abstract String getInfo();
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Stats{");
        sb.append("sql='").append(sql).append('\'');
        sb.append(", fileName='").append(fileName).append('\'');
        sb.append(", start=").append(start);
        sb.append(", currentDayOfWeek=").append(currentDayOfWeek);
        sb.append('}');
        return sb.toString();
    }
}
