package ru.vachok.ostpst.fileworks.nopst;


import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.ostpst.fileworks.FileWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


/**
 @since 30.05.2019 (11:04) */
public class Uploader implements FileWorker {
    
    
    private String readFileName;
    
    private String writeFileName;
    
    private long readingCRC;
    
    private byte[] bytes;
    
    public Uploader(String readFileName) {
        this.readFileName = readFileName;
        this.writeFileName = "tmp_" + new File(readFileName).getName();
        initMethod(writeFileName);
    }
    
    @Override public String chkFile() {
        if (bytes == null) {
            throw new UnsupportedOperationException("Unsupported for " + getClass().getSimpleName());
        }
        Checksum readCRC32 = new CRC32();
        Checksum writeCRC32 = new CRC32();
        
        readCRC32.reset();
        writeCRC32.reset();
        
        if (readingCRC == 0) {
            readCRC32.update(bytes, 0, bytes.length);
            this.readingCRC = readCRC32.getValue();
        }
        else {
            writeCRC32.update(bytes, 0, bytes.length);
        }
        String valueOf = String.valueOf(String.valueOf(readCRC32.getValue()).equalsIgnoreCase(String.valueOf(writeCRC32.getValue())));
        return valueOf;
    }
    
    @Override public String clearCopy() {
        try {
            PREFERENCES_USER_ROOT.clear();
            Files.deleteIfExists(Paths.get(writeFileName));
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties("ostpst-" + getClass().getSimpleName());
            boolean delProps = initProperties.delProps();
            return e.getMessage();
        }
        catch (IOException e) {
            new File(writeFileName).deleteOnExit();
            return e.getMessage();
        }
        return "ok";
    }
    
    @Override public long continuousCopy() {
        return 0;
    }
    
    @Override public void showCurrentResult() {
    
    }
    
    @Override public String saveAndExit() {
        return null;
    }
    
    @Override public boolean processNewCopy() {
        String clearCopy = clearCopy();
        long continuousCopy = continuousCopy();
        return clearCopy.equalsIgnoreCase("ok") && continuousCopy > 0;
    }
}
