package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.Keeper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.tutu.conf.InformationFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.data.enums.ConstantsFor.STR_P;


/**
 @see ScanMessagesCreatorTest
 @since 18.11.2019 (10:11) */
public class ScanMessagesCreator implements Keeper {
    
    
    @NotNull String getMsg() {
        long timeElapsed = InitProperties.getUserPref().getLong(PropertiesNames.LASTSCAN, MyCalen.getLongFromDate(7, 1, 1984, 2, 0));
        
        StringBuilder stringBuilder = new StringBuilder();
        timeElapsed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timeElapsed);
        stringBuilder.append(timeElapsed);
        stringBuilder.append(" seconds elapsed (");
        stringBuilder.append((float) timeElapsed / ConstantsFor.ONE_HOUR_IN_MIN);
        stringBuilder.append(" min) <br>");
        try {
            InitProperties.getTheProps().setProperty(PropertiesNames.TRAINS, String.valueOf(6));
            stringBuilder.append(getTrains());
        }
        catch (NoSuchElementException e) {
            InitProperties.getTheProps().setProperty(PropertiesNames.TRAINS, String.valueOf(2));
            stringBuilder.append(e.getMessage()).append(" ").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private @NotNull String getTrains() {
        InformationFactory factory = InformationFactory.getInstance();
        factory.setClassOption(Integer.parseInt(InitProperties.getTheProps().getProperty(PropertiesNames.TRAINS, String.valueOf(4))));
        return factory.getInfo().replace("[", "").replace(", ", "<br>").replace("]", "");
    }
    
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
