// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 Class ru.vachok.networker.services.FilesZipPacker
 <p>
 @see ru.vachok.networker.services.FilesZipPackerTest
 @since 21.06.2019 (20:14) */
public class FilesZipPacker implements Callable<String> {
    
    
    @Override public String call() throws IOException {
        String retString;
        try {
            retString = zipFilesMakerCopy();
        }
        catch (IOException e) {
            File zipFile = new File(ConstantsFor.FILENAME_STATSZIP);
            zipFile.delete();
            retString = zipFilesMakerCopy();
        }
        return retString;
    }
    
    private String zipFilesMakerCopy() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        
        File[] inetStatsFiles = new File(ConstantsFor.STR_INETSTATS).listFiles();
        
        long statFilesSize = 0;
        
        for (File file : Objects.requireNonNull(inetStatsFiles)) {
            statFilesSize += file.length();
        }
        stringBuilder.append(inetStatsFiles.length).append(" files of statistics. Total size: ").append(statFilesSize / ConstantsFor.MBYTE).append(" megabytes.\n");
        System.out.println(stringBuilder);
        stringBuilder.append(createNEWZip(Arrays.asList(inetStatsFiles)));
        return stringBuilder.toString();
    }
    
    private String createNEWZip(List<File> toPackInZipFilesList) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        File fileZip = new File(ConstantsFor.FILENAME_STATSZIP);
        if (!fileZip.exists()) {
            writeToFile(toPackInZipFilesList, stringBuilder);
        }
        else {
            changeExistZip(toPackInZipFilesList);
        }
        return stringBuilder.toString();
    }
    
    private void writeToFile(Collection<?> toPackInZipFilesList, StringBuilder stringBuilder) throws IOException {
        File zipFileRaw = new File(ConstantsFor.FILENAME_STATSZIP);
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
                zipEntry = new ZipEntry(Objects.requireNonNull(file).getPath());
                zipOutputStream.putNextEntry(zipEntry.setLastModifiedTime(FileTime.fromMillis(file.lastModified())));
                int inRead;
                try (InputStream inputStream = new FileInputStream(file.getAbsolutePath())) {
                    while ((inRead = inputStream.read(bufBytes)) > 0) {
                        zipOutputStream.write(bufBytes, 0, inRead);
                    }
                }
                
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
    }
    
    private void changeExistZip(List<File> toPackInZipFilesList) throws IOException {
        try (ZipFile zipFile = new ZipFile(ConstantsFor.FILENAME_STATSZIP)) {
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
                oldEntries.stream().forEach(entry->fileNameZipEntryMap.replace(entry.getName(), entry));
                Collection<ZipEntry> values = fileNameZipEntryMap.values();
                zipFile.close();
                createNEWZipFileWithEntry(values);
            }
            else {
                System.out.println("NO CHANGES NEED!");
            }
        }
    }
    
    private void createNEWZipFileWithEntry(Collection<?> zipEntries) {
        try {
            this.writeToFile(zipEntries, new StringBuilder());
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".createNEWZipFileWithEntry");
        }
    }
    
}