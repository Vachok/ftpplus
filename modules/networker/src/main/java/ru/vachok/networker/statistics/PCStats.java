// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.statistics;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 Class ru.vachok.networker.statistics.PCStats
 <p>
 
 @since 19.05.2019 (23:13) */
public class PCStats implements StatsOfNetAndUsers {
    
    
    private StatsOfNetAndUsers statsOfNetAndUsers = new WeekPCStats();
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    @Override public String getStats() {
        System.out.println(statsOfNetAndUsers.getStats());
        return countStat();
    }
    
    private String countStat() {
        List<String> readFileAsList = FileSystemWorker.readFileToList(ConstantsFor.FILENAME_VELKOMPCUSERAUTOTXT);
        FileSystemWorker.writeFile(ConstantsFor.FILENAME_PCAUTODISTXT, readFileAsList.parallelStream().distinct());
        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            String toCopy = "\\\\10.10.111.1\\Torrents-FTP\\" + ConstantsFor.FILENAME_PCAUTODISTXT;
            FileSystemWorker.copyOrDelFile(new File(ConstantsFor.FILENAME_PCAUTODISTXT), toCopy, false);
        }
        return countFreqOfUsers();
    }
    
    private String countFreqOfUsers() {
        List<String> pcAutoThisList = FileSystemWorker.readFileToList(new File(ConstantsFor.FILENAME_PCAUTODISTXT).getAbsolutePath());
        Collections.sort(pcAutoThisList);
        Collection<String> stringCollect = new LinkedList<>();
        for (String pcUser : pcAutoThisList) {
            stringCollect.add(Collections.frequency(pcAutoThisList, pcUser) + "times = " + pcUser);
        }
        String absolutePath = new File("possible_users.txt").getAbsolutePath();
        boolean fileWritten = FileSystemWorker.writeFile(absolutePath, stringCollect.stream());
        if (fileWritten) {
            return absolutePath;
        }
        else {
            return "Error. File not written!\n\n\n\n" + absolutePath;
        }
    }
    
}