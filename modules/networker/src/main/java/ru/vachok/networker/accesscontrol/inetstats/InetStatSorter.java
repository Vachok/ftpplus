// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.util.*;
import java.util.concurrent.ForkJoinPool;


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


    private void sortFiles() {
        File[] rootFiles = new File(".").listFiles();
        Map<String, File> ipsFromFiles = new HashMap<>();
        ForkJoinPool forkJoinPool = new ForkJoinPool(3);
        for(File fCSV : Objects.requireNonNull(rootFiles)){
            if(fCSV.getName().toLowerCase().contains(".csv")) {
                try{
                    String[] nameSplit = fCSV.getName().split("net.");
                    ipsFromFiles.put(nameSplit[1].replace(".csv", ""), fCSV);
                }catch(ArrayIndexOutOfBoundsException ignore){
                    //
                }
            }
        }
        Set<String> fileAsSet = new LinkedHashSet<>();
        for (String s : ipsFromFiles.keySet()) {
            File ipF = ipsFromFiles.get(s);
            fileAsSet.add(FileSystemWorker.readFile(ipF.getAbsolutePath()));
            File f = new File(s + ".csv");
            forkJoinPool.execute(()->makeCSV(f, fileAsSet));
            ipF.deleteOnExit();
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