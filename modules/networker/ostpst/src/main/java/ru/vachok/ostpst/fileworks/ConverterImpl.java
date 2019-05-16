package ru.vachok.ostpst.fileworks;


import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.utils.CharsetEncoding;
import ru.vachok.ostpst.utils.FileSystemWorker;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.Callable;


/**
 @since 14.05.2019 (12:14) */
public class ConverterImpl implements MakeConvert {
    
    
    private String fileName;
    
    public ConverterImpl(String fileName) {
        this.fileName = new CharsetEncoding("windows-1251").getStrInAnotherCharset(fileName);
    }
    
    @Override public String convertToPST() {
        throw new IllegalComponentStateException("15.05.2019 (9:27)");
    }
    
    @Override public void saveFolders() {
        ParserFoldersWithAttachments parserFoldersWithAttachments = new ParserFoldersWithAttachments(fileName);
        System.out.println("pstContentToFoldersWithAttachments = " + parserFoldersWithAttachments.showFoldersIerarchy());
    }
    
    @Override public String saveContacts(String csvFileName) {
        StringBuilder stringBuilder = new StringBuilder();
        Path root = Paths.get(fileName).toAbsolutePath().getParent();
    
        if (csvFileName == null || csvFileName.isEmpty()) {
            csvFileName = root + FileSystemWorker.SYSTEM_DELIMITER + "contacts.csv";
        }
        File csvFile = new File(csvFileName);
        Callable<String> contacts = new ParserContacts(fileName, Objects.requireNonNull(csvFileName, "No CSV fileName given!"));
    
        try {
            File file = new File(contacts.call());
            if (file.length() > 4096) {
                stringBuilder.append(file.toPath());
            }
            else {
                stringBuilder.append(csvFileName + " is " + csvFile.length());
            }
        }
        catch (Exception e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
    
    @Override public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public String copyierWithSave() {
        RNDFileCopy rndFileCopy = new RNDFileCopy(fileName);
        return rndFileCopy.copyFile();
    }
    
    @Override public String showListFolders() {
        ParserFoldersWithAttachments parserFoldersWithAttachments = new ParserFoldersWithAttachments(fileName);
        return parserFoldersWithAttachments.showFoldersIerarchy();
    }
    
    @Override public Deque<String> getDequeFolderNamesAndWriteToDisk() {
        ParserFoldersWithAttachments parserFoldersWithAttachments = new ParserFoldersWithAttachments(fileName);
        return parserFoldersWithAttachments.getDeqFolderNamesAndWriteToDisk();
    }
    
    @Override public String clearCopy() {
        String clearPositions = new RNDFileCopy(fileName).clearPositions();
        return clearPositions;
    }
}
