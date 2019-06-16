// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.util.*;


/**
 Class ru.vachok.networker.accesscontrol.inetstats.InetStatSorter
 <p>
 Устойчивость - 1/(1+1). 100%.

 @since 14.04.2019 (4:09) */
public class InetStatSorter implements Runnable {

    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());


    @Override public void run() {
        sortFiles();
    }
    
    /**
     Поиск файлов .csv в папке запуска.
     */
    private void sortFiles() {
        File[] rootFiles = new File(".").listFiles();
        Map<String, File> ipsFromFiles = new HashMap<>();
        Set<String> fileAsSet = new LinkedHashSet<>();
    
        for (File fileFromRoot : Objects.requireNonNull(rootFiles)) {
            if (fileFromRoot.getName().toLowerCase().contains(".csv")) {
                try{
                    String[] nameSplit = fileFromRoot.getName().split("net.");
                    ipsFromFiles.put(nameSplit[1].replace(".csv", ""), fileFromRoot);
                }catch(ArrayIndexOutOfBoundsException ignore){
                    //
                }
            }
        }
        if (ipsFromFiles.size() == 0) {
            FileSystemWorker.writeFile("no.csv", new Date().toString());
        }
        else
            for (String ipKey : ipsFromFiles.keySet()) {
                File fileInetIPCsv = ipsFromFiles.get(ipKey);
                fileAsSet.add(FileSystemWorker.readFile(fileInetIPCsv.getAbsolutePath()));
                File finalFile = new File(ipKey + ".csv");
                makeCSV(finalFile, fileAsSet);
                fileInetIPCsv.deleteOnExit();
            }
    }
    
    private void makeCSV(File tmpInetStatFile, Collection<String> fileAsSet) {
        if (!tmpInetStatFile.exists()) {
            messageToUser.info(tmpInetStatFile.getAbsolutePath(), "is exist: " + false, " Written =  " + fileAsSet.size() + " files");
            FileSystemWorker.writeFile(tmpInetStatFile.getName(), fileAsSet.stream());
        }
        else {
            List<String> stringsFromFile = FileSystemWorker.readFileToList(tmpInetStatFile.getAbsolutePath());
            fileAsSet.addAll(stringsFromFile);
            messageToUser.info(tmpInetStatFile.getAbsolutePath(), "exist: " + true, " To write =  " + fileAsSet.size() + " strings");
            FileSystemWorker.writeFile(tmpInetStatFile.getName(), fileAsSet.stream());
        }
    }
}