// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;


/**
 @since 19.05.2019 (23:04) */
public abstract class Stats implements InformationFactory {
    
    
    protected String sql;
    
    protected String fileName;
    
    protected long start;
    
    public static @NotNull PCStats getPCStats() {
        PCStats pcStats = new PCStats();
        pcStats.sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;
        return pcStats;
    }
    
    public static @NotNull WeeklyInternetStats getInetStats() {
        WeeklyInternetStats weeklyInternetStats = new WeeklyInternetStats();
        weeklyInternetStats.run();
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
        sb.append('}');
        return sb.toString();
    }
}
