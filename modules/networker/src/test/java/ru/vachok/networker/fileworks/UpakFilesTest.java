// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 @see UpakFiles
 @since 06.07.2019 (7:32) */
public class UpakFilesTest {
    
    
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
        int compLevel = 5;
        UpakFiles upakFiles = new UpakFiles(compLevel);
        File fileToPack = new File("g:\\tmp_a.v.komarov.pst");
        String zipFileName = "komarov.zip";
        String upakResult = upakFiles.packFiles(Collections.singletonList(fileToPack), zipFileName);
        Assert.assertTrue(new File(zipFileName).exists());
        long realTime = System.nanoTime() - start;
        long cpuTime = threadMXBean.getCurrentThreadCpuTime();
        long origSize = fileToPack.length() / ConstantsFor.KBYTE;
        long packedSize = new File(zipFileName).length() / ConstantsFor.KBYTE;
    
        String saveInfo = cpuTime + " cpu time, real time: " + realTime + " in nanoseconds. Compression: " + compLevel + ". File size orig (pst): " + origSize;
        saveInfo = saveInfo + " kbytes. Packed: " + packedSize + " kbytes diff: " + (origSize - packedSize);
        saveInfo = saveInfo + "\nOr " + TimeUnit.NANOSECONDS.toSeconds(realTime) + " " + TimeUnit.NANOSECONDS.toSeconds(cpuTime) + " in seconds. " + new Date() + "\n\n\n";
    
        FileSystemWorker.appendObjectToFile(new File(getClass().getSimpleName() + ".res"), saveInfo);
    }
    
    @Test
    public void toStringTest() {
        Assert.assertTrue(new UpakFiles(9).toString().contains("compressionLevelFrom0To9=9"));
    }
    
    @Test(enabled = false)
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
    
    private void packFile(File toZipFile, ZipOutputStream zipOutputStream) {
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