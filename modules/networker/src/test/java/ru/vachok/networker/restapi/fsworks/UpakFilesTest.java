// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.fsworks;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 @see UpakFiles
 @since 06.07.2019 (7:32) */
public class UpakFilesTest {
    
    
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
    
    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    
    @BeforeMethod
    public void setMXBean() {
        threadMXBean.resetPeakThreadCount();
        threadMXBean.setThreadCpuTimeEnabled(true);
        threadMXBean.setThreadContentionMonitoringEnabled(true);
    }
    
    
    @Test
    public void testUpak() {
        final long start = System.nanoTime();
        UpakFiles upakFiles = new UpakFiles();
    
        Map<File, Integer> fileSizes = new TreeMap<>();
        for (File listFile : new File(ConstantsFor.ROOT_PATH_WITH_SEPARATOR).listFiles()) {
            fileSizes.put(listFile, Math.toIntExact(listFile.length() / ConstantsFor.KBYTE));
        }
        Optional<Integer> max = fileSizes.values().stream().max(Comparator.naturalOrder());
        File fileToPack = null;
        for (Map.Entry<File, Integer> integerEntry : fileSizes.entrySet()) {
            if (max.get().equals(integerEntry.getValue())) {
                fileToPack = integerEntry.getKey();
            }
        }
        String zipFileName = (fileToPack != null ? fileToPack.getName().split("\\Q.\\E") : new String[0])[0] + ".zip";
        int compLevel = new Random().nextInt(9);
        String upakResult = upakFiles.createZip(Collections.singletonList(fileToPack), zipFileName, compLevel);
        Assert.assertTrue(new File(zipFileName).exists());
        long realTime = System.nanoTime() - start;
        long cpuTime = threadMXBean.getCurrentThreadCpuTime();
        long origSize = fileToPack.length() / ConstantsFor.KBYTE;
        long packedSize = new File(zipFileName).length() / ConstantsFor.KBYTE;
    
        String saveInfo = cpuTime + " cpu time, real time: " + realTime + " in nanoseconds. Compression: " + compLevel + ". File size orig (" + fileToPack.getName() + "): " + origSize;
        saveInfo = saveInfo + " kbytes. Packed: " + packedSize + " kbytes diff: " + (origSize - packedSize);
        saveInfo = saveInfo + "\nOr " + TimeUnit.NANOSECONDS.toSeconds(realTime) + " " + TimeUnit.NANOSECONDS.toSeconds(cpuTime) + " in seconds. " + new Date() + "\n\n\n";
    
        FileSystemWorker.appendObjectToFile(new File(getClass().getSimpleName() + ".res"), saveInfo);
    }
    
    @Test
    public void toStringTest() {
        String toStr = new UpakFiles().toString();
        Assert.assertTrue(toStr.contains("compressionLevelFrom0To9=5"), toStr);
    }
    
    @Test
    public void makeZip() {
        List<File> filesToPack = new ArrayList<>();
        filesToPack.add(new File("\\\\10.10.111.1\\Torrents-FTP\\logsCopy\\common.own"));
        filesToPack.add(new File("\\\\10.10.111.1\\Torrents-FTP\\logsCopy\\common.rgh"));
        
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream("new.zip"))) {
            for (File toZipFile : filesToPack) {
                packFile(toZipFile, zipOutputStream);
            }
        }
        catch (IOException e) {
            org.testng.Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    private void packFile(@NotNull File toZipFile, @NotNull ZipOutputStream zipOutputStream) {
        try (InputStream inputStream = new FileInputStream(toZipFile)) {
            ZipEntry zipEntry = new ZipEntry(toZipFile.getName());
            zipOutputStream.putNextEntry(zipEntry);
    
            byte[] bytesBuff = new byte[ConstantsFor.KBYTE];
            while (inputStream.read(bytesBuff) > 0) {
                zipOutputStream.write(bytesBuff);
            }
        }
        catch (IOException e) {
            org.testng.Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}