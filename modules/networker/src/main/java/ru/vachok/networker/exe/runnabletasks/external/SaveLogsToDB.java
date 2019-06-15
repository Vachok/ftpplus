package ru.vachok.networker.exe.runnabletasks.external;


import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;


/**
 @since 06.06.2019 (13:40) */
public class SaveLogsToDB implements Runnable {
    
    
    private static final ru.vachok.stats.SaveLogsToDB LOGS_TO_DB_EXT = ru.vachok.stats.SaveLogsToDB.getI(new RegRuMysql().getDataSourceSchema(ConstantsFor.DBBASENAME_U0466446_VELKOM));
    
    public SaveLogsToDB getI() {
        return this;
    }
    
    public static void startScheduled() {
        LOGS_TO_DB_EXT.startScheduled();
    }
    
    public static String showInfo() {
        LOGS_TO_DB_EXT.showInfo();
        return "LOGS_TO_DB_EXT.showInfo();";
    }
    
    @Override public void run() {
        LOGS_TO_DB_EXT.startScheduled();
    }
}
