// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;
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
        try {
            System.out.println("callCopy() = " + callCopy());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
    
    private String callCopy() throws IOException {
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
    
    private String zipFilesMakerCopy() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
    
        File[] inetStatsFiles = new File("inetstats").listFiles();
        Assert.assertNotNull(inetStatsFiles);
        
        long statFilesSize = 0;
    
        for (File file : inetStatsFiles) {
            statFilesSize += file.length();
        }
        stringBuilder.append(inetStatsFiles.length).append(" files of statistics. Total size: ").append(statFilesSize / ConstantsFor.MBYTE).append(" megabytes.\n");
        System.out.println(stringBuilder.toString());
        stringBuilder.append(createNEWZip(Arrays.asList(inetStatsFiles)));
        return stringBuilder.toString();
    }
    
    private String createNEWZip(List<File> toPackInZipFilesList) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        File fileZip = new File("stats.zip");
        if (!fileZip.exists()) {
            writeToFile(toPackInZipFilesList, stringBuilder);
        }
        else {
            changeExistZip(toPackInZipFilesList);
        }
        return stringBuilder.toString();
    }
    
    private void writeToFile(Collection<?> toPackInZipFilesList, StringBuilder stringBuilder) throws IOException {
        File zipFileRaw = new File("stats.zip");
        if (zipFileRaw.exists()) {
            Files.deleteIfExists(zipFileRaw.toPath());
            Files.createFile(zipFileRaw.toPath());
        }
        try (OutputStream outputStream = new FileOutputStream(zipFileRaw);
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            int numFiles = toPackInZipFilesList.size();
            for (Object obj : toPackInZipFilesList) {
                File file = null;
                ZipEntry zipEntry = null;
                if (obj instanceof File) {
                    file = (File) obj;
                }
                else if (obj instanceof ZipEntry) {
                    zipEntry = (ZipEntry) obj;
                }
                byte[] bufBytes = new byte[ConstantsFor.KBYTE];
                zipEntry = new ZipEntry(file.getPath());
                zipOutputStream.putNextEntry(zipEntry.setLastModifiedTime(FileTime.fromMillis(file.lastModified())));
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
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".writeToFile");
        }
        Assert.assertTrue(zipFileRaw.length() > 10);
    }
    
    private void changeExistZip(List<File> toPackInZipFilesList) throws IOException {
        ZipFile zipFile = new ZipFile("stats.zip");
        Enumeration<? extends ZipEntry> inZipEntries = zipFile.entries();
        Set<ZipEntry> oldEntries = new HashSet<>();
        Map<String, ZipEntry> fileNameZipEntryMap = new HashMap<>();
        List<ZipEntry> listEnt = new ArrayList<>();
        
        while (inZipEntries.hasMoreElements()) {
            ZipEntry entry = inZipEntries.nextElement();
            fileNameZipEntryMap.put(entry.getName(), entry);
        }
        Set<String> fileNames = fileNameZipEntryMap.keySet();
        fileNames.forEach(fileName->{
            ZipEntry zipEntry = fileNameZipEntryMap.get(fileName);
            long lastModFile = new File(fileName).lastModified();
            long lastModEntry = zipEntry.getLastModifiedTime().toMillis();
            lastModEntry = lastModEntry / 1000;
            lastModFile = lastModFile / 1000;
            
            if (lastModEntry != lastModFile) {
                oldEntries.add(zipEntry);
            }
        });
        if (oldEntries.size() > 0) {
            oldEntries.stream().forEach(entry->{
                fileNameZipEntryMap.replace(entry.getName(), entry);
            });
            Collection<ZipEntry> values = fileNameZipEntryMap.values();
            zipFile.close();
            createNEWZipFileWithEntry(values);
        }
        else {
            System.out.println("NO CHANGES NEED!");
        }
    }
    
    private void createNEWZipFileWithEntry(Collection<?> zipEntries) {
        try {
            this.writeToFile(zipEntries, new StringBuilder());
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
    }
}