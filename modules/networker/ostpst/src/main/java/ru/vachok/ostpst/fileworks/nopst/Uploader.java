// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.fileworks.nopst;


import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.ostpst.ConstantsOst;
import ru.vachok.ostpst.fileworks.FileWorker;
import ru.vachok.ostpst.utils.CharsetEncoding;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
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
    
    private final long startStamp = System.currentTimeMillis();
    
    private boolean isVerbose;
    
    private int bytesBuffer = ConstantsOst.KBYTE_BYTES;
    
    public Uploader(String readFileName) {
        this.readFileName = readFileName;
        this.writeFileName = "tmp_" + new File(readFileName).getName();
        PREFERENCES_USER_ROOT.put(ConstantsOst.PR_WRITEFILENAME, writeFileName);
        PREFERENCES_USER_ROOT.put(ConstantsOst.PR_READFILENAME, readFileName);
        initMethod(writeFileName);
    }
    
    protected Uploader(String readFileName, String writeFileName) {
        this.readFileName = readFileName;
        boolean isFile = Paths.get(readFileName).toAbsolutePath().normalize().toFile().isFile();
        if (!isFile) {
            this.readFileName = new CharsetEncoding("windows-1251", "UTF8").getStrInAnotherCharset(readFileName);
        }
        this.writeFileName = writeFileName;
        PREFERENCES_USER_ROOT.put(ConstantsOst.PR_WRITEFILENAME, this.writeFileName);
        PREFERENCES_USER_ROOT.put(ConstantsOst.PR_READFILENAME, this.readFileName);
        initMethod(writeFileName);
    }
    
    public void setVerbose(boolean verbose) {
        isVerbose = verbose;
    }
    
    public int getBytesBuffer() {
        return bytesBuffer;
    }
    
    public void setBytesBuffer(int bytesBuffer) {
        this.bytesBuffer = bytesBuffer;
    }
    
    @Override public String chkFile() {
        if (bytes == null) {
            throw new UnsupportedOperationException("Unsupported for " + getClass().getSimpleName());
        }
        String valueOf = "false";
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
            if (this.readingCRC == writeCRC32.getValue()) {
                valueOf = "true";
            }
            this.readingCRC = 0;
        }
        return valueOf;
    }
    
    @Override public String clearCopy() {
        try {
            PREF_MAP.clear();
            PREFERENCES_USER_ROOT.clear();
            Files.deleteIfExists(Paths.get(writeFileName));
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties("ostpst-" + getClass().getSimpleName());
            boolean delProps = initProperties.delProps();
            return e.getMessage();
        }
        catch (IOException e) {
            return e.getMessage();
        }
        initMethod(writeFileName);
        return "clear ok";
    }
    
    @Override public long continuousCopy() {
        long writePos = Long.parseLong(PREF_MAP.get(ConstantsOst.PR_POSWRITE));
        long readPos = Long.parseLong(PREF_MAP.get(ConstantsOst.PR_POSREAD));
        do {
            readPos = getRead();
            writePos = getWrite(readPos);
            if (writePos != readPos) {
                long l = readPos - writePos;
                System.out.println(l + " difference. Rejected.");
                return l;
            }
            else {
                writePos = new File(writeFileName).length();
            }
        } while (readPos == writePos);
    
        return readPos - writePos;
    }
    
    @Override public void showCurrentResult() {
        long writeKB = Long.parseLong(PREF_MAP.get(ConstantsOst.PR_POSWRITE)) / ConstantsOst.KBYTE_BYTES;
        long readKB = Long.parseLong(PREF_MAP.get(ConstantsOst.PR_POSREAD)) / ConstantsOst.KBYTE_BYTES;
        
        long leftKB = (new File(readFileName).length() / ConstantsOst.KBYTE_BYTES) - writeKB;
        System.out.println(new StringBuilder()
                .append(readKB).append(" kb read from: ")
                .append(PREF_MAP.get(ConstantsOst.PR_READFILENAME)).append(", ")
                .append(writeKB).append(" kb write to: ")
                .append(PREF_MAP.get(ConstantsOst.PR_WRITEFILENAME)).append(", speed = ")
                .append(getSpeed(writeKB)).append("\n")
                .append(leftKB).append(" kb left ")
            .append(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()-startStamp)).append(" sec elapsed, ")
            .append(bytesBuffer).append(" bytes per one block.")
            /*.append(getProc(leftKB)).append(" %")*/);
        try {
            PREF_MAP.forEach(PREFERENCES_USER_ROOT::put);
            PREFERENCES_USER_ROOT.sync();
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new DBRegProperties("ostpst-" + getClass().getSimpleName());
            Properties properties = initProperties.getProps();
            properties.putAll(PREF_MAP);
            boolean setProps = initProperties.setProps(properties);
            System.out.println("properties = " + setProps);
        }
    }
    
    @Override public String saveAndExit() {
        throw new IllegalComponentStateException("30.05.2019 (13:27)");
    }
    
    private int getProc(long kb) {
        long readFileKB = new File(readFileName).length() / ConstantsOst.KBYTE_BYTES;
        BigDecimal bigDecimal = BigDecimal.valueOf((float) kb).divide(BigDecimal.valueOf((float) readFileKB), RoundingMode.HALF_EVEN);
        int retInt = (int) (bigDecimal.doubleValue() * 100);
        throw new IllegalComponentStateException("30.05.2019 (17:53)");
    }
    
    private String getSpeed(long writeKB) {
        long secondsToWork = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startStamp);
        if (secondsToWork == 0) {
            secondsToWork = 1;
        }
        long kbSec = writeKB / secondsToWork;
        return kbSec + " kb/sec (" + kbSec / ConstantsOst.KBYTE_BYTES + " mb)";
    }
    
    @Override public boolean processNewCopy() {
        String clearCopy = clearCopy();
        long continuousCopy = continuousCopy();
        return clearCopy.equalsIgnoreCase("ok") && continuousCopy > 0;
    }
    
    private long getRead() {
        try (RandomAccessFile readFile = new RandomAccessFile(readFileName, "r")) {
            long readPosition = Long.parseLong(PREF_MAP.get(ConstantsOst.PR_POSREAD));
            int lenMinusBuf = (int) (new File(readFileName).length() - readPosition);
            if (bytesBuffer > lenMinusBuf && lenMinusBuf > 0) {
                this.bytesBuffer = lenMinusBuf;
            }
            this.bytes = new byte[bytesBuffer];
            readFile.seek(readPosition);
            int read = readFile.read(bytes);
            if (isVerbose) {
                System.out.println("read = " + read);
            }
            long readFileFilePointer = readFile.getFilePointer();
            if (chkFile().equalsIgnoreCase("false")) {
                PREF_MAP.put(ConstantsOst.PR_POSREAD, String.valueOf(readFileFilePointer));
            }
            return readFileFilePointer;
        }
        catch (IOException e) {
            return -2;
        }
    }
    
    private long getWrite(long readPosition) {
        try (RandomAccessFile writeFile = new RandomAccessFile(writeFileName, "rws")) {
            long writePosition = new File(writeFileName).length();
            if (readPosition < writePosition) {
                clearCopy();
            }
            else if (writePosition == readPosition) {
                return 0;
            }
            else {
                writeFile.seek(writePosition);
                writeFile.write(bytes);
            }
            writePosition = writeFile.getFilePointer();
            if (chkFile().equalsIgnoreCase("true")) {
                PREF_MAP.put(ConstantsOst.PR_POSWRITE, String.valueOf(writePosition));
                try {
                    showCurrentResult();
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            
            return writePosition;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }
}
