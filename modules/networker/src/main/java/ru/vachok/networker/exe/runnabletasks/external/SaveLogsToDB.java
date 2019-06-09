package ru.vachok.networker.exe.runnabletasks.external;


/**
 @since 06.06.2019 (13:40) */
public class SaveLogsToDB implements Runnable {
    
    
    private static final ru.vachok.stats.SaveLogsToDB LOGS_TO_DB_EXT = new ru.vachok.stats.SaveLogsToDB();
    
    public static SaveLogsToDB getI() {
        return new SaveLogsToDB();
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
