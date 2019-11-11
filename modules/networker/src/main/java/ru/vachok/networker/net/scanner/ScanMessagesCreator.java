package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.Keeper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;

import static ru.vachok.networker.data.enums.ConstantsFor.STR_P;


public class ScanMessagesCreator implements Keeper {
    
    
    private long lastScanStamp = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, System.currentTimeMillis());
    
    @NotNull String getMsg() {
        long timeLeft = InitProperties.getUserPref().getLong(PropertiesNames.NEXTSCAN, MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(timeLeft);
        stringBuilder.append(" seconds (");
        stringBuilder.append((float) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN);
        stringBuilder.append(" min) left<br>Delay period is ");
        stringBuilder.append(PcNamesScanner.DURATION_MIN);
        return stringBuilder.toString();
    }
    
    @NotNull String getTitle(int currentPC) {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(currentPC);
        titleBuilder.append("/");
        titleBuilder.append(InitProperties.getTheProps().getProperty(PropertiesNames.TOTPC));
        titleBuilder.append(" PCs (");
        titleBuilder.append(InitProperties.getTheProps().getProperty(PropertiesNames.ONLINEPC, "0"));
        titleBuilder.append(") Next run ");
        titleBuilder.append(new Date(lastScanStamp));
        return titleBuilder.toString();
    }
    
    @NotNull String fillUserPCForWEBModel() {
        StringBuilder brStringBuilder = new StringBuilder();
        brStringBuilder.append(STR_P);
        ConcurrentNavigableMap<String, Boolean> linksMap = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
        if (linksMap.size() == 0) {
            brStringBuilder.append(FileSystemWorker.readRawFile(new File(FileNames.LASTNETSCAN_TXT).getAbsolutePath()));
        }
        else {
            Set<String> keySet = linksMap.keySet();
            List<String> list = new ArrayList<>(keySet.size());
            list.addAll(keySet);
            
            Collections.sort(list);
            Collections.reverse(list);
            for (String keyMap : list) {
                String valueMap = String.valueOf(NetKeeper.getUsersScanWebModelMapWithHTMLLinks().get(keyMap));
                brStringBuilder.append(keyMap).append(" ").append(valueMap);
            }
        }
        return brStringBuilder.toString().replace("true", "").replace(ConstantsFor.STR_FALSE, "");
        
    }
}
