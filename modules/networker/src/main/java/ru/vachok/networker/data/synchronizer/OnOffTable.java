package ru.vachok.networker.data.synchronizer;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.net.scanner.PcNamesScanner;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentLinkedDeque;


/**
 @see OnOffTableTest
 @since 30.09.2019 (13:48) */
class OnOffTable extends SyncData {
    
    
    private static final String DB_SYNC = ConstantsFor.DB_ONOFF;
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", OnOffTable.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
    
    @Override
    String getDbToSync() {
        return this.DB_SYNC;
    }
    
    @Override
    public void setDbToSync(String dbToSync) {
        throw new UnsupportedOperationException("velkom.onoff is constant!");
    }
    
    @Override
    public void setOption(Object option) {
        throw new UnsupportedOperationException("velkom.onoff is constant!");
    }
    
    /**
     @return sync results
     
     @see OnOffTableTest#testSyncData()
     */
    @Override
    public String syncData() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        Deque<String> pcNames = new ConcurrentLinkedDeque<>(new PcNamesScanner().getCycleNames(AppComponents.getProps().getProperty(PropertiesNames.PREFIX)));
        StringBuilder stringBuilder = new StringBuilder();
        while (!pcNames.isEmpty()) {
            String pcName = pcNames.removeFirst();
            if (!new NameOrIPChecker(pcName).isLocalAddress()) {
                stringBuilder.append(MessageFormat.format("{0} is bad pc... ...ЭТО {1} ГОВОРИТ...", pcName, this.getClass().getSimpleName())).append("\n");
            }
            else {
                stringBuilder.append(infoGetIfExistsInDNS(pcName).replace("<br>", "\n"));
            }
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String infoGetIfExistsInDNS(String pcName) {
        PCInfo pcInfo = PCInfo.getInstance("PCOff");
        pcInfo.setClassOption(pcName);
        String info = pcInfo.getInfoAbout(pcName);
        return pcName + " " + info;
    }
    
    @Override
    public void superRun() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OnOffTable.superRun( void ) at 30.09.2019 - (13:48)");
    }
    
    @Override
    public int uploadCollection(Collection stringsCollection, String tableName) {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OnOffTable.uploadCollection( int ) at 30.09.2019 - (13:48)");
    }
    
    @Override
    Map<String, String> makeColumns() {
        throw new TODOException("ru.vachok.networker.data.synchronizer.OnOffTable.makeColumns( Map<String, String> ) at 30.09.2019 - (13:48)");
    }
}