// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 Class ru.vachok.networker.fileworks.UpakFiles
 <p>
 
 @see ru.vachok.networker.fileworks.UpakFilesTest
 @since 06.07.2019 (7:32) */
public class UpakFiles extends FileSystemWorker {
    
    
    private List<File> filesToPack;
    
    private String zipName;
    
    @Override public String packFiles(List<File> filesToZip, String zipName) {
        this.filesToPack = filesToZip;
        this.zipName = zipName;
        makeZip();
        return new File(zipName).getAbsolutePath();
    }
    
    private void makeZip() {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipName))) {
            for (File toZipFile : filesToPack) {
                packFile(toZipFile, zipOutputStream);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".makeZip");
        }
    }
    
    private void packFile(File toZipFile, ZipOutputStream zipOutputStream) {
        try (InputStream inputStream = new FileInputStream(toZipFile)) {
            ZipEntry zipEntry = new ZipEntry(toZipFile.getName());
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.setLevel(9);
            byte[] bytesBuff = new byte[ConstantsFor.KBYTE];
            while (inputStream.read(bytesBuff) > 0) {
                zipOutputStream.write(bytesBuff);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".packFile");
        }
    }
}