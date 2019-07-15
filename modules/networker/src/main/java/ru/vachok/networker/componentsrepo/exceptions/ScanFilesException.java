// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.exceptions;


import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.schedule.DiapazonScan;

import java.io.File;
import java.util.Map;


/**
 @since 08.07.2019 (12:40) */
public class ScanFilesException extends IllegalStateException {
    
    
    private Map<String, File> scanFiles;
    
    private String msg;
    
    public ScanFilesException() {
        this.scanFiles = DiapazonScan.getInstance().editScanFiles();
        this.msg = new TForms().fromArray(scanFiles);
    }
    
    public ScanFilesException(String msg) {
        this.msg = msg;
    }
    
    @Override public String getMessage() {
        return msg;
    }
}
