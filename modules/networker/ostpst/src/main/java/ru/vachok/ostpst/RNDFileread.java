package ru.vachok.ostpst;


import com.pff.PSTRAFileContent;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.*;
import java.util.Properties;


/**
 @since 30.04.2019 (9:36) */
class RNDFileread {
    
    
    private PSTRAFileContent content;
    
    private long lastFileCaretPosition;
    
    private long lastWritePosition;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private Properties properties;
    
    public RNDFileread(PSTRAFileContent content) {
        this.content = content;
        try {
            this.properties = new Properties();
            this.properties.load(new FileInputStream("app.properties"));
            this.lastFileCaretPosition = Long.parseLong(properties.getProperty("reading"));
            this.lastWritePosition = Long.parseLong(properties.getProperty("write"));
        }
        catch (NullPointerException e) {
            this.lastFileCaretPosition = 0;
            this.lastWritePosition = new File("test.pst").length();
        }
        catch (IOException e) {
            properties = new Properties();
        }
    }
    
    long readRNDFileContentFromPosition() {
        int capacity = 1024 * 1024 * 500;
        byte[] bytes = new byte[capacity];
        try {
            content.seek(lastFileCaretPosition);
            int read = content.read(bytes);
            this.lastFileCaretPosition += read;
            properties.setProperty("reading", String.valueOf(lastFileCaretPosition));
            properties.setProperty("write", String.valueOf(writeReaded(bytes)));
            properties.store(new FileOutputStream("app.properties"), "");
            return lastFileCaretPosition;
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
            return -1;
        }
    }
    
    private long writeReaded(byte[] array) {
        try (RandomAccessFile pstWriteFile = new RandomAccessFile("test.pst", "rw")) {
            pstWriteFile.seek(lastWritePosition);
            pstWriteFile.write(array);
            this.lastWritePosition += array.length;
            messageToUser.info(getClass().getSimpleName() + ".writeReaded", "filePointer", " = " + lastWritePosition);
            return lastWritePosition;
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
            return -1;
        }
    }
}
