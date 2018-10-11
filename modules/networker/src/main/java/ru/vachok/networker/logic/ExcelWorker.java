package ru.vachok.networker.logic;


import org.apache.poi.POIDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.OutputStream;

/**
 @since 11.10.2018 (12:58) */
public class ExcelWorker extends POIDocument {

    public ExcelWorker(POIFSFileSystem fs) {
        super(fs);
    }

    @Override
    public void write() {

    }

    @Override
    public void write(File newFile) {

    }

    @Override
    public void write(OutputStream out) {

    }
}
