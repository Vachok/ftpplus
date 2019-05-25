// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTRAFileContent;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.TFormsOST;

import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.RejectedExecutionException;


/**
 @since 30.04.2019 (9:36) */
class RNDFileCopy implements Serializable {
    
    
    private static final long serialVersionUID = 42L;
    
    private String fileName;
    
    private long lastFileCaretPosition;
    
    private long lastWritePosition;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private ThreadMXBean threadMXBean;
    
    private Properties properties;
    
    /**
     @param fileName may be null
     */
    RNDFileCopy(String fileName) {
        this.fileName = fileName;
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadCpuTimeEnabled(true);
        threadMXBean.setThreadContentionMonitoringEnabled(true);
        try {
            this.properties = new Properties();
            this.properties.load(new FileInputStream(ConstantsOst.FILENAME_PROPERTIES));
            this.lastFileCaretPosition = Long.parseLong(properties.getProperty(ConstantsOst.PR_READING));
            this.lastWritePosition = Long.parseLong(properties.getProperty(ConstantsOst.PR_WRITING));
        }
        catch (NullPointerException | NumberFormatException e) {
            this.lastFileCaretPosition = 0;
            File file = new File("tmp_" + Paths.get(fileName).getFileName());
            if (file.exists()) {
                this.lastWritePosition = file.length();
            }
            else {
                this.lastWritePosition = 0;
            }
        }
        catch (IOException e) {
            this.properties = new Properties();
        }
    }
    
    String clearPositions() {
        StringBuilder stringBuilder = new StringBuilder();
        File filePath = null;
        try {
            filePath = new File(fileName);
        }
        catch (NullPointerException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        String fileNameLocal = filePath.getName();
        try {
            this.lastFileCaretPosition = 0;
            this.lastWritePosition = 0;
            Path absPath = Paths.get("tmp_" + fileNameLocal).toAbsolutePath();
            boolean tmpDel = Files.deleteIfExists(absPath);
            Object read = properties.setProperty(ConstantsOst.PR_READING, "0");
            Object write = properties.setProperty(ConstantsOst.PR_WRITING, "0");
            properties.store(new FileOutputStream(ConstantsOst.FILENAME_PROPERTIES), "clearPositions");
            stringBuilder.append(tmpDel).append(" deleting post file: ").append(absPath).append(". ").append(read).append(" read, ").append(write).append(" write.");
        }
        catch (IOException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        stringBuilder.append("\n\n").append(threadMXBean.getThreadInfo(Thread.currentThread().getId()));
        return stringBuilder.toString();
    }
    
    String copyFile(String newCP) {
        if (newCP.equalsIgnoreCase("y")) {
            System.out.println(clearPositions());
        }
        else if (newCP.equals("e")) {
            return "NO COPY!";
        }
        else if (newCP.equals("c")) {
            return ConstantsOst.STR_NOT_READY_YET + "\n" + threadMXBean.getThreadInfo(Thread.currentThread().getId());
        }
        int megaByte = ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES;
        StringBuilder stringBuilder = new StringBuilder();
        long tmpFileLen;
        try {
            tmpFileLen = new File("tmp_" + Paths.get(fileName).getFileName()).length();
            stringBuilder.append(tmpFileLen).append(" bytes copied\n");
            final long lengthOfCopy = new File(fileName).length();
            int mb30 = (ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES) * 30;
            if (lengthOfCopy < mb30) {
                properties.setProperty(ConstantsOst.PR_CAPACITY, String.valueOf(lengthOfCopy));
            }
            else {
                BigDecimal capLong = BigDecimal.valueOf((float) lengthOfCopy).divide(BigDecimal.valueOf((float) mb30), RoundingMode.HALF_DOWN);
                BigDecimal bufLen = BigDecimal.valueOf(lengthOfCopy).divide(capLong, RoundingMode.HALF_DOWN);
        
                long floor = lengthOfCopy - bufLen.longValue() * capLong.longValue();
                properties.setProperty(ConstantsOst.PR_CAPACITY, String.valueOf(bufLen));
                properties.setProperty(ConstantsOst.PR_CAPFLOOR, String.valueOf(floor));
            }
            long totalMegaBytesToCopy = lengthOfCopy / megaByte;
            while (true) {
                long positionOfCopy = readRNDFileContentFromPosition();
                long mBytesCopied = positionOfCopy / megaByte;
                String copiedStr = mBytesCopied + "/" + totalMegaBytesToCopy + " mb readied";
                System.out.println(copiedStr);
                if (positionOfCopy >= lengthOfCopy) {
                    break;
                }
            }
        }
        catch (NullPointerException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TFormsOST().fromArray(e));
        }
        stringBuilder.append(Paths.get(properties.getProperty(ConstantsOst.PR_TMPFILE)).toAbsolutePath()).append("\n\n");
        stringBuilder.append(threadMXBean.getThreadInfo(Thread.currentThread().getId()));
        return stringBuilder.toString();
    }
    
    private long readRNDFileContentFromPosition() {
        int capacity = ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES;
        byte[] bytes;
        try {
            capacity = Integer.parseInt(properties.getProperty(ConstantsOst.PR_CAPACITY));
        }
        catch (Exception e) {
            properties.setProperty(ConstantsOst.PR_CAPACITY, String.valueOf(ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES));
        }
        bytes = new byte[capacity];
        try {
            File file;
            try {
                file = new File(fileName);
            }
            catch (NullPointerException e) {
                messageToUser.error("NO FILE GIVEN! Please, set the file...");
                throw new RejectedExecutionException("No File");
            }
            PSTRAFileContent content;
            try {
                content = new PSTRAFileContent(file);
            }
            catch (FileNotFoundException e) {
                content = new PSTRAFileContent(new File(new CharsetEncoding(ConstantsOst.CP_WINDOWS_1251).getStrInAnotherCharset(fileName)));
            }
            if (lastWritePosition > lastFileCaretPosition) {
                return lastWritePosition;
            }
            else {
                content.seek(lastFileCaretPosition);
            }
            int read = content.read(bytes);
            this.lastFileCaretPosition += read;
            properties.setProperty(ConstantsOst.PR_READING, String.valueOf(lastFileCaretPosition));
            properties.setProperty(ConstantsOst.PR_WRITING, String.valueOf(writeReaded(bytes, "tmp_" + file.getName())));
            properties.setProperty("file", file.getAbsolutePath());
            properties.setProperty(ConstantsOst.PR_TMPFILE, "tmp_" + file.getName());
            properties.store(new FileOutputStream(ConstantsOst.FILENAME_PROPERTIES), "readRNDFileContentFromPosition");
            return lastFileCaretPosition;
        }
        catch (IOException e) {
            messageToUser.error(getClass().getSimpleName(), e.getMessage(), new TFormsOST().fromArray(e));
            return -1;
        }
    }
    
    private long writeReaded(byte[] array, String pathOfTempFile) {
        if (!fileName.contains(pathOfTempFile.replace("tmp_", ""))) {
            throw new IllegalComponentStateException(fileName + " is not " + pathOfTempFile);
        }
        try (RandomAccessFile pstWriteFile = new RandomAccessFile(pathOfTempFile, "rw")) {
            pstWriteFile.seek(lastWritePosition);
            pstWriteFile.write(array);
            this.lastWritePosition += array.length;
            return lastWritePosition;
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
            return -1;
        }
    }
}
