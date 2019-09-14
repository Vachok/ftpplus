// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.*;

import java.io.File;
import java.util.*;


/**
 Получение более детальной информации о ПК
 <p>
 
 @since 25.01.2019 (11:06) */
class TvPcInformation extends PCInfo {
    
    
    private static final String TV = "tv";
    
    private String aboutWhat = TV;
    
    private boolean isOnline;
    
    public boolean getOnline() {
        return this.isOnline;
    }
    
    @Override
    public String getInfoAbout(@NotNull String aboutWhat) {
        this.aboutWhat = aboutWhat;
        if (aboutWhat.equalsIgnoreCase("tv")) {
            return getTVNetInfoHTML();
        }
        else {
            return ConstantsFor.STR_ERROR;
        }
    }
    
    @Override
    public void setClassOption(Object option) {
        this.isOnline = (boolean) option;
    }
    
    @Override
    public String getInfo() {
        return getTVNetInfoHTML();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", TvPcInformation.class.getSimpleName() + "[\n", "\n]")
                .add("aboutWhat = '" + aboutWhat + "'")
                .add("isOnline = " + isOnline)
                .toString();
    }
    
    private static @NotNull String getTVNetInfoHTML() {
        File ptvFile = new File(FileNames.FILENAME_PTV);
        
        List<String> readFileToList = FileSystemWorker.readFileToList(ptvFile.getAbsolutePath());
        List<String> onList = new ArrayList<>();
        List<String> offList = new ArrayList<>();
        readFileToList.stream().flatMap(x->Arrays.stream(x.split(", "))).forEach(s->{
            if (s.contains("true")) {
                onList.add(s.split("/")[0]);
            }
            else {
                offList.add(s.split("/")[0]);
            }
        });
        
        String ptv1Str = OtherKnownDevices.PTV1_EATMEAT_RU;
        String ptv2Str = OtherKnownDevices.PTV2_EATMEAT_RU;
        
        int frequencyOffPTV1 = Collections.frequency(offList, ptv1Str);
        int frequencyOnPTV1 = Collections.frequency(onList, ptv1Str);
        int frequencyOnPTV2 = Collections.frequency(onList, ptv2Str);
        int frequencyOffPTV2 = Collections.frequency(offList, ptv2Str);
        
        String ptv1Stats = "<br><font color=\"#00ff69\">" + frequencyOnPTV1 + " on " + ptv1Str + "</font> | <font color=\"red\">" + frequencyOffPTV1 + " off " + ptv1Str + "</font>";
        String ptv2Stats = "<font color=\"#00ff69\">" + frequencyOnPTV2 + " on " + ptv2Str + "</font> | <font color=\"red\">" + frequencyOffPTV2 + " off " + ptv2Str + "</font>";
        
        return String.join("<br>\n", ptv1Stats, ptv2Stats);
    }
}
