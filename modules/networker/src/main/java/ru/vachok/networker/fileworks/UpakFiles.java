// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import ru.vachok.networker.ConstantsFor;

import java.io.*;
import java.nio.file.attribute.FileTime;
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
    
    private int compressionLevelFrom0To9;
    
    public UpakFiles(int compressionLevelFrom0To9) {
        this.compressionLevelFrom0To9 = compressionLevelFrom0To9;
    }
    
    @Override public String packFiles(List<File> filesToZip, String zipName) {
        this.filesToPack = filesToZip;
        this.zipName = zipName;
        makeZip();
        return new File(zipName).getAbsolutePath();
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("UpakFiles{");
        sb.append("compressionLevelFrom0To9=").append(compressionLevelFrom0To9);
        sb.append(", filesToPack=").append(filesToPack);
        sb.append(", zipName='").append(zipName).append('\'');
        sb.append('}');
        return sb.toString();
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
        File zipCreatedFile = new File(zipName);
        System.out.println(zipCreatedFile.getAbsolutePath() + " zip. Size = " + (zipCreatedFile.length() / ConstantsFor.MBYTE) + " compress level: " + compressionLevelFrom0To9);
    }
    
    private void packFile(File toZipFile, ZipOutputStream zipOutputStream) {
        try (InputStream inputStream = new FileInputStream(toZipFile)) {
            ZipEntry zipEntry = new ZipEntry(toZipFile.getName());
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.setLevel(compressionLevelFrom0To9);
            zipEntry.setCreationTime(FileTime.fromMillis(ConstantsFor.getAtomicTime()));
            byte[] bytesBuff = new byte[ConstantsFor.KBYTE];
            while (inputStream.read(bytesBuff) > 0) {
                zipOutputStream.write(bytesBuff);
            }
            zipOutputStream.closeEntry();
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".packFile");
        }
    }
}