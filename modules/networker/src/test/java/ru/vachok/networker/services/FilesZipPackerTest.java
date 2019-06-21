// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
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
        throw new IllegalStateException("21.06.2019 (21:12)");
    }
    
    private String callCopy() {
        return zipFilesMakerCopy();
    }
    
    private String zipFilesMakerCopy() {
        StringBuilder stringBuilder = new StringBuilder();
        String startPath = Paths.get(".").toAbsolutePath().normalize().toString();
        String sysSepar = System.getProperty("file.separator");
        String pathInetStatFiles = startPath + sysSepar + "inetstats";
        File[] inetStatsFiles = new File(pathInetStatFiles).listFiles();
        long statFilesSize = 0;
        for (File file : inetStatsFiles) {
            statFilesSize += file.length();
        }
        stringBuilder.append(inetStatsFiles.length).append(" files of statistics. Total size: ").append(statFilesSize / ConstantsFor.MBYTE).append(" megabytes.\n");
        try {
            stringBuilder.append(createZip(pathInetStatFiles, Arrays.asList(inetStatsFiles)));
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage());
            Assert.assertNull(e);
        }
        return stringBuilder.toString();
    }
    
    private String createZip(String pathInetStatFiles, List<File> toPackInZipFilesList) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String fileAbsPathAsString = pathInetStatFiles + System.getProperty("file.separator") + "stats.zip";
        File zipFileRaw = new File(fileAbsPathAsString);
        try (OutputStream outputStream = new FileOutputStream(fileAbsPathAsString);
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            int numFiles = toPackInZipFilesList.size();
            for (File file : toPackInZipFilesList) {
                byte[] bufBytes = new byte[ConstantsFor.KBYTE];
                zipOutputStream.putNextEntry(new ZipEntry(file.getPath()));
                int inRead;
                InputStream inputStream = new FileInputStream(file);
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
        return stringBuilder.toString();
    }
    
}