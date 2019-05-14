package ru.vachok.ostpst.fileworks;


import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


/**
 @since 14.05.2019 (12:14) */
public class ConverterImpl implements MakeConvert {
    
    
    private String fileName;
    
    public ConverterImpl(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public String convertToPST() {
        return null;
    }
    
    @Override public void saveFolders() {
    
    }
    
    @Override public String saveContacts(String csvFileName) {
        if (csvFileName == null) {
            Path root = Paths.get(fileName).toAbsolutePath().getParent();
            csvFileName = root + FileSystemWorker.SYSTEM_DELIMITER + "contacts.csv";
        }
        ParserContacts contacts = new ParserContacts(fileName, Objects.requireNonNull(csvFileName, "No CSV fileName given!"));
        contacts.run();
        return csvFileName;
    }
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public long copyierWithSave() {
        return 0;
    }
    
    @Override public String folderContentItemsString() {
        return null;
    }
}
