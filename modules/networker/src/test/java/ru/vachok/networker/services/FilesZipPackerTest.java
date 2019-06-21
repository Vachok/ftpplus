// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 @see FilesZipPacker
 @since 21.06.2019 (20:15) */
@SuppressWarnings("ALL") public class FilesZipPackerTest {
    
    
    @Test
    public void testCall() {
        FilesZipPacker filesZipPacker = new FilesZipPacker();
        try {
            filesZipPacker.call();
        }
        catch (Exception e) {
            Assert.assertNotNull(e);
        }
        System.out.println("callCopy() = " + callCopy());
    }
    
    private String callCopy() {
        String retString = "null";
        try {
            retString = zipFilesMakerCopy();
        }
        catch (Exception e) {
            File zipFile = new File("stats.zip");
            zipFile.delete();
            retString = zipFilesMakerCopy();
        }
        return retString;
    }
    
    @Test(enabled = false)
    public void zipTest() {
        File[] testArray = new File("lib").listFiles();
        Assert.assertTrue(testArray.length > 2);
        try {
            String zipCreateLog = createNEWZip(Arrays.asList(testArray));
            System.out.println("zipCreateLog = " + zipCreateLog);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    private String zipFilesMakerCopy() {
        StringBuilder stringBuilder = new StringBuilder();
    
        File[] inetStatsFiles = new File("inetstats").listFiles();
        Assert.assertNotNull(inetStatsFiles);
        
        long statFilesSize = 0;
    
        for (File file : inetStatsFiles) {
            statFilesSize += file.length();
        }
        stringBuilder.append(inetStatsFiles.length).append(" files of statistics. Total size: ").append(statFilesSize / ConstantsFor.MBYTE).append(" megabytes.\n");
        System.out.println(stringBuilder.toString());
        try {
            stringBuilder.append(createNEWZip(Arrays.asList(inetStatsFiles)));
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage());
            Assert.assertNull(e);
            Assert.assertTrue(new File("stats.zip").delete());
        }
        return stringBuilder.toString();
    }
    
    private String createNEWZip(List<File> toPackInZipFilesList) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        File zipFileRaw = new File("stats.zip");
        boolean isExists = zipFileRaw.exists();
        if (!isExists) {
            try (OutputStream outputStream = new FileOutputStream(zipFileRaw);
                 ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                int numFiles = toPackInZipFilesList.size();
                for (File file : toPackInZipFilesList) {
                    byte[] bufBytes = new byte[ConstantsFor.KBYTE];
                    zipOutputStream.putNextEntry(new ZipEntry(file.getPath()).setLastModifiedTime(FileTime.fromMillis(file.lastModified())));
                    int inRead;
                    InputStream inputStream = new FileInputStream(file.getAbsolutePath());
                    while ((inRead = inputStream.read(bufBytes)) > 0) {
                        zipOutputStream.write(bufBytes, 0, inRead);
                    }
                    inputStream.close();
                
                    String progressString = new StringBuilder().append(zipFileRaw.length() / ConstantsFor.MBYTE)
                        .append(" mbytes in: ").append(zipFileRaw.getAbsolutePath())
                        .append(" file last packet is: ").append(file.getName())
                        .append(" left: ").append(--numFiles)
                        .append(" files").toString();
                
                    System.out.println(progressString);
                    stringBuilder.append(progressString);
                }
            }
            Assert.assertTrue(zipFileRaw.length() > 10);
        }
        else {
            changeExistZip(toPackInZipFilesList);
        }
        return stringBuilder.toString();
    }
    
    private void changeExistZip(List<File> toPackInZipFilesList) throws IOException {
        ZipFile zipFile = new ZipFile("stats.zip");
        Enumeration<? extends ZipEntry> inZipEntries = zipFile.entries();
        Queue<File> toChangeInZip = new LinkedList<>();
        
        while (inZipEntries.hasMoreElements()) {
            ZipEntry zipEntry = inZipEntries.nextElement();
            Predicate<File> predicate = file->zipEntry.getName().contains(file.getName()) & (zipEntry.getTime() < file.lastModified());
            toPackInZipFilesList.stream().anyMatch(predicate.and(file->toChangeInZip.add(file)));
        }
        Assert.assertTrue(toChangeInZip.size() > 0);
        throw new IllegalStateException("22.06.2019 (2:57)");
    }
    
    private void makeChangesInZip(ZipFile zipFile, Queue<File> zipRenewQueue) throws IOException {
        while (zipRenewQueue.iterator().hasNext()) {
            File changedFile = zipRenewQueue.poll();
            ZipEntry zipEntry = zipFile.getEntry(changedFile.getPath());
            Assert.assertNotNull(zipEntry);
            zipEntry = new ZipEntry(changedFile.getPath());
            if (changedFile.lastModified() > zipEntry.getTime()) {
                try (InputStream inputStream = new FileInputStream(changedFile);
                     OutputStream outputStream = new FileOutputStream(zipFile.getName());
                     ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                    zipOutputStream.putNextEntry(zipEntry);
                    byte[] bytesBuff = new byte[ConstantsFor.KBYTE];
                    int inRead;
                    while ((inRead = inputStream.read(bytesBuff)) > 0) {
                        zipOutputStream.write(bytesBuff, 0, inRead);
                    }
                }
                Assert.assertTrue(new File(zipFile.getName()).lastModified() > System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(30));
                System.out.println(new Date(zipEntry.getTime()) + " in zip" + zipEntry.getName() + " FILE IS CHANGED " + new Date(changedFile.lastModified()) + " is " + changedFile
                    .getAbsolutePath());
            }
            else {
                Assert.assertTrue(new File(zipFile.getName()).lastModified() < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(30));
                System.out.println(new Date(zipEntry.getTime()) + " in zip" + zipEntry.getName() + " FILE IS NOT CHANGED " + new Date(changedFile.lastModified()) + " is " + changedFile
                    .getAbsolutePath());
            }
        }
    }
    
}