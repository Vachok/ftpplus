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


    private void sortFiles() {
        File[] rootFiles = new File(".").listFiles();
        List<String> ipsFromFiles = new ArrayList<>();

        for(File fCSV : Objects.requireNonNull(rootFiles)){
            if(fCSV.getName().toLowerCase().contains(".csv")) {
                try{
                    String[] nameSplit = fCSV.getName().split("_");
                    ipsFromFiles.add(nameSplit[0].replace(".csv" , ""));
                }catch(ArrayIndexOutOfBoundsException ignore){
                    //
                }
            }
        }
        for (File ipF : rootFiles) {
            Set<String> fileAsSet = new LinkedHashSet<>();
            ipsFromFiles.forEach(x->{
                if(ipF.getName().contains(x + "_")) {
                    fileAsSet.add(FileSystemWorker.readFile(ipF.getAbsolutePath()));
                    File f = new File("inet." + ipF.getName().split("_")[0] + ".csv");
                    makeCSV(f, fileAsSet);
                    ipF.deleteOnExit();
                }
            });
        }
    }
    
    
    private void makeCSV(File f, Collection<String> fileAsSet) {
        if(!f.exists()) {
            messageToUser.info(f.getAbsolutePath(), "is exist: " + false, " Queue to write =  " + fileAsSet.size() + " items.");
            FileSystemWorker.writeFile(f.getName(), fileAsSet.stream());
        }
        else {
            List<String> stringsFromFile = FileSystemWorker.readFileToList(f.getAbsolutePath());
            fileAsSet.addAll(stringsFromFile);
            messageToUser.info(f.getAbsolutePath(), "exist: " + true, " To write =  " + fileAsSet.size() + " strings");
            FileSystemWorker.writeFile(f.getName(), fileAsSet.stream());
        }
    }
}