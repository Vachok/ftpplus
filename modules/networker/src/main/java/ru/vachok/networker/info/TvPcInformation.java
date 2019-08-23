// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.OtherKnownDevices;

import java.io.File;
import java.util.*;


/**
 Получение более детальной информации о ПК
 <p>
 
 @since 25.01.2019 (11:06) */
class TvPcInformation implements InformationFactory {
    
    
    private static final String TV = "tv";
    
    private String aboutWhat = TV;
    
    private boolean isOnline;
    
    public boolean getOnline() {
        return this.isOnline;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", TvPcInformation.class.getSimpleName() + "[\n", "\n]")
            .add("aboutWhat = '" + aboutWhat + "'")
            .add("isOnline = " + isOnline)
            .toString();
    }
    
    @Override
    public String getInfoAbout(@NotNull String aboutWhat) {
        InformationFactory informationFactory = PCInfo.getInstance(aboutWhat);
        this.aboutWhat = aboutWhat;
        if (aboutWhat.equalsIgnoreCase(TV)) {
            return getTVNetInfo();
        }
        else {
            return informationFactory.getInfoAbout(aboutWhat);
        }
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.isOnline = (boolean) classOption;
    }
    
    @Override
    public String getInfo() {
        return toString();
    }
    
    private static @NotNull String getTVNetInfo() {
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

//        String ptv3Str = OtherKnownDevices.PTV3_EATMEAT_RU;
        
        int frequencyOffPTV1 = Collections.frequency(offList, ptv1Str);
        int frequencyOnPTV1 = Collections.frequency(onList, ptv1Str);
        int frequencyOnPTV2 = Collections.frequency(onList, ptv2Str);
        int frequencyOffPTV2 = Collections.frequency(offList, ptv2Str);
//        int frequencyOnPTV3 = Collections.frequency(onList, ptv3Str);
//        int frequencyOffPTV3 = Collections.frequency(offList, ptv3Str);
        
        String ptv1Stats = "<br><font color=\"#00ff69\">" + frequencyOnPTV1 + " on " + ptv1Str + "</font> | <font color=\"red\">" + frequencyOffPTV1 + " off " + ptv1Str + "</font>";
        String ptv2Stats = "<font color=\"#00ff69\">" + frequencyOnPTV2 + " on " + ptv2Str + "</font> | <font color=\"red\">" + frequencyOffPTV2 + " off " + ptv2Str + "</font>";
//        String ptv3Stats = "<font color=\"#00ff69\">" + frequencyOnPTV3 + " on " + ptv3Str + "</font> | <font color=\"red\">" + frequencyOffPTV3 + " off " + ptv3Str + "</font>";
        
        return String.join("<br>\n", ptv1Stats, ptv2Stats);
    }
    
}
