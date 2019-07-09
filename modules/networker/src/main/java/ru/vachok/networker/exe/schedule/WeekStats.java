// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.services.DBMessenger;
import ru.vachok.networker.statistics.InternetStats;
import ru.vachok.networker.statistics.PCStats;
import ru.vachok.networker.statistics.StatsOfNetAndUsers;
import ru.vachok.networker.systray.MessageToTray;

import java.time.LocalDate;
import java.util.List;


/**
 Сбор статы по-недельно
 <p>
 Устойчивость (in/(in+out)): 2/(2+6) = 0.25 (устойчив на 75%);
 
 @see InternetStats
 @see PCStats
 @since 08.12.2018 (0:12) */
public class WeekStats implements Runnable, StatsOfNetAndUsers {
    
    /**
     Лист только с именами ПК
     */
    private static final List<String> PC_NAMES_IN_TABLE = PCStats.getPcNamesInTable();
    
    private static MessageToUser messageToUser;

    private String sql;

    private String fileName;
    
    public WeekStats(String sql, String fileName) {
        this.sql = sql;
        this.fileName = fileName;
    }
    
    
    public WeekStats(String sql) {
        this.sql = sql;
        this.fileName = ConstantsFor.FILENAME_VELKOMPCUSERAUTOTXT;
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("WeekStats{");
        sb.append(LocalDate.now().getDayOfWeek());
        sb.append('}');
        return sb.toString();
    }
    
    public WeekStats() {
        this.sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;
    }
    
    static {
        try {
            messageToUser = new MessageToTray(WeekStats.class.getSimpleName());
        }
        catch (UnsupportedOperationException e) {
            messageToUser = new DBMessenger(WeekStats.class.getSimpleName());
        }
    }
    
    
    @Override public String getPCStats() {
        DataBaseRegSQL dataBaseRegSQL = new PCStats();
        int selectFrom = dataBaseRegSQL.selectFrom();
        String retStr = "total pc: " + selectFrom;
        messageToUser.info(getClass().getSimpleName(), "pc stats: ", retStr);
        return retStr;
    }
    
    @Override public String getInetStats() {
        InternetStats dataBaseRegSQL = new InternetStats();
        dataBaseRegSQL.run();
        return dataBaseRegSQL.toString();
    }
    
    @Override
    public void run() {
        final long stArt = System.currentTimeMillis();
        getPCStats();
        getInetStats();
    }
}
