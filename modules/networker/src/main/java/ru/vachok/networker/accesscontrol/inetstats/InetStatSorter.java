// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.services.FilesZipPacker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


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
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().submit(new FilesZipPacker());
        try {
            System.out.println(submit.get());
        }
        catch (InterruptedException | ExecutionException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    public void sortFiles() {
        File[] rootFiles = new File(".").listFiles();
        Map<File, String> mapFileStringIP = new TreeMap<>();
    
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
            Set<String> ipsSet = new TreeSet<>(mapFileStringIP.values());
            ipsSet.forEach(ip->{
                Collection<File> csvTMPFilesQueue = new LinkedList<>();
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
    
    private void makeCSV(String ip, Collection<File> queueCSVFilesFromRoot) {
        String fileSeparator = System.getProperty(ConstantsFor.PRSYS_SEPARATOR);
        String pathInetStats = Paths.get(".").toAbsolutePath().normalize() + fileSeparator + ConstantsFor.STR_INETSTATS + fileSeparator;
        File finalFile = new File(pathInetStats + ip + ".csv");
        checkDirExists(pathInetStats);
        Set<String> toWriteStatsSet = new HashSet<>();
        
        if (finalFile.exists() & queueCSVFilesFromRoot.size() > 0) {
            toWriteStatsSet.addAll(FileSystemWorker.readFileToSet(finalFile.toPath()));
        }
        if (queueCSVFilesFromRoot.size() > 0) {
            System.out.println("Adding statistics to: " + finalFile.getAbsolutePath());
            boolean isDelete = false;
            for (File nextFile : queueCSVFilesFromRoot) {
                toWriteStatsSet.addAll(FileSystemWorker.readFileToSet(nextFile.toPath()));
                isDelete = nextFile.delete();
                if (!isDelete) {
                    nextFile.deleteOnExit();
                }
            }
            boolean isWrite = FileSystemWorker.writeFile(finalFile.getAbsolutePath(), toWriteStatsSet.stream());
            System.out.println(isWrite + " write: " + finalFile.getAbsolutePath());
            System.out.println(isDelete + " deleted temp csv.");
        }
        else {
            System.out.println(finalFile.getAbsolutePath() + " is NOT modified.");
        }
    }
    
    private void checkDirExists(String directoryName) {
        File inetStatsDirectory = new File(directoryName);
        if (!inetStatsDirectory.exists() || !inetStatsDirectory.isDirectory()) {
            try {
                Files.createDirectories(inetStatsDirectory.toPath());
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
    
    private void copyToFolder(File file) {
        String absPath = Paths.get(".").toAbsolutePath().normalize().toString();
    
        String fileSepar = System.getProperty(ConstantsFor.PRSYS_SEPARATOR);
        File inetStatsDir = new File(absPath + fileSepar + ConstantsFor.STR_INETSTATS);
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
                new File(file.getAbsolutePath().replace(fileSepar + ConstantsFor.STR_INETSTATS + fileSepar, fileSepar)).deleteOnExit();
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        
    }
}