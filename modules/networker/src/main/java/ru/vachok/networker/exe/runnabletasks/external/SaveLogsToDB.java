// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 @since 06.06.2019 (13:40) */
public class SaveLogsToDB implements Runnable {
    
    protected static final String CLEANED = "Cleaned: ";
    
    private static final ru.vachok.stats.SaveLogsToDB LOGS_TO_DB_EXT = new ru.vachok.stats.SaveLogsToDB();
    
    @Contract(pure = true)
    public static ru.vachok.stats.SaveLogsToDB getI() {
        return LOGS_TO_DB_EXT;
    }
    
    public static void startScheduled() {
        LOGS_TO_DB_EXT.startScheduled();
    }
    
    public static @NotNull String showInfo() {
        LOGS_TO_DB_EXT.showInfo();
        return "LOGS_TO_DB_EXT.showInfo();";
    }
    
    @Override
    public void run() {
        LOGS_TO_DB_EXT.startScheduled();
        showInfo();
    }
}
