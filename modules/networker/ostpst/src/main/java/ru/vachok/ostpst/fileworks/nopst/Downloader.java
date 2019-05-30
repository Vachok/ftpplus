// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @since 28.05.2019 (9:48) */
public class Downloader implements FileWorker {
    
    private static final String STR_RESPREFS = " resetting prefs: ";
    
    private final long startStamp = System.currentTimeMillis();
    
    private String readFileName;
    
    private String writeFileName;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private int bufLen = 8192 * ConstantsOst.KBYTE_BYTES;
    
    public Downloader(String readFileName, String writeFileName) {
        this.readFileName = readFileName;
        this.writeFileName = writeFileName;
        try {
            initMethod(writeFileName);
        }
        catch (IllegalStateException e) {
            System.err.println(e.getMessage() + STR_RESPREFS + clearCopy());
        }
    }
    
    public Downloader(String readFileName) {
        this.readFileName = readFileName;
        this.writeFileName = new StringBuilder()
            .append(Paths.get(".").normalize().toAbsolutePath())
            .append("tmp_")
            .append(Paths.get(readFileName).toFile().getName()).toString();
        try {
            initMethod(writeFileName);
        }
        catch (IllegalStateException e) {
            System.err.println(e.getMessage() + STR_RESPREFS + clearCopy());
        }
    }
    
    public void setBufLen(int bufLen) {
        this.bufLen = bufLen;
    }
    
    @Override public String chkFile() {
        String readFileNamePref = PREF_MAP.getOrDefault(ConstantsOst.PR_READFILENAME, "");
        String writeFileNamePref = PREF_MAP.getOrDefault(PR_WRITEFILENAME, "");
        
        if (readFileNamePref.equalsIgnoreCase(readFileName) || (writeFileName.contains(readFileName))) {
            long positionOfWrite = Long.parseLong(PREF_MAP.getOrDefault(ConstantsOst.PR_POSWRITE, String.valueOf(0)));
            long positionOfRead = Long.parseLong(PREF_MAP.getOrDefault(ConstantsOst.PR_POSREAD, String.valueOf(0)));
            if (positionOfWrite > 0) {
                continuousCopy();
            }
            else {
                processNewCopy();
            }
        }
        else {
            PREF_MAP.put(ConstantsOst.PR_READFILENAME, readFileName);
            processNewCopy();
        }
        return "Read from: " + readFileName + "\nWrite to: " + writeFileName;
    }
    
    @Override public String clearCopy() {
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.clear();
            PREF_MAP.clear();
            new File(writeFileName).delete();
            return "Using PREFERENCES_USER_ROOT - true";
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties(ConstantsOst.APPNAME_OSTPST + getClass().getSimpleName());
            initProperties.delProps();
            new File(writeFileName).deleteOnExit();
            return "Using PREFERENCES_USER_ROOT - false";
        }
    }
    
    @Override public long continuousCopy() {
    
        long writePos = new File(writeFileName).length();
        long readPos = Long.parseLong(PREF_MAP.getOrDefault(ConstantsOst.PR_POSREAD, "0"));
    
        PREF_MAP.put(ConstantsOst.PR_POSREAD, String.valueOf(readPos));
        
        do {
            writePos = Long.parseLong(PREF_MAP.getOrDefault(ConstantsOst.PR_WRITING, String.valueOf(writePos)));
            readPos = continuousCopyProc();
        } while (readPos != writePos);
        saveAndExit();
        return readPos;
    }
    
    @Override public void showCurrentResult() {
        long readMB = Long.parseLong(PREF_MAP.getOrDefault(ConstantsOst.PR_POSREAD, "0"));
        long totalMB = new File(readFileName).length();
        readMB /= (ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES);
        totalMB /= (ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES);
        long toSecondsDura = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startStamp);
        if (toSecondsDura == 0) {
            toSecondsDura = 1;
        }
        System.out.println("Read/Total: " + readMB + "/" + totalMB + ". Time: " + toSecondsDura + " speed: " + (float) readMB / (float) toSecondsDura + " mb/sec.");
    }
    
    @Override public String saveAndExit() {
        boolean pr = savePr();
        if (pr) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        return this + " is ok: " + pr;
    }
    
    @Override public boolean processNewCopy() {
        String clearCopyStr = clearCopy();
        continuousCopy();
        return savePr();
    }
    
    @Override public String toString() {
        String showMe = PREF_MAP.get(ConstantsOst.PR_POSREAD) + " read, " + PREF_MAP.get(ConstantsOst.PR_POSWRITE) + ConstantsOst.STR_WRITE;
        final StringBuilder sb = new StringBuilder("HardCopy{");
        sb.append("bufLen=").append(bufLen);
        sb.append(", readFileName='").append(readFileName).append('\'');
        sb.append(", startStamp=").append(new Date(startStamp));
        sb.append(", writeFileName='").append(writeFileName).append('\'');
        sb.append("\n").append(showMe).append('\'');
        sb.append("\nBuffer = ").append(bufLen / ConstantsOst.KBYTE_BYTES).append('\'');
        Preferences preferences = Preferences.userRoot();
        sb.append("\n").append(new TFormsOST().fromArray(PREF_MAP)).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private long continuousCopyProc() {
        long writePos = Long.parseLong(PREF_MAP.getOrDefault(ConstantsOst.PR_POSWRITE, "0"));
        long readPos = Long.parseLong(PREF_MAP.getOrDefault(ConstantsOst.PR_POSREAD, String.valueOf(new File(readFileName).length())));
        long toEnd = new File(readFileName).length() - new File(writeFileName).length();
        if (bufLen >= toEnd & toEnd != 0) {
            this.bufLen = (int) (toEnd);
        }
    
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(readFileName, "r")) {
            randomAccessFile.seek(readPos);
            byte[] bufBytes = new byte[this.bufLen];
            System.out.println(toEnd / ConstantsOst.KBYTE_BYTES + " left. new byte[" + (bufBytes.length / ConstantsOst.KBYTE_BYTES) + "]");
            try (RandomAccessFile outFile = new RandomAccessFile(writeFileName, "rwd")) {
                outFile.seek(writePos);
                randomAccessFile.read(bufBytes);
                if (new File(writeFileName).length() != new File(readFileName).length()) {
                    outFile.write(bufBytes);
                }
                PREF_MAP.put(ConstantsOst.PR_POSREAD, String.valueOf(randomAccessFile.getFilePointer()));
                PREF_MAP.put(ConstantsOst.PR_POSWRITE, String.valueOf(outFile.getFilePointer()));
                boolean isSavePr = savePr();
                if (isSavePr) {
                    showCurrentResult();
                }
                return randomAccessFile.getFilePointer();
            
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println(new TFormsOST().fromArray(e));
            return -1;
        }
    }
    
    private boolean savePr() {
        boolean retBool = false;
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.sync();
            long pWrite = Long.parseLong(PREF_MAP.getOrDefault(ConstantsOst.PR_POSWRITE, String.valueOf(0)));
            preferences.putLong(ConstantsOst.PR_POSWRITE, pWrite);
            long pRead = Long.parseLong(PREF_MAP.getOrDefault(ConstantsOst.PR_POSREAD, String.valueOf(0)));
            preferences.putLong(ConstantsOst.PR_POSREAD, pRead);
            preferences.put(ConstantsOst.PR_READFILENAME, readFileName);
            preferences.put(PR_WRITEFILENAME, writeFileName);
            preferences.put("crc32" + bufLen, PREF_MAP.getOrDefault("crc32" + bufLen, "-1"));
            if (pRead == pWrite) {
                retBool = true;
            }
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties(ConstantsOst.APPNAME_OSTPST + getClass().getSimpleName());
            Properties properties = new Properties();
            properties.putAll(PREF_MAP);
            initProperties.setProps(properties);
            retBool = true;
        }
        return retBool;
    }
    
}
