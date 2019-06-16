// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
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
        String[] commandsToGetList = {"sudo cat /etc/pf/24hrs && exit", "sudo cat /etc/pf/vipnet && exit", "sudo cat /etc/pf/squid && exit", "sudo cat /etc/pf/squidlimited && exit", "sudo cat /etc/pf/tempfull && exit"};
        for (String getList : commandsToGetList) {
            sshFactory.setCommandSSH(getList);
            String call = sshFactory.call();
            Set<String> stringSet = FileSystemWorker.readFileToSet(sshFactory.getTempFile());
            String fileName = FILENAME_PATTERN.split(FILENAME_COMPILE.split(getList)[1])[0] + ".list";
            fileNames.add(fileName);
            FileSystemWorker.writeFile(fileName, stringSet.stream());
            stringBuilder.append(call);
        }
        parseListFiles();
        return stringBuilder.toString();
    }
    
    private static String getSRVNeed() {
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            return ConstantsFor.IPADDR_SRVNAT;
        }
        else {
            return ConstantsFor.IPADDR_SRVGIT;
        }
    }
    
    private void parseListFiles() {
        Map<String, String> mapInternetUsageLog = new HashMap<>();
        for (String fileName : fileNames) {
            Queue<String> stringDeque = FileSystemWorker.readFileToQueue(new File(fileName).toPath());
            while (!stringDeque.isEmpty()) {
                String key = stringDeque.poll();
                String put = mapInternetUsageLog.putIfAbsent(key, fileName);
                if (put != null) {
                    mapInternetUsageLog.put(key + " " + fileName, "NOT UNIQUE");
                }
            }
        }
        String fromArray = new TForms().fromArray(mapInternetUsageLog, false);
        messageToUser.info(getClass().getSimpleName(), ".parseListFiles", " = \n" + fromArray);
        FileSystemWorker.writeFile(ConstantsFor.FILENAME_INETUNIQ, fromArray);
        AppComponents.netKeeper().setInetUniqMap(mapInternetUsageLog);
    }
}
