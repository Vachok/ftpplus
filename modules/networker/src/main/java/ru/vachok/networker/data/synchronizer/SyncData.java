package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.data.enums.ConstantsFor;


public interface SyncData {
    
    
    @Contract(value = " -> new", pure = true)
    static @NotNull SyncData getInstance() {
        return new SyncWithRegRu();
    }
    
    @Contract(pure = true)
    static void syncDB(String dbToSync) {
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            new DBDownloader(ConstantsFor.DBBASENAME_U0466446_VELKOM).writeLocalDBFromFile();
        }
        else {
            System.err.println(UsefulUtilities.thisPC() + " is not synced DB!");
        }
    }
    
    String syncData();
    
    void setOption(Object option);
}
