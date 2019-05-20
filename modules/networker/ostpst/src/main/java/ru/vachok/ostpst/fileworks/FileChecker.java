package ru.vachok.ostpst.fileworks;


import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

import static java.math.RoundingMode.HALF_EVEN;


/**
 @since 16.05.2019 (11:48) */
public class FileChecker implements FileWorker {
    
    
    private String fileName;
    
    private File file;
    
    public FileChecker(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public String chkFile() {
        double fileSize = 0;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            fileSize = checkFileSize(fileName);
        }
        catch (UnsupportedEncodingException e) {
            stringBuilder.append(e.getMessage() + "\n").append(new TFormsOST().fromArray(e));
        }
        boolean isOst = fileName.toLowerCase().contains(".ost") || fileName.toLowerCase().contains(".pst");
        if (fileSize == 666 || !isOst) {
            stringBuilder.append("MenuConsoleLocal.chkFile ERROR!" + " No file, or file is corrupted".toUpperCase());
            stringBuilder.append("Enter another file, or type exit for exit");
        }
        else {
            stringBuilder.append("Checking file size... Filename is: " + fileName).append("\n");
            stringBuilder.append(fileName + " = " + fileSize + " GB");
            boolean isLock = lockFile(fileName);
            stringBuilder.append("is readonly = " + isLock);
        }
        return stringBuilder.toString();
    }
    
    @Override public String clearCopy() {
        String clearPositions = new RNDFileCopy(fileName).clearPositions();
        return clearPositions;
    }
    
    private boolean lockFile(String fileName) {
        File file = new File(fileName);
        return file.setReadOnly();
    }
    
    private double checkFileSize(String fileName) throws UnsupportedEncodingException {
        this.file = new File(fileName.trim());
        long size = 666;
        try {
            size = Files.size(file.toPath());
        }
        catch (IOException | InvalidPathException e) {
            System.err.println(e.getMessage() + "\n");
            String anotherCharset = new CharsetEncoding("windows-1251", "utf-8").getStrInAnotherCharset(fileName);
            this.file = new File(anotherCharset);
            this.fileName = anotherCharset;
            size = file.length();
            System.out.println("FileName is converted CP1251-UTF8: " + anotherCharset);
        }
        BigDecimal valueOf = BigDecimal.valueOf((float) size).divide(BigDecimal.valueOf(1024 * 1024 * 1024)).setScale(2, HALF_EVEN);
        return valueOf.doubleValue();
    }
    
}
