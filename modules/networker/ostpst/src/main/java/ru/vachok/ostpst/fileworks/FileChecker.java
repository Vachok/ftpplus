package ru.vachok.ostpst.fileworks;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

import static java.math.RoundingMode.HALF_EVEN;


/**
 @since 16.05.2019 (11:48) */
public class FileChecker implements FileWorker {
    
    
    @Override public boolean chkFile(String fileName) {
        double fileSize = checkFileSize(fileName);
        boolean isOst = fileName.toLowerCase().contains(".ost") || fileName.toLowerCase().contains(".pst");
        if (fileSize == 666 || !isOst) {
            System.err.println("MenuConsoleLocal.chkFile ERROR!" + " No file, or file is corrupted".toUpperCase());
            System.out.println("Enter another file, or type exit for exit");
            return false;
        }
        else {
            System.out.println("Checking file size... Filename is: " + fileName);
            System.out.println(fileName + " = " + fileSize + " GB");
            boolean isLock = lockFile(fileName);
            System.out.println("is readonly = " + isLock);
            return isLock;
        }
    }
    
    private boolean lockFile(String fileName) {
        File file = new File(fileName);
        return file.setReadOnly();
    }
    
    private double checkFileSize(String fileName) {
        try {
            File file = new File(fileName.trim());
            long size = Files.size(file.toPath());
            BigDecimal valueOf = BigDecimal.valueOf((float) size).divide(BigDecimal.valueOf(1024 * 1024 * 1024)).setScale(2, HALF_EVEN);
            return valueOf.doubleValue();
        }
        catch (IOException | InvalidPathException e) {
            return 666;
        }
    }
    
}
