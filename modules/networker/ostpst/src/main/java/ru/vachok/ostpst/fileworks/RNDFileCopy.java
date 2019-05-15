// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks;


import com.pff.PSTRAFileContent;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsFor;
import ru.vachok.ostpst.utils.TForms;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
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
    
    private Properties properties;
    
    /**
     @param fileName may be null
     */
    RNDFileCopy(String fileName) {
        this.fileName = fileName;
        try {
            this.properties = new Properties();
            this.properties.load(new FileInputStream(ru.vachok.ostpst.ConstantsFor.FILENAME_PROPERTIES));
            this.lastFileCaretPosition = Long.parseLong(properties.getProperty(ru.vachok.ostpst.ConstantsFor.PR_READING));
            this.lastWritePosition = Long.parseLong(properties.getProperty(ru.vachok.ostpst.ConstantsFor.PR_WRITING));
        }
        catch (NullPointerException e) {
            this.lastFileCaretPosition = 0;
            this.fileName = properties.getProperty("file");
            this.lastWritePosition = new File("tmp_" + Paths.get(fileName).getFileName()).length();
        }
        catch (IOException e) {
            this.properties = new Properties();
        }
    }
    
    String clearPositions() {
        File filePath = new File(fileName);
        String fileNameP = filePath.getName();
        try {
            this.lastFileCaretPosition = 0;
            this.lastWritePosition = 0;
            boolean tmpDel = Files.deleteIfExists(Paths.get("tmp_" + fileNameP));
            Object read = properties.setProperty(ConstantsFor.PR_READING, "0");
            Object write = properties.setProperty(ConstantsFor.PR_WRITING, "0");
            properties.store(new FileOutputStream(ConstantsFor.FILENAME_PROPERTIES), "clearPositions");
            return tmpDel + " deleting post file, " + read + " read, " + write + " write.";
        }
        catch (IOException e) {
            return e.getMessage() + "\n" + new TForms().fromArray(e);
        }
    }
    
    String copyFile() {
        int megaByte = ConstantsFor.KBYTE_BYTES * ConstantsFor.KBYTE_BYTES;
        StringBuilder stringBuilder = new StringBuilder();
        long tmpFileLen = lastWritePosition;
        try {
            tmpFileLen = new File("tmp_" + Paths.get(fileName).getFileName()).length();
            stringBuilder.append(tmpFileLen).append(" bytes copied\n");
            
            long totalMegaBytesToCopy = new File(fileName).length() / megaByte;
            while (readRNDFileContentFromPosition() > 0) {
                String ANSI_CLEAR_SEQ = "\u001b[2J";
                System.out.println(ANSI_CLEAR_SEQ);
                long mBytesCopied = readRNDFileContentFromPosition() / megaByte;
                String copiedStr = mBytesCopied + " mb readied" + BigDecimal.valueOf((mBytesCopied / totalMegaBytesToCopy) * 100);
                System.out.print(copiedStr);
            }
        }
        catch (NullPointerException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e));
            this.lastWritePosition = 0;
        }
        return stringBuilder.toString();
    }
    
    private long readRNDFileContentFromPosition() {
        int capacity = ConstantsFor.KBYTE_BYTES * ConstantsFor.KBYTE_BYTES;
        byte[] bytes = new byte[ConstantsFor.KBYTE_BYTES * ConstantsFor.KBYTE_BYTES];
        try {
            capacity = Integer.parseInt(properties.getProperty(ru.vachok.ostpst.ConstantsFor.PR_CAPACITY));
        }
        catch (Exception e) {
            properties.setProperty(ru.vachok.ostpst.ConstantsFor.PR_CAPACITY, String.valueOf(ConstantsFor.KBYTE_BYTES * ConstantsFor.KBYTE_BYTES));
        }
        bytes = new byte[capacity];
        try {
            File file = null;
            try {
                file = new File(fileName);
            }
            catch (NullPointerException e) {
                messageToUser.error("NO FILE GIVEN! Please, set the file...");
                throw new RejectedExecutionException("No File");
            }
            PSTRAFileContent content = new PSTRAFileContent(file);
            content.seek(lastFileCaretPosition);
            int read = content.read(bytes);
            this.lastFileCaretPosition += read;
            properties.setProperty(ru.vachok.ostpst.ConstantsFor.PR_READING, String.valueOf(lastFileCaretPosition));
            properties.setProperty(ru.vachok.ostpst.ConstantsFor.PR_WRITING, String.valueOf(writeReaded(bytes, "tmp_" + file.getName())));
            properties.setProperty("file", file.getAbsolutePath());
            properties.setProperty("tmpfile", "tmp_" + file.getName());
            properties.store(new FileOutputStream(ru.vachok.ostpst.ConstantsFor.FILENAME_PROPERTIES), "readRNDFileContentFromPosition");
            return lastFileCaretPosition;
        }
        catch (IOException e) {
            messageToUser.error(getClass().getSimpleName(), e.getMessage(), new TForms().fromArray(e));
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
