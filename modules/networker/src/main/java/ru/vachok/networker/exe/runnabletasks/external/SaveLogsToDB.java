// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks.external;


import org.jetbrains.annotations.NotNull;


/**
 @since 06.06.2019 (13:40) */
public abstract class SaveLogsToDB {
    
    
    private static final ru.vachok.stats.SaveLogsToDB LOGS_TO_DB_EXT = new ru.vachok.stats.SaveLogsToDB();
    
    protected static final String CLEANED = "Cleaned: ";
    
    public static ru.vachok.stats.SaveLogsToDB getI() {
        startScheduled();
        return LOGS_TO_DB_EXT;
    }
    
    public static @NotNull String showInfo() {
        LOGS_TO_DB_EXT.showInfo();
        return startScheduled();
    }
    
    private static @NotNull String startScheduled() {
        return LOGS_TO_DB_EXT.startScheduled();
    }
}
