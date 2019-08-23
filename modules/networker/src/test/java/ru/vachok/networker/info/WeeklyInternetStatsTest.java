// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 @see WeeklyInternetStats */
@SuppressWarnings("ALL")
public class WeeklyInternetStatsTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private InformationFactory stats = InformationFactory.getInstance(InformationFactory.STATS_WEEKLYINET);
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testInetStat() {
        
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            String inetStats = stats.getInfo();
            assertFalse(inetStats.contains("does not exists!"), inetStats);
        }
        else {
            Assert.assertTrue(stats.toString().contains(LocalDate.now().getDayOfWeek().toString()), stats.toString());
        }
    }
    
    @Test
    public void dayOfWeekTesting() {
        DateFormat format = new SimpleDateFormat("E");
        String weekDay = format.format(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)));
        System.out.println(weekDay);
    }
    
    @Test
    public void testRun() {
        WeeklyInternetStats weeklyInternetStats = new WeeklyInternetStats();
        try {
            weeklyInternetStats.run();
        }
        catch (InvokeIllegalException e) {
            if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
            else {
                Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            }
        }
    }
    
    @Test
    public void testSelectFrom() {
        ((WeeklyInternetStats) stats).setSql();
        ((WeeklyInternetStats) stats).setFileName(FileNames.FILENAME_INETSTATSIPCSV);
        String userSites = ((WeeklyInternetStats) stats).writeLog("10.200.213.103", "15");
        Assert.assertTrue(userSites.contains(".csv"));
        File statFile = new File(userSites.split(" file")[0]);
        Queue<String> csvStats = FileSystemWorker.readFileToQueue(statFile.toPath());
        assertTrue(csvStats.size() == 15);
        statFile.deleteOnExit();
    }
    
    @Test
    public void testDeleteFrom() {
        long i = ((WeeklyInternetStats) stats).deleteFrom("10.200.213.103", "3");
        Assert.assertTrue(i == 3, i + " rows deleted for 10.200.213.103");
    }
    
    /**
     @see WeeklyInternetStats.InetStatSorter
     @since 15.06.2019 (9:25)
     */
    @SuppressWarnings("ALL")
    private static class InetStatSorterTest {
        
        
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
         @see WeeklyInternetStats.InetStatSorter#run()
         */
        @Test
        public void testRun() {
            throw new InvokeEmptyMethodException("22.08.2019 (17:42)");
        }
        
        /**
         @see WeeklyInternetStats.InetStatSorter#sortFiles()
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
}