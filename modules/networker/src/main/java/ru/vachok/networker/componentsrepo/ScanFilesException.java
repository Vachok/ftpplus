package ru.vachok.networker.componentsrepo;


import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.schedule.DiapazonScan;

import java.io.File;
import java.util.Map;


/**
 @since 08.07.2019 (12:40) */
public class ScanFilesException extends IllegalStateException {
    
    
    private Map<String, File> scanFiles;
    
    public ScanFilesException() {
        this.scanFiles = DiapazonScan.getInstance().getScanFiles();
    }
    
    @Override public String getMessage() {
        return new TForms().fromArray(scanFiles);
    }
}
