package ru.vachok.ostpst.fileworks.nopst;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.TFormsOST;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @since 28.05.2019 (9:48) */
public class HardCopy implements FileWorker {
    
    
    private final long startStamp = System.currentTimeMillis();
    
    private Map<String, String> prefMap = new HashMap<>();
    
    private String readFileName;
    
    private String writeFileName;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    public HardCopy(String readFileName, String writeFileName) {
        this.readFileName = readFileName;
        this.writeFileName = writeFileName;
        initMethod();
    }
    
    public HardCopy(String readFileName) {
        this.readFileName = readFileName;
        this.writeFileName = new StringBuilder()
            .append(Paths.get(".").normalize().toAbsolutePath())
            .append("tmp_")
            .append(Paths.get(readFileName).toFile().getName()).toString();
        initMethod();
    }
    
    @Override public String chkFile() {
        String readFileNamePref = prefMap.getOrDefault("readFileName", "");
        String writeFileNamePref = prefMap.getOrDefault("writeFileName", "");
        
        if (readFileNamePref.equalsIgnoreCase(readFileName) || (writeFileName.contains(readFileName))) {
            long positionOfWrite = Long.parseLong(prefMap.getOrDefault("positionOfWrite", String.valueOf(0)));
            long positionOfRead = Long.parseLong(prefMap.getOrDefault("positionOfRead", String.valueOf(0)));
            if (positionOfWrite > 0) {
                continuousCopy();
            }
            else {
                processNewCopy();
            }
        }
        else {
            prefMap.put("readFileName", readFileName);
            processNewCopy();
        }
        return "Read from: " + readFileName + "\nWrite to: " + writeFileName;
    }
    
    @Override public String clearCopy() {
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.clear();
            prefMap.clear();
            return "Using preferences - true";
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties("ostpst-" + getClass().getSimpleName());
            initProperties.delProps();
            return "Using preferences - false";
        }
    }
    
    @Override public long continuousCopy() {
        
        long writePos = Long.parseLong(prefMap.getOrDefault("positionOfWrite", "0"));
        long readPos = Long.parseLong(prefMap.getOrDefault("positionOfRead", "0"));
        
        prefMap.put("positionOfRead", String.valueOf(readPos));
        
        do {
            readPos = Long.parseLong(prefMap.get("positionOfRead"));
            writePos = continuousCopyProc();
        } while (readPos != writePos);
        return readPos;
    }
    
    @Override public void showCurrentResult() {
        long readMB = Long.parseLong(prefMap.getOrDefault("positionOfRead", "0"));
        long writeMB = Long.parseLong(prefMap.getOrDefault("positionOfWrite", "0"));
        readMB /= (ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES);
        writeMB /= (ConstantsOst.KBYTE_BYTES * ConstantsOst.KBYTE_BYTES);
        System.out.println("Read/Write: " + readMB + "/" + writeMB);
        long toSecondsDura = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startStamp);
        if (toSecondsDura == 0) {
            toSecondsDura = 1;
        }
        System.out.println("Time: " + toSecondsDura + " speed: " + (float) writeMB / (float) toSecondsDura + " mb/sec");
    }
    
    @Override public String saveAndExit() {
        boolean pr = savePr();
        return getClass().getSimpleName() + ".saveAndExit = " + pr;
    }
    
    @Override public boolean processNewCopy() {
        String clearCopyStr = clearCopy();
        continuousCopy();
        return savePr();
    }
    
    private long continuousCopyProc() {
        
        long writePos = Long.parseLong(prefMap.getOrDefault("positionOfWrite", "0"));
        long readPos = Long.parseLong(prefMap.getOrDefault("positionOfRead", "0"));
        
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(readFileName, "r")) {
            randomAccessFile.seek(readPos);
            byte[] bufBytes = new byte[8192];
            randomAccessFile.read(bufBytes);
            prefMap.put("positionOfRead", String.valueOf(randomAccessFile.getFilePointer()));
            
            try (RandomAccessFile outFile = new RandomAccessFile(writeFileName, "rw")) {
                outFile.seek(writePos);
                outFile.write(bufBytes);
                prefMap.put("positionOfWrite", String.valueOf(outFile.getFilePointer()));
                savePr();
                return outFile.getFilePointer();
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
            preferences.putLong("positionOfWrite", Long.parseLong(prefMap.getOrDefault("positionOfWrite", String.valueOf(0))));
            preferences.putLong("positionOfRead", Long.parseLong(prefMap.getOrDefault("positionOfRead", String.valueOf(0))));
            
            preferences.put("readFileName", readFileName);
            preferences.put("writeFileName", writeFileName);
            retBool = true;
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties("ostpst-" + getClass().getSimpleName());
            Properties properties = new Properties();
            properties.putAll(prefMap);
            initProperties.setProps(properties);
            retBool = true;
        }
        return retBool;
    }
    
    private void initMethod() {
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.sync();
            prefMap.putIfAbsent("readFileName", preferences.get("readFileName", ""));
            prefMap.putIfAbsent("writeFileName", preferences.get("writeFileName", ""));
            
            prefMap.putIfAbsent("positionOfWrite", preferences.get("positionOfWrite", String.valueOf(0)));
            prefMap.putIfAbsent("positionOfRead", preferences.get("positionOfRead", String.valueOf(0)));
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties("ostpst-" + getClass().getSimpleName());
            Properties properties = initProperties.getProps();
            
            prefMap.putIfAbsent("readFileName", properties.getProperty("readFileName", ""));
            prefMap.putIfAbsent("writeFileName", properties.getProperty("writeFileName", ""));
            
            prefMap.putIfAbsent("positionOfWrite", properties.getProperty("positionOfWrite", String.valueOf(0)));
            prefMap.putIfAbsent("positionOfRead", properties.getProperty("positionOfRead", String.valueOf(0)));
        }
        
    }
    
    
}
