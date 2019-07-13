// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.stats.connector.SSHWorker;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;


/**
 @since 17.04.2019 (11:30) */
public class AccessListsCheckUniq implements SSHWorker, Runnable {
    
    
    private static final Pattern FILENAME_COMPILE = Pattern.compile("/pf/");

    private static final Pattern FILENAME_PATTERN = Pattern.compile(" && ");
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private Collection<String> fileNames = new ArrayList<>();
    
    @Override public void run() {
        messageToUser.info(getClass().getSimpleName() + ".run", "uploadLibs()", " = " + connectTo());
    }
    
    @Override public String connectTo() {
        StringBuilder stringBuilder = new StringBuilder();
        SSHFactory.Builder builder = new SSHFactory.Builder(getSRVNeed(), "uname -a", getClass().getSimpleName());
        SSHFactory sshFactory = builder.build();
        String[] commandsToGetList = {ConstantsNet.COM_CAT24HRSLIST, "sudo cat /etc/pf/vipnet && exit", ConstantsFor.SSH_SHOW_PFSQUID, ConstantsFor.SSH_SHOW_SQUIDLIMITED, ConstantsFor.SSH_SHOW_PROXYFULL};
        for (String getList : commandsToGetList) {
            makePfListFiles(getList, sshFactory, stringBuilder);
        }
        parseListFiles();
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", AccessListsCheckUniq.class.getSimpleName() + "[\n", "\n]")
            .add("fileNames = " + fileNames.size())
            .toString();
    }
    
    private static String getSRVNeed() {
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            return SwitchesWiFi.RUPSGATE;
        }
        else {
            return SwitchesWiFi.IPADDR_SRVGIT;
        }
    }
    
    private void parseListFiles() {
        Map<String, String> usersIPFromPFLists = new HashMap<>();
        for (String fileName : fileNames) {
            Queue<String> stringDeque = FileSystemWorker.readFileToQueue(new File(fileName).toPath());
            while (!stringDeque.isEmpty()) {
                String key = stringDeque.poll();
                String put = usersIPFromPFLists.putIfAbsent(key, fileName);
                if (put != null) {
                    usersIPFromPFLists.put(key + " " + fileName, "NOT UNIQUE"); //пометка адресов, которые присутствуют в более чем одном списке.
                }
            }
        }
        StringBuilder fromArray = new StringBuilder();
        for (Map.Entry<String, String> ipEntries : usersIPFromPFLists.entrySet()) {
            if (ipEntries.getValue().equals("NOT UNIQUE")) {
                fromArray.append(ipEntries.getKey()).append(" ").append(ipEntries.getValue()).append("\n");
            }
        }
        
        messageToUser.info(getClass().getSimpleName(), ".parseListFiles", " = \n" + fromArray);
        FileSystemWorker.writeFile(ConstantsFor.FILENAME_INETUNIQ, fromArray.toString());
        AppComponents.netKeeper().setInetUniqMap(usersIPFromPFLists);
    }
    
    private void makePfListFiles(String getList, @NotNull SSHFactory sshFactory, @NotNull StringBuilder stringBuilder) {
        sshFactory.setCommandSSH(getList);
        String call = sshFactory.call();
        Set<String> stringSet = FileSystemWorker.readFileToSet(sshFactory.getTempFile());
        String fileName = FILENAME_PATTERN.split(FILENAME_COMPILE.split(getList)[1])[0] + ".list";
        fileNames.add(fileName);
        FileSystemWorker.writeFile(fileName, stringSet.stream());
        stringBuilder.append(call);
    }
}
