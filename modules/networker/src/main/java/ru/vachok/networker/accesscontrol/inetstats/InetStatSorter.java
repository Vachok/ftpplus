// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;



import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;


/**
 Class ru.vachok.networker.accesscontrol.inetstats.InetStatSorter
 <p>
 Устойчивость - 1/(1+1). 100%.
 
 @see ru.vachok.networker.accesscontrol.inetstats.InetStatSorterTest
 @since 14.04.2019 (4:09) */
public class InetStatSorter implements Runnable {

    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());


    @Override public void run() {
        sortFiles();
    }
    
    public void sortFiles() {
        File[] rootFiles = new File(".").listFiles();
        Map<File, String> mapFileStringIP = new TreeMap<>();
        Set<String> ipsSet = new TreeSet<>();
        
        for (File fileFromRoot : Objects.requireNonNull(rootFiles)) {
            if (fileFromRoot.getName().toLowerCase().contains(".csv")) {
                try {
                    String[] nameSplit = fileFromRoot.getName().split("_");
                    mapFileStringIP.put(fileFromRoot, nameSplit[0].replace(".csv", ""));
                }
                catch (ArrayIndexOutOfBoundsException ignore) {
                    //
                }
            }
        }
        if (mapFileStringIP.size() == 0) {
            FileSystemWorker.writeFile("no.csv", new Date().toString());
        }
        else {
            mapFileStringIP.values().stream().forEach(ipAddr->ipsSet.add(ipAddr));
            ipsSet.forEach(ip->{
                Queue<File> csvTMPFilesQueue = new LinkedList<>();
                for (File file : mapFileStringIP.keySet()) {
                    if (file.getName().contains("_") & file.getName().contains(ip)) {
                        csvTMPFilesQueue.add(file);
                    }
                }
                makeCSV(ip, csvTMPFilesQueue);
            });
            FileSystemWorker.writeFile("inetips.set", ipsSet.stream());
        }
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("InetStatSorter{");
        sb.append(LocalDate.now().getDayOfWeek());
        sb.append('}');
        return sb.toString();
    }
    
    private void makeCSV(String ip, Queue<File> queueCSVFilesFromRoot) {
        String fileSepar = System.getProperty("file.separator");
        String pathInetStats = Paths.get(".").toAbsolutePath().normalize() + fileSepar + "inetstats" + fileSepar;
        File finalFile = new File(pathInetStats + ip + ".csv");
        
        Set<String> toWriteStatsSet = new TreeSet<>();
        
        if (finalFile.exists() & queueCSVFilesFromRoot.size() > 0) {
            toWriteStatsSet.addAll(FileSystemWorker.readFileToSet(finalFile.toPath()));
        }
        if (queueCSVFilesFromRoot.size() > 0) {
            System.out.println("Adding statistics to: " + finalFile.getAbsolutePath());
            Iterator<File> fileIterator = queueCSVFilesFromRoot.iterator();
            while (fileIterator.hasNext()) {
                File nextFile = fileIterator.next();
                toWriteStatsSet.addAll(FileSystemWorker.readFileToSet(nextFile.toPath()));
                nextFile.deleteOnExit();
            }
            boolean isWrite = FileSystemWorker.writeFile(finalFile.getAbsolutePath(), toWriteStatsSet.stream());
            System.out.println(isWrite);
        }
        else {
            System.out.println(finalFile.getAbsolutePath() + " is NOT modified.");
        }
    }
    
    private void copyToFolder(File file) {
        String absPath = Paths.get(".").toAbsolutePath().normalize().toString();
        
        String fileSepar = System.getProperty("file.separator");
        File inetStatsDir = new File(absPath + fileSepar + "inetstats");
        boolean isDirExist = inetStatsDir.isDirectory();
        
        if (!isDirExist) {
            try {
                Files.createDirectories(inetStatsDir.toPath());
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        
        try {
            Path copyPath = Files.copy(Paths.get(absPath + fileSepar + file.getName()), file.toPath());
            if (file.equals(copyPath.toFile())) {
                new File(file.getAbsolutePath().replace(fileSepar + "inetstats" + fileSepar, fileSepar)).deleteOnExit();
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        
    }
    
    /* 21.06.2019 (12:07)
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
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("InetStatSorter{");
        sb.append(LocalDate.now().getDayOfWeek());
        sb.append('}');
        return sb.toString();
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
    }*/
}