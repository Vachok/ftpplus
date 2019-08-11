// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.statistics.InternetStats;
import ru.vachok.networker.statistics.PCStats;
import ru.vachok.networker.statistics.Stats;

import java.time.LocalDate;


/**
 Сбор статы по-недельно
 <p>
 Устойчивость (in/(in+out)): 2/(2+6) = 0.25 (устойчив на 75%);
 
 @see InternetStats
 @see PCStats
 @since 08.12.2018 (0:12) */
public class WeekStats implements Stats {
    
    
    private String sql;
    
    private String fileName;
    
    private long start;
    
    @Contract(pure = true)
    public WeekStats(String sql, String fileName) {
        this.sql = sql;
        this.fileName = fileName;
    }
    
    @Contract(pure = true)
    public WeekStats(String sql) {
        this.sql = sql;
        this.fileName = FileNames.FILENAME_VELKOMPCUSERAUTOTXT;
    }
    
    @Contract(pure = true)
    public WeekStats() {
        this.sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WeekStats{");
        sb.append(LocalDate.now().getDayOfWeek());
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String getPCStats() {
        return new PCStats().call();
    }
    
    @Override
    public String getInetStats() {
        InternetStats internetStats = new InternetStats();
        internetStats.run();
        return internetStats.toString();
    }
}
