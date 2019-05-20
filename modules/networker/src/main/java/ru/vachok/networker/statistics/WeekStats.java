// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;

import java.awt.*;
import java.util.List;


/**
 Сбор статы по-недельно
 <p>
 Устойчивость (in/(in+out)): 2/(2+6) = 0.25 (устойчив на 75%);
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
    
    public WeekStats() {
        this.sql = ConstantsFor.SQL_SELECTFROM_PCUSERAUTO;
    }
    
    static {
        try {
            messageToUser = new MessageToTray(WeekStats.class.getSimpleName());
        }
        catch (UnsupportedOperationException e) {
            messageToUser = new MessageLocal(WeekStats.class.getSimpleName());
        }
    }
    
    
    @Override public String getPCStats() {
        DataBaseRegSQL dataBaseRegSQL = new PCStats();
        this.fileName = ConstantsFor.FILENAME_VELKOMPCUSERAUTOTXT;
        int selectFrom = dataBaseRegSQL.selectFrom();
        return new TForms().fromArray(PC_NAMES_IN_TABLE, false);
    }
    
    @Override public String getInetStats() {
        throw new IllegalComponentStateException("20.05.2019 (9:45)");
    }
    
    @Override
    public void run() {
        AppComponents.threadConfig().thrNameSet("week");
        final long stArt = System.currentTimeMillis();
    }
}
