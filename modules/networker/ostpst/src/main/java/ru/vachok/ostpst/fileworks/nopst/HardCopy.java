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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @since 28.05.2019 (9:48) */
public class HardCopy implements FileWorker {
    
    
    public static final String PR_WRITEFILENAME = "writeFileName";
    
    private static final String STR_RESPREFS = " resetting prefs: ";
    
    private final long startStamp = System.currentTimeMillis();
    
    private Map<String, String> prefMap = new HashMap<>();
    
    private String readFileName;
    
    private String writeFileName;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private int bufLen = 8192 * ConstantsOst.KBYTE_BYTES;
    
    public HardCopy(String readFileName, String writeFileName) {
        this.readFileName = readFileName;
        this.writeFileName = writeFileName;
        try {
            initMethod();
        }
        catch (IllegalStateException e) {
            System.err.println(e.getMessage() + STR_RESPREFS + clearCopy());
        }
    }
    
    public HardCopy(String readFileName) {
        this.readFileName = readFileName;
        this.writeFileName = new StringBuilder()
            .append(Paths.get(".").normalize().toAbsolutePath())
            .append("tmp_")
            .append(Paths.get(readFileName).toFile().getName()).toString();
        try {
            initMethod();
        }
        catch (IllegalStateException e) {
            System.err.println(e.getMessage() + STR_RESPREFS + clearCopy());
        }
    }
    
    public void setBufLen(int bufLen) {
        this.bufLen = bufLen;
    }
    
    @Override public String chkFile() {
        String readFileNamePref = prefMap.getOrDefault(ConstantsOst.PR_READFILENAME, "");
        String writeFileNamePref = prefMap.getOrDefault(PR_WRITEFILENAME, "");
        
        if (readFileNamePref.equalsIgnoreCase(readFileName) || (writeFileName.contains(readFileName))) {
            long positionOfWrite = Long.parseLong(prefMap.getOrDefault(ConstantsOst.PR_POSWRITE, String.valueOf(0)));
            long positionOfRead = Long.parseLong(prefMap.getOrDefault(ConstantsOst.PR_POSREAD, String.valueOf(0)));
            if (positionOfWrite > 0) {
                continuousCopy();
            }
            else {
                processNewCopy();
            }
        }
        else {
            prefMap.put(ConstantsOst.PR_READFILENAME, readFileName);
            processNewCopy();
        }
        return "Read from: " + readFileName + "\nWrite to: " + writeFileName;
    }
    
    @Override public String clearCopy() {
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.clear();
            prefMap.clear();
            new File(writeFileName).delete();
            return "Using preferences - true";
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties(ConstantsOst.APPNAME_OSTPST + getClass().getSimpleName());
            initProperties.delProps();
            new File(writeFileName).deleteOnExit();
            return "Using preferences - false";
        }
    }
    
    @Override public long continuousCopy() {
    
        long writePos = new File(writeFileName).length();
        long readPos = Long.parseLong(prefMap.getOrDefault(ConstantsOst.PR_POSREAD, "0"));
    
        prefMap.put(ConstantsOst.PR_POSREAD, String.valueOf(readPos));
        
        do {
            writePos = new File(writeFileName).length();
            readPos = continuousCopyProc();
            showCurrentResult();
        } while (readPos != writePos);
        saveAndExit();
        return readPos;
    }
    
    @Override public void showCurrentResult() {
        long readMB = Long.parseLong(prefMap.getOrDefault(ConstantsOst.PR_POSREAD, "0"));
        long totalMB = new File(readFileName).length();
        readMB /= (ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES);
        totalMB /= (ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES);
        long toSecondsDura = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startStamp);
        if (toSecondsDura == 0) {
            toSecondsDura = 1;
        }
        System.out
            .println("Read/Total: " + readMB + "/" + totalMB + ". Time: " + toSecondsDura + " speed: " + (float) readMB / (float) toSecondsDura + " mb/sec. Buffer = " + bufLen / ConstantsOst.KBYTE_BYTES);
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
        String showMe = prefMap.get(ConstantsOst.PR_POSREAD) + " read, " + prefMap.get(ConstantsOst.PR_POSWRITE) + ConstantsOst.STR_WRITE;
        final StringBuilder sb = new StringBuilder("HardCopy{");
        sb.append("bufLen=").append(bufLen);
        sb.append(", readFileName='").append(readFileName).append('\'');
        sb.append(", startStamp=").append(new Date(startStamp));
        sb.append(", writeFileName='").append(writeFileName).append('\'');
        sb.append("\n").append(showMe).append('\'');
        sb.append("\nBuffer = ").append(bufLen / ConstantsOst.KBYTE_BYTES).append('\'');
        Preferences preferences = Preferences.userRoot();
        sb.append("\n").append(new TFormsOST().fromArray(prefMap)).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private long continuousCopyProc() {
        long writePos = Long.parseLong(prefMap.getOrDefault(ConstantsOst.PR_POSWRITE, "0"));
        long readPos = Long.parseLong(prefMap.getOrDefault(ConstantsOst.PR_POSREAD, String.valueOf(new File(readFileName).length())));
        long toEnd = new File(readFileName).length() - new File(writeFileName).length();
        if (bufLen >= toEnd & toEnd != 0) {
            this.bufLen = (int) (toEnd);
        }
        
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(readFileName, "r");
             RandomAccessFile outFile = new RandomAccessFile(writeFileName, "rw")
        ) {
            randomAccessFile.seek(readPos);
            byte[] bufBytes = new byte[this.bufLen];
            randomAccessFile.read(bufBytes);
    
            prefMap.put(ConstantsOst.PR_POSREAD, String.valueOf(randomAccessFile.getFilePointer()));
            System.out.println(toEnd / ConstantsOst.KBYTE_BYTES + " left. new byte[" + (bufBytes.length / ConstantsOst.KBYTE_BYTES) + "]");
            outFile.seek(writePos);
            if (new File(writeFileName).length() != new File(readFileName).length()) {
                outFile.write(bufBytes);
            }
            prefMap.put(ConstantsOst.PR_POSWRITE, String.valueOf(new File(writeFileName).length()));
            savePr();
            return randomAccessFile.getFilePointer();
            
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
            long pWrite = Long.parseLong(prefMap.getOrDefault(ConstantsOst.PR_POSWRITE, String.valueOf(0)));
            preferences.putLong(ConstantsOst.PR_POSWRITE, pWrite);
            long pRead = Long.parseLong(prefMap.getOrDefault(ConstantsOst.PR_POSREAD, String.valueOf(0)));
            preferences.putLong(ConstantsOst.PR_POSREAD, pRead);
            preferences.put(ConstantsOst.PR_READFILENAME, readFileName);
            preferences.put(PR_WRITEFILENAME, writeFileName);
            preferences.put("crc32" + bufLen, prefMap.getOrDefault("crc32" + bufLen, "-1"));
            if (pRead == pWrite) {
                retBool = true;
            }
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties(ConstantsOst.APPNAME_OSTPST + getClass().getSimpleName());
            Properties properties = new Properties();
            properties.putAll(prefMap);
            initProperties.setProps(properties);
            retBool = true;
        }
        return retBool;
    }
    
    private void initMethod() throws IllegalStateException {
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.sync();
            prefMap.putIfAbsent(ConstantsOst.PR_READFILENAME, preferences.get(ConstantsOst.PR_READFILENAME, ""));
            prefMap.putIfAbsent(PR_WRITEFILENAME, preferences.get(PR_WRITEFILENAME, ""));
    
            prefMap.putIfAbsent(ConstantsOst.PR_POSWRITE, String.valueOf(new File(writeFileName).length()));
            prefMap.putIfAbsent(ConstantsOst.PR_POSREAD, preferences.get(ConstantsOst.PR_POSREAD, String.valueOf(0)));
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties(ConstantsOst.APPNAME_OSTPST + getClass().getSimpleName());
            Properties properties = initProperties.getProps();
    
            prefMap.putIfAbsent(ConstantsOst.PR_READFILENAME, properties.getProperty(ConstantsOst.PR_READFILENAME, ""));
            prefMap.putIfAbsent(PR_WRITEFILENAME, String.valueOf(new File(writeFileName).length()));
    
            prefMap.putIfAbsent(ConstantsOst.PR_POSWRITE, properties.getProperty(ConstantsOst.PR_POSWRITE, String.valueOf(0)));
            prefMap.putIfAbsent(ConstantsOst.PR_POSREAD, properties.getProperty(ConstantsOst.PR_POSREAD, String.valueOf(0)));
        }
        String poRead = prefMap.get(ConstantsOst.PR_POSREAD);
        String poWrite = prefMap.get(ConstantsOst.PR_POSWRITE);
        if (!(poRead.equals(poWrite))) {
            throw new IllegalStateException("ConstantsOst.PR_POSREAD (" + poRead + ") != ConstantsOst.PR_POSWRITE (" + poWrite + ")");
        }
    }
}
