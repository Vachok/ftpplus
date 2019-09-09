package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.data.enums.ConstantsFor;


public interface SyncData {
    
    
    @Contract(pure = true)
    static void syncDB(String dbToSync) {
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            new DBSyncronizer(ConstantsFor.DBBASENAME_U0466446_VELKOM).writeLocalDBFromFile(dbToSync);
        }
        else {
            System.err.println(UsefulUtilities.thisPC() + " is not synced DB!");
        }
    }
    
    String syncData();
}
