package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.Keeper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.data.enums.ConstantsFor.STR_P;


public class ScanMessagesCreator implements Keeper {
    
    
    @NotNull String getMsg() {
        long timeLeft = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
        
        StringBuilder stringBuilder = new StringBuilder();
        timeLeft = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timeLeft);
        stringBuilder.append(timeLeft);
        stringBuilder.append(" seconds elapsed (");
        stringBuilder.append((float) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN);
        stringBuilder.append(" min) <br>");
//        stringBuilder.append(getTrains());
        return stringBuilder.toString();
    }
    
/*
    private @NotNull String getTrains() {
        BackEngine backEngine = new SiteParser();
        List<Date> comingTrains = backEngine.getComingTrains();
        StringBuilder stringBuilder = new StringBuilder();
        for (Date date : comingTrains) {
            long minLeft = TimeUnit.MILLISECONDS.toMinutes(date.getTime() - System.currentTimeMillis());
            stringBuilder.append(minLeft).append(" min left<br>");
        }
        return stringBuilder.toString();
    }
*/
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScanMessagesCreator{");
        sb.append('}');
        return sb.toString();
    }
    
    @NotNull String getTitle(int currentPC) {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(currentPC);
        titleBuilder.append("/");
        titleBuilder.append(InitProperties.getTheProps().getProperty(PropertiesNames.TOTPC));
        titleBuilder.append(" PCs (");
        titleBuilder.append(InitProperties.getTheProps().getProperty(PropertiesNames.ONLINEPC, "0"));
        titleBuilder.append(")");
        return titleBuilder.toString();
    }
    
    @NotNull String fillUserPCForWEBModel() {
        StringBuilder brStringBuilder = new StringBuilder();
        brStringBuilder.append(STR_P);
        ConcurrentNavigableMap<String, Boolean> linksMap = NetKeeper.getUsersScanWebModelMapWithHTMLLinks();
        if (linksMap.size() < 50) {
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
