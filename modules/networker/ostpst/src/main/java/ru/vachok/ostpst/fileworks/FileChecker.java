// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.TFormsOST;

import java.awt.*;
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
            stringBuilder.append("MenuConsoleLocal.chkFile ERROR!").append(" No file, or file is corrupted".toUpperCase());
            stringBuilder.append("Enter another file, or type exit for exit");
        }
        else {
            stringBuilder.append("Checking file size... Filename is: ").append(fileName).append("\n");
            stringBuilder.append(fileName).append(" = ").append(fileSize).append(" GB");
            boolean isLock = lockFile(fileName);
            stringBuilder.append("is readonly = ").append(isLock);
        }
        return stringBuilder.toString();
    }
    
    @Override public String clearCopy() {
        return new RNDPSTFileCopy(fileName).clearPositions();
    }
    
    @Override public long continuousCopy() {
        throw new IllegalComponentStateException("28.05.2019 (10:01)");
    }
    
    @Override public void showCurrentResult() {
        throw new IllegalComponentStateException("28.05.2019 (13:45)");
    }
    
    @Override public String saveAndExit() {
        throw new IllegalComponentStateException("28.05.2019 (10:01)");
    }
    
    @Override public boolean processNewCopy() {
        throw new IllegalComponentStateException("28.05.2019 (10:28)");
    }
    
    private boolean lockFile(String fileName) {
        return file.setReadOnly();
    }
    
    private double checkFileSize(String fileName) throws UnsupportedEncodingException {
        this.file = new File(fileName.trim());
        long size = 666;
        try {
            size = Files.size(file.toPath());
        }
        catch (IOException | InvalidPathException e) {
            System.err.println(e.getMessage() + ": " + fileName);
            String anotherCharset = new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251, "UTF-8").getStrInAnotherCharset(fileName);
            this.file = new File(anotherCharset);
            this.fileName = anotherCharset;
            size = file.length();
            System.out.println("Trying to decode name CP1251-UTF8: " + anotherCharset);
            System.out.println(System.getProperties().getProperty(ConstantsOst.STR_ENCODING));
        }
        BigDecimal valueOf = BigDecimal.valueOf((float) size).divide(BigDecimal.valueOf(1024 * 1024 * 1024)).setScale(2, HALF_EVEN);
        return valueOf.doubleValue();
    }
    
}
