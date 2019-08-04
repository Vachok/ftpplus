// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;


/**
 @see InetStatSorter
 @since 15.06.2019 (9:25) */
@SuppressWarnings("ALL") public class InetStatSorterTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    /**
     @see InetStatSorter#run()
     */
    @Test
    public void testRun() {
        InetStatSorter inetStatSorter = new InetStatSorter();
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            File noCsv = new File("no.csv");
            inetStatSorter.run();
            Assert.assertFalse(noCsv.exists());
            noCsv.deleteOnExit();
        }
        else {
            Assert.assertTrue(inetStatSorter.toString().contains(LocalDate.now().getDayOfWeek().toString()), inetStatSorter.toString());
        }
    }
    
    /**
     @see InetStatSorter#sortFiles()
     */
    @Test(enabled = false)
    public void sortFilesTESTCopy() {
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
    
    private void makeCSV(String ip, @NotNull Queue<File> queueCSVFilesFromRoot) {
        String fileSepar = System.getProperty("file.separator");
        String pathInetStats = Paths.get(".").toAbsolutePath().normalize().toString() + fileSepar + ConstantsFor.STR_INETSTATS + fileSepar;
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
        File inetStatsDir = new File(absPath + fileSepar + ConstantsFor.STR_INETSTATS);
        boolean isDirExist = inetStatsDir.isDirectory();
        
        if (!isDirExist) {
            try {
                Files.createDirectories(inetStatsDir.toPath());
            }
            catch (IOException e) {
                Assert.assertNull(e, e.getMessage());
            }
        }
        
        try {
            Path copyPath = Files.copy(Paths.get(absPath + fileSepar + file.getName()), file.toPath());
            if (file.equals(copyPath.toFile())) {
                new File(file.getAbsolutePath().replace(fileSepar + ConstantsFor.STR_INETSTATS + fileSepar, fileSepar)).deleteOnExit();
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
        
    }
}