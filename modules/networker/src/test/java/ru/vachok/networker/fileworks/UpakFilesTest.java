// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.fileworks;


import org.junit.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.InvokeEmptyMethodException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 @see UpakFiles
 @since 06.07.2019 (7:32) */
public class UpakFilesTest {
    
    
    private List<File> filesToPack;
    
    
    @Test
    public void testUpak() {
        UpakFiles upakFiles = new UpakFiles();
        try {
            String upakResult = upakFiles.packFile(new File("g:\\tmp_a.v.komarov.pst"));
        }
        catch (InvokeEmptyMethodException e) {
            System.err.println(e.getMessage());
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void makeZip() {
        this.filesToPack = new ArrayList<>();
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
            zipOutputStream.setLevel(9);
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