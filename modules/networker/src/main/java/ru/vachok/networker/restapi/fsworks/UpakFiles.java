// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.fsworks;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractNetworkerFactory;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.net.NetScanFileWorker;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.attribute.FileTime;
import java.util.Deque;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 Class ru.vachok.networker.restapi.fsworks.UpakFiles
 <p>
 
@see ru.vachok.networker.restapi.fsworks.UpakFilesTest
 @since 06.07.2019 (7:32) */
public class UpakFiles extends AbstractNetworkerFactory implements FilesHelper {
    
    
    private List<File> filesToPack;
    
    private String zipName;
    
    private int compressionLevelFrom0To9;
    
    @Contract(pure = true)
    public UpakFiles(int compressionLevelFrom0To9) {
        this.compressionLevelFrom0To9 = compressionLevelFrom0To9;
    }
    
    public String packFiles(List<File> filesToZip, String zipName) {
        this.filesToPack = filesToZip;
        this.zipName = zipName;
        makeZip();
        return new File(zipName).getAbsolutePath();
    }
    
    @Override
    public UpakFiles getUpakFiles() {
        return this;
    }
    
    @Override
    public Deque<InetAddress> getOnlineDevicesInetAddress() {
        return new NetScanFileWorker().getOnlineDevicesInetAddress();
    }
    
    @Override
    public List<String> getCurrentScanLists() {
        return DiapazonScan.getCurrentPingStats();
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
                zipOutputStream.setLevel(compressionLevelFrom0To9);
                packFile(toZipFile, zipOutputStream);
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".makeZip");
        }
        File zipCreatedFile = new File(zipName);
        System.out.println(zipCreatedFile.getAbsolutePath() + " zip. Size = " + (zipCreatedFile.length() / ConstantsFor.MBYTE) + " compress level: " + compressionLevelFrom0To9);
    }
    
    private void packFile(@NotNull File toZipFile, @NotNull ZipOutputStream zipOutputStream) {
        try (InputStream inputStream = new FileInputStream(toZipFile)) {
            ZipEntry zipEntry = new ZipEntry(toZipFile.getName());
            zipOutputStream.putNextEntry(zipEntry);
    
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